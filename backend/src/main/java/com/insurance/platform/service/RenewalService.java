package com.insurance.platform.service;

import com.insurance.platform.dto.renewal.RenewalRequest;
import com.insurance.platform.dto.renewal.RenewalResponse;
import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.exception.ForbiddenException;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.mapper.RenewalMapper;
import com.insurance.platform.model.entity.CustomerProfile;
import com.insurance.platform.model.entity.InsurancePolicy;
import com.insurance.platform.model.entity.PolicyRenewal;
import com.insurance.platform.model.enums.PolicyStatus;
import com.insurance.platform.model.enums.RenewalStatus;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.CustomerProfileRepository;
import com.insurance.platform.repository.InsurancePolicyRepository;
import com.insurance.platform.repository.PolicyRenewalRepository;
import com.insurance.platform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Renewal workflow: upcoming/expiring queries, deduped creation, completion and missed marking.
 */
@Service
public class RenewalService {

    private static final Logger log = LoggerFactory.getLogger(RenewalService.class);

    private final PolicyRenewalRepository renewalRepository;
    private final InsurancePolicyRepository policyRepository;
    private final CustomerProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final PolicyService policyService;
    private final RenewalMapper renewalMapper;
    private final AuditLogService auditLogService;

    public RenewalService(PolicyRenewalRepository renewalRepository,
                          InsurancePolicyRepository policyRepository,
                          CustomerProfileRepository profileRepository,
                          UserRepository userRepository,
                          PolicyService policyService,
                          RenewalMapper renewalMapper,
                          AuditLogService auditLogService) {
        this.renewalRepository = renewalRepository;
        this.policyRepository = policyRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.policyService = policyService;
        this.renewalMapper = renewalMapper;
        this.auditLogService = auditLogService;
    }

    /** Lists pending renewals visible to the caller. */
    @Transactional(readOnly = true)
    public List<RenewalResponse> upcoming(String email) {
        if (isAdmin(email)) {
            return renewalRepository.findByStatus(RenewalStatus.PENDING).stream()
                    .map(renewalMapper::toResponse).toList();
        }
        CustomerProfile customer = requireProfile(email);
        Long userId = customer.getUser() != null ? customer.getUser().getId() : null;
        return renewalRepository.findByPolicyCustomerId(userId).stream()
                .filter(r -> r.getStatus() == RenewalStatus.PENDING)
                .map(renewalMapper::toResponse).toList();
    }

    /** Lists owned policies expiring within the given number of days. */
    @Transactional(readOnly = true)
    public List<RenewalResponse> expiringWithinDays(String email, int days) {
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(days);
        List<InsurancePolicy> policies;
        if (isAdmin(email)) {
            policies = policyRepository.findByExpiryDateBetween(today, horizon);
        } else {
            CustomerProfile customer = requireProfile(email);
            Long userId = customer.getUser() != null ? customer.getUser().getId() : null;
            policies = policyRepository.findByCustomerIdAndExpiryDateBetween(userId, today, horizon);
        }
        return policies.stream().map(policy -> {
            RenewalResponse dto = new RenewalResponse();
            dto.setPolicyId(policy.getId());
            dto.setPolicyNumber(policy.getPolicyNumber());
            dto.setPreviousExpiryDate(policy.getExpiryDate());
            dto.setStatus(RenewalStatus.PENDING.name());
            dto.setDaysRemaining(daysRemaining(policy.getExpiryDate()));
            return dto;
        }).toList();
    }

    /** Lists owned policies that have already expired. */
    @Transactional(readOnly = true)
    public List<RenewalResponse> expired(String email) {
        LocalDate today = LocalDate.now();
        List<InsurancePolicy> policies;
        if (isAdmin(email)) {
            policies = policyRepository.findAll().stream()
                    .filter(p -> p.getExpiryDate() != null && p.getExpiryDate().isBefore(today))
                    .toList();
        } else {
            CustomerProfile customer = requireProfile(email);
            Long userId = customer.getUser() != null ? customer.getUser().getId() : null;
            policies = policyRepository.findByCustomerId(userId).stream()
                    .filter(p -> p.getExpiryDate() != null && p.getExpiryDate().isBefore(today))
                    .toList();
        }
        return policies.stream().map(policy -> {
            RenewalResponse dto = new RenewalResponse();
            dto.setPolicyId(policy.getId());
            dto.setPolicyNumber(policy.getPolicyNumber());
            dto.setPreviousExpiryDate(policy.getExpiryDate());
            dto.setStatus(RenewalStatus.PENDING.name());
            dto.setDaysRemaining(daysRemaining(policy.getExpiryDate()));
            return dto;
        }).toList();
    }

