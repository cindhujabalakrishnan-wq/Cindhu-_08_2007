package com.insurance.platform.service;

import com.insurance.platform.dto.admin.AuditLogResponse;
import com.insurance.platform.dto.admin.DashboardStatsResponse;
import com.insurance.platform.dto.policy.PolicyResponse;
import com.insurance.platform.dto.policy.PolicySearchParams;
import com.insurance.platform.dto.renewal.RenewalResponse;
import com.insurance.platform.dto.user.UserResponse;
import com.insurance.platform.mapper.PolicyMapper;
import com.insurance.platform.mapper.RenewalMapper;
import com.insurance.platform.mapper.UserMapper;
import com.insurance.platform.model.enums.PaymentStatus;
import com.insurance.platform.model.enums.PolicyStatus;
import com.insurance.platform.model.enums.RenewalStatus;
import com.insurance.platform.repository.CustomerProfileRepository;
import com.insurance.platform.repository.InsurancePolicyRepository;
import com.insurance.platform.repository.NotificationRepository;
import com.insurance.platform.repository.PolicyRenewalRepository;
import com.insurance.platform.repository.PremiumPaymentRepository;
import com.insurance.platform.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin read-models: platform dashboard stats and paged entity lists.
 */
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final CustomerProfileRepository profileRepository;
    private final InsurancePolicyRepository policyRepository;
    private final PolicyRenewalRepository renewalRepository;
    private final PremiumPaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;
    private final PolicyMapper policyMapper;
    private final RenewalMapper renewalMapper;

    public AdminService(UserRepository userRepository,
                        CustomerProfileRepository profileRepository,
                        InsurancePolicyRepository policyRepository,
                        PolicyRenewalRepository renewalRepository,
                        PremiumPaymentRepository paymentRepository,
                        NotificationRepository notificationRepository,
                        AuditLogService auditLogService,
                        UserMapper userMapper,
                        PolicyMapper policyMapper,
                        RenewalMapper renewalMapper) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.policyRepository = policyRepository;
        this.renewalRepository = renewalRepository;
        this.paymentRepository = paymentRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogService = auditLogService;
        this.userMapper = userMapper;
        this.policyMapper = policyMapper;
        this.renewalMapper = renewalMapper;
    }

    /** Aggregates platform-wide counts for the admin dashboard. */
    @Transactional(readOnly = true)
    public DashboardStatsResponse dashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalCustomers(profileRepository.count());
        stats.setTotalPolicies(policyRepository.count());
        stats.setActivePolicies(policyRepository.countByStatus(PolicyStatus.ACTIVE));
        stats.setExpiringSoon(policyRepository.countByStatus(PolicyStatus.EXPIRING_SOON));
        stats.setExpiredPolicies(policyRepository.countByStatus(PolicyStatus.EXPIRED));
        java.math.BigDecimal totalPremium = policyRepository.findAll().stream()
                .map(p -> p.getPremiumAmount() != null
                        ? p.getPremiumAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        stats.setTotalPremium(totalPremium);
        stats.setPendingRenewals(renewalRepository.countByStatus(RenewalStatus.PENDING));
        stats.setOverduePayments(paymentRepository.countByStatus(PaymentStatus.OVERDUE));
        stats.setUnreadNotifications(notificationRepository.countByReadFalse());

        Pageable recent = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        stats.setRecentPolicies(policyRepository.findAll(recent)
                .map(policyMapper::toResponse).getContent());
        stats.setRecentUsers(userRepository.findAll(recent)
                .map(userMapper::toResponse).getContent());

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate horizon = today.plusDays(30);
        stats.setUpcomingRenewals(policyRepository.findByExpiryDateBetween(today, horizon)
                .stream().map(policy -> {
                    RenewalResponse dto = new RenewalResponse();
                    dto.setPolicyId(policy.getId());
                    dto.setPolicyNumber(policy.getPolicyNumber());
                    dto.setPolicyName(policy.getPolicyName());
                    dto.setExpiryDate(policy.getExpiryDate());
                    dto.setPremiumAmount(policy.getPremiumAmount());
                    if (policy.getCustomer() != null) {
                        dto.setHolderName(policy.getCustomer().getFirstName()
                                + " " + policy.getCustomer().getLastName());
                    }
                    if (policy.getInsuranceCompany() != null) {
                        dto.setCompanyName(policy.getInsuranceCompany().getName());
                    }
                    dto.setPreviousExpiryDate(policy.getExpiryDate());
                    dto.setStatus(RenewalStatus.PENDING.name());
                    dto.setDaysRemaining(java.time.temporal.ChronoUnit.DAYS
                            .between(today, policy.getExpiryDate()));
                    return dto;
                }).toList());
        return stats;
    }

    /** Paged users, optionally filtered by a name/email search. */
    @Transactional(readOnly = true)
    public Page<UserResponse> users(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            String q = search.trim();
            return userRepository
                    .findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                            q, q, q, pageable)
                    .map(userMapper::toResponse);
        }
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    /** Paged policies with optional filters. */
    @Transactional(readOnly = true)
    public Page<PolicyResponse> policies(PolicySearchParams params, Pageable pageable) {
        PolicyStatus status = null;
        com.insurance.platform.model.enums.PolicyCategory category = null;
        java.time.LocalDate expiryBefore = null;
        try {
            if (params.getStatus() != null && !params.getStatus().isBlank()) {
                status = PolicyStatus.valueOf(params.getStatus().trim().toUpperCase());
            }
            if (params.getCategory() != null && !params.getCategory().isBlank()) {
                category = com.insurance.platform.model.enums.PolicyCategory
                        .valueOf(params.getCategory().trim().toUpperCase());
            }
        } catch (IllegalArgumentException ex) {
            throw new com.insurance.platform.exception.BadRequestException("Invalid filter value");
        }
        if (params.getExpiringWithinDays() != null) {
            expiryBefore = java.time.LocalDate.now().plusDays(params.getExpiringWithinDays());
        }
        String search = params.getSearch() != null && !params.getSearch().isBlank()
                ? params.getSearch().trim() : null;
        return policyRepository.searchAllFiltered(status, category, expiryBefore, search, pageable)
                .map(policyMapper::toResponse);
    }

    /** Paged renewals, optionally filtered by status. */
    @Transactional(readOnly = true)
    public Page<RenewalResponse> renewals(String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            try {
                RenewalStatus parsed = RenewalStatus.valueOf(status.trim().toUpperCase());
                return renewalRepository.findByStatus(parsed, pageable).map(renewalMapper::toResponse);
            } catch (IllegalArgumentException ex) {
                throw new com.insurance.platform.exception.BadRequestException(
                        "Invalid renewal status: " + status);
            }
        }
        return renewalRepository.findAll(pageable).map(renewalMapper::toResponse);
    }

    /** Paged audit logs. */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> auditLogs(Pageable pageable) {
        return auditLogService.list(pageable);
    }
}
