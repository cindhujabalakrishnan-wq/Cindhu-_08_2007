package com.insurance.platform.service;

import com.insurance.platform.dto.policy.DashboardSummaryResponse;
import com.insurance.platform.dto.policy.PolicyRequest;
import com.insurance.platform.dto.policy.PolicyResponse;
import com.insurance.platform.dto.policy.PolicySearchParams;
import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.exception.ForbiddenException;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.mapper.PolicyMapper;
import com.insurance.platform.model.entity.InsuranceCompany;
import com.insurance.platform.model.entity.InsurancePolicy;
import com.insurance.platform.model.entity.PolicyType;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.PaymentStatus;
import com.insurance.platform.model.enums.PolicyCategory;
import com.insurance.platform.model.enums.PolicyStatus;
import com.insurance.platform.model.enums.PremiumFrequency;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.CustomerProfileRepository;
import com.insurance.platform.repository.InsuranceCompanyRepository;
import com.insurance.platform.repository.InsurancePolicyRepository;
import com.insurance.platform.repository.PolicyTypeRepository;
import com.insurance.platform.repository.PremiumPaymentRepository;
import com.insurance.platform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Policy lifecycle: creation, ownership-guarded reads/updates, search, dashboard summary and guarded deletes.
 */
@Service
public class PolicyService {

    private static final Logger log = LoggerFactory.getLogger(PolicyService.class);

    private final InsurancePolicyRepository policyRepository;
    private final CustomerProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final InsuranceCompanyRepository companyRepository;
    private final PolicyTypeRepository policyTypeRepository;
    private final PremiumPaymentRepository paymentRepository;
    private final PolicyMapper policyMapper;
    private final AuditLogService auditLogService;

    public PolicyService(InsurancePolicyRepository policyRepository,
                         CustomerProfileRepository profileRepository,
                         UserRepository userRepository,
                         InsuranceCompanyRepository companyRepository,
                         PolicyTypeRepository policyTypeRepository,
                         PremiumPaymentRepository paymentRepository,
                         PolicyMapper policyMapper,
                         AuditLogService auditLogService) {
        this.policyRepository = policyRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.policyTypeRepository = policyTypeRepository;
        this.paymentRepository = paymentRepository;
        this.policyMapper = policyMapper;
        this.auditLogService = auditLogService;
    }

    /**
     * Derives the effective status from dates: CANCELLED/RENEWED are sticky, past expiry
     * means EXPIRED, expiry within 30 days means EXPIRING_SOON, otherwise ACTIVE.
     */
    public PolicyStatus calculatePolicyStatus(InsurancePolicy policy) {
        if (policy.getStatus() == PolicyStatus.CANCELLED) {
            return PolicyStatus.CANCELLED;
        }
        if (policy.getStatus() == PolicyStatus.RENEWED) {
            return PolicyStatus.RENEWED;
        }
        LocalDate today = LocalDate.now();
        if (policy.getExpiryDate() != null && policy.getExpiryDate().isBefore(today)) {
            return PolicyStatus.EXPIRED;
        }
        if (policy.getExpiryDate() != null && !policy.getExpiryDate().isAfter(today.plusDays(30))) {
            return PolicyStatus.EXPIRING_SOON;
        }
        return PolicyStatus.ACTIVE;
    }