    /**
     * Creates a renewal for an owned policy. If a PENDING renewal already exists
     * it is returned (dedupe) instead of creating a duplicate.
     */
    @Transactional
    public RenewalResponse create(String email, Long policyId, RenewalRequest request) {
        InsurancePolicy policy = policyService.requireOwned(email, policyId);
        RenewalResponse existing = renewalRepository.findFirstByPolicyIdAndStatus(policyId, RenewalStatus.PENDING)
                .map(renewalMapper::toResponse).orElse(null);
        if (existing != null) {
            log.info("Deduped renewal creation for policy {}", policy.getPolicyNumber());
            return existing;
        }
        PolicyRenewal renewal = new PolicyRenewal();
        renewal.setPolicy(policy);
        renewal.setPreviousExpiryDate(policy.getExpiryDate());
        LocalDate renewalDate = request.getRenewalDate() != null ? request.getRenewalDate() : LocalDate.now();
        LocalDate newExpiry = request.getNewExpiryDate() != null ? request.getNewExpiryDate()
                : (policy.getExpiryDate() != null ? policy.getExpiryDate().plusYears(1) : renewalDate.plusYears(1));
        if (!newExpiry.isAfter(policy.getExpiryDate() != null ? policy.getExpiryDate() : renewalDate.minusDays(1))) {
            throw new BadRequestException("New expiry date must be after the current expiry date");
        }
        renewal.setRequestedAt(renewalDate.atStartOfDay());
        renewal.setNewExpiryDate(newExpiry);
        renewal.setRenewalPremium(request.getRenewalPremium() != null
                ? request.getRenewalPremium() : policy.getPremiumAmount());
        renewal.setStatus(RenewalStatus.PENDING);
        renewal.setRemarks(request.getNotes());
        PolicyRenewal saved = renewalRepository.save(renewal);
        auditLogService.record(policy.getCustomer(), "CREATE", "PolicyRenewal",
                String.valueOf(saved.getId()), "Renewal created for " + policy.getPolicyNumber(), null);
        return renewalMapper.toResponse(saved);
    }

    /**
     * Completes a renewal: marks it COMPLETED and extends the policy expiry,
     * reactivating the policy status.
     */
    @Transactional
    public RenewalResponse complete(String email, Long renewalId) {
        PolicyRenewal renewal = requireOwned(email, renewalId);
        renewal.setStatus(RenewalStatus.COMPLETED);
        InsurancePolicy policy = renewal.getPolicy();
        policy.setExpiryDate(renewal.getNewExpiryDate());
        policy.setStatus(policyService.calculatePolicyStatus(policy));
        if (policy.getStatus() == PolicyStatus.EXPIRED) {
            policy.setStatus(PolicyStatus.ACTIVE);
        }
        policyRepository.save(policy);
        PolicyRenewal saved = renewalRepository.save(renewal);
        auditLogService.record(policy.getCustomer(), "COMPLETE", "PolicyRenewal",
                String.valueOf(saved.getId()), "Renewal completed for " + policy.getPolicyNumber(), null);
        log.info("Renewal {} completed for policy {}", renewalId, policy.getPolicyNumber());
        return renewalMapper.toResponse(saved);
    }

    /** Marks a renewal as cancelled (missed). */
    @Transactional
    public RenewalResponse markMissed(Long renewalId) {
        PolicyRenewal renewal = renewalRepository.findById(renewalId)
                .orElseThrow(() -> new ResourceNotFoundException("Renewal not found: " + renewalId));
        renewal.setStatus(RenewalStatus.CANCELLED);
        return renewalMapper.toResponse(renewalRepository.save(renewal));
    }

    /** Days from today until the given date (negative when overdue). */
    public long daysRemaining(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    private PolicyRenewal requireOwned(String email, Long renewalId) {
        PolicyRenewal renewal = renewalRepository.findById(renewalId)
                .orElseThrow(() -> new ResourceNotFoundException("Renewal not found: " + renewalId));
        if (isAdmin(email)) {
            return renewal;
        }
        CustomerProfile customer = requireProfile(email);
        Long userId = customer.getUser() != null ? customer.getUser().getId() : null;
        if (renewal.getPolicy() == null || renewal.getPolicy().getCustomer() == null
                || !renewal.getPolicy().getCustomer().getId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this renewal");
        }
        return renewal;
    }

    private CustomerProfile requireProfile(String email) {
        return profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for: " + email));
    }

    private boolean isAdmin(String email) {
        return userRepository.findByEmail(email)
                .map(u -> u.getRole() == Role.ROLE_ADMIN)
                .orElse(false);
    }
}
