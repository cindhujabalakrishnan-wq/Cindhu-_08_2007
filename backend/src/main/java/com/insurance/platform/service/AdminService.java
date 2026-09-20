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
import org.springframework.data.domain.Pageable;
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
        stats.setExpiredPolicies(policyRepository.countByStatus(PolicyStatus.EXPIRED));
        stats.setPendingRenewals(renewalRepository.countByStatus(RenewalStatus.PENDING));
        stats.setOverduePayments(paymentRepository.countByStatus(PaymentStatus.PENDING));
        stats.setUnreadNotifications(notificationRepository.countByReadFalse());
        return stats;
    }

    /** Paged users. */
    @Transactional(readOnly = true)
    public Page<UserResponse> users(Pageable pageable) {
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

    /** Paged renewals. */
    @Transactional(readOnly = true)
    public Page<RenewalResponse> renewals(Pageable pageable) {
        return renewalRepository.findAll(pageable).map(renewalMapper::toResponse);
    }

    /** Paged audit logs. */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> auditLogs(Pageable pageable) {
        return auditLogService.list(pageable);
    }
}