    /** Creates a policy for the current user. */
    @Transactional
    public PolicyResponse create(String email, PolicyRequest request) {
        assertExpiryAfterStart(request.getStartDate(), request.getExpiryDate());
        User customer = requireUser(email);
        InsuranceCompany company = companyRepository.findById(request.getInsuranceCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Insurance company not found"));
        PolicyType type = policyTypeRepository.findById(request.getPolicyTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy type not found"));

        InsurancePolicy policy = new InsurancePolicy();
        policy.setPolicyNumber(generatePolicyNumber());
        policy.setCustomer(customer);
        policy.setInsuranceCompany(company);
        policy.setPolicyType(type);
        policy.setPolicyName(request.getPolicyName());
        policy.setStartDate(request.getStartDate());
        policy.setExpiryDate(request.getExpiryDate());
        policy.setPremiumAmount(request.getPremiumAmount());
        policy.setPremiumFrequency(parseFrequency(request.getPremiumFrequency()));
        policy.setCoverageAmount(request.getCoverageAmount());
        policy.setNomineeName(request.getNomineeName());
        policy.setNomineeContact(request.getNomineeContact());
        policy.setNotes(request.getNotes());
        policy.setStatus(calculatePolicyStatus(policy));

        InsurancePolicy saved = policyRepository.save(policy);
        auditLogService.record(customer, "CREATE", "InsurancePolicy",
                String.valueOf(saved.getId()), "Policy created: " + saved.getPolicyNumber(), null);
        log.info("Policy created {} for {}", saved.getPolicyNumber(), email);
        return policyMapper.toResponse(saved);
    }

    /** Ownership-guarded fetch of a single policy. */
    @Transactional(readOnly = true)
    public PolicyResponse getById(String email, Long policyId) {
        return policyMapper.toResponse(requireOwned(email, policyId));
    }

    /** Searches the caller's policies (admins may search across all customers). */
    @Transactional(readOnly = true)
    public Page<PolicyResponse> search(String email, PolicySearchParams params, Pageable pageable) {
        PolicyStatus status = parseStatus(params.getStatus());
        PolicyCategory category = parseCategory(params.getCategory());
        LocalDate expiryBefore = params.getExpiringWithinDays() != null
                ? LocalDate.now().plusDays(params.getExpiringWithinDays())
                : null;
        String search = params.getSearch() != null && !params.getSearch().isBlank()
                ? params.getSearch().trim() : null;

        if (isAdmin(email)) {
            return policyRepository.searchAllFiltered(status, category, expiryBefore, search, pageable)
                    .map(policyMapper::toResponse);
        }
        User customer = requireUser(email);
        return policyRepository.searchByCustomerFiltered(customer.getId(), status, category, expiryBefore, search, pageable)
                .map(policyMapper::toResponse);
    }

    /** Updates an owned policy. */
    @Transactional
    public PolicyResponse update(String email, Long policyId, PolicyRequest request) {
        assertExpiryAfterStart(request.getStartDate(), request.getExpiryDate());
        InsurancePolicy policy = requireOwned(email, policyId);
        InsuranceCompany company = companyRepository.findById(request.getInsuranceCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Insurance company not found"));
        PolicyType type = policyTypeRepository.findById(request.getPolicyTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy type not found"));
        policy.setInsuranceCompany(company);
        policy.setPolicyType(type);
        policy.setPolicyName(request.getPolicyName());
        policy.setStartDate(request.getStartDate());
        policy.setExpiryDate(request.getExpiryDate());
        policy.setPremiumAmount(request.getPremiumAmount());
        policy.setPremiumFrequency(parseFrequency(request.getPremiumFrequency()));
        policy.setCoverageAmount(request.getCoverageAmount());
        policy.setNomineeName(request.getNomineeName());
        policy.setNomineeContact(request.getNomineeContact());
        policy.setNotes(request.getNotes());
        policy.setStatus(calculatePolicyStatus(policy));
        InsurancePolicy saved = policyRepository.save(policy);
        auditLogService.record(policy.getCustomer(), "UPDATE", "InsurancePolicy",
                String.valueOf(saved.getId()), "Policy updated: " + saved.getPolicyNumber(), null);
        return policyMapper.toResponse(saved);
    }

    /**
     * Deletes a policy unless it is ACTIVE with COMPLETED payments (blocked to protect
     * financial history). CANCELLED/EXPIRED policies are eligible for deletion.
     */
    @Transactional
    public void delete(String email, Long policyId) {
        InsurancePolicy policy = requireOwned(email, policyId);
        boolean hasPaidPayments = paymentRepository.existsByPolicyIdAndStatus(policyId, PaymentStatus.COMPLETED);
        if (policy.getStatus() == PolicyStatus.ACTIVE && hasPaidPayments) {
            throw new ForbiddenException("Cannot delete an ACTIVE policy with COMPLETED payments");
        }
        policyRepository.delete(policy);
        auditLogService.record(policy.getCustomer(), "DELETE", "InsurancePolicy",
                String.valueOf(policyId), "Policy deleted: " + policy.getPolicyNumber(), null);
        log.info("Policy deleted {} by {}", policy.getPolicyNumber(), email);
    }

    /** Builds the per-customer dashboard summary. */
    @Transactional(readOnly = true)
    public DashboardSummaryResponse dashboardSummary(String email) {
        User customer = requireUser(email);
        List<InsurancePolicy> policies = policyRepository.findByCustomerId(customer.getId());
        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(30);

        DashboardSummaryResponse summary = new DashboardSummaryResponse();
        summary.setTotalPolicies(policies.size());
        summary.setActivePolicies(policies.stream()
                .filter(p -> calculatePolicyStatus(p) == PolicyStatus.ACTIVE).count());
        summary.setExpiredPolicies(policies.stream()
                .filter(p -> calculatePolicyStatus(p) == PolicyStatus.EXPIRED).count());
        long expiringSoonCount = policies.stream()
                .filter(p -> calculatePolicyStatus(p) == PolicyStatus.EXPIRING_SOON)
                .count();
        summary.setExpiringSoon(expiringSoonCount);
        summary.setUpcomingRenewals(expiringSoonCount);
        summary.setTotalPremium(policies.stream()
                .map(p -> p.getPremiumAmount() != null ? p.getPremiumAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        BigDecimal paid = BigDecimal.ZERO;
        BigDecimal pending = BigDecimal.ZERO;
        for (InsurancePolicy policy : policies) {
            paid = paid.add(paymentRepository.findByPolicyIdAndStatus(policy.getId(), PaymentStatus.COMPLETED)
                    .stream().map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            pending = pending.add(paymentRepository.findByPolicyIdAndStatus(policy.getId(), PaymentStatus.PENDING)
                    .stream().map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        summary.setTotalPremiumPaid(paid);
        summary.setTotalPendingAmount(pending);
        return summary;
    }

    /** Loads a policy entity after verifying ownership (admins bypass). */
    @Transactional(readOnly = true)
    public InsurancePolicy requireOwned(String email, Long policyId) {
        InsurancePolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found: " + policyId));
        if (isAdmin(email)) {
            return policy;
        }
        User customer = requireUser(email);
        if (policy.getCustomer() == null || !policy.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("You do not have access to this policy");
        }
        return policy;
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private boolean isAdmin(String email) {
        return userRepository.findByEmail(email)
                .map(User::getRole)
                .map(role -> role == Role.ROLE_ADMIN)
                .orElse(false);
    }

    private void assertExpiryAfterStart(LocalDate start, LocalDate expiry) {
        if (start != null && expiry != null && !expiry.isAfter(start)) {
            throw new BadRequestException("expiryDate must be after startDate");
        }
    }

    private PolicyStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return PolicyStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid policy status: " + raw);
        }
    }

    private PolicyCategory parseCategory(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return PolicyCategory.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid policy category: " + raw);
        }
    }

    private String generatePolicyNumber() {
        return "POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PremiumFrequency parseFrequency(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return PremiumFrequency.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid premium frequency: " + raw);
        }
    }
}
