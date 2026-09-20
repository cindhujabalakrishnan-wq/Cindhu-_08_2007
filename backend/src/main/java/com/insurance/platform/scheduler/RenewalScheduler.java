package com.insurance.platform.scheduler;

import com.insurance.platform.model.entity.InsurancePolicy;
import com.insurance.platform.model.entity.PolicyRenewal;
import com.insurance.platform.model.enums.NotificationType;
import com.insurance.platform.model.enums.PolicyStatus;
import com.insurance.platform.model.enums.RenewalStatus;
import com.insurance.platform.repository.InsurancePolicyRepository;
import com.insurance.platform.repository.PolicyRenewalRepository;
import com.insurance.platform.service.AuditLogService;
import com.insurance.platform.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Nightly renewal job: refreshes policy statuses, creates deduped expiry
 * notifications, marks expired policies and writes audit entries.
 */
@Component
@EnableScheduling
public class RenewalScheduler {

    private static final Logger log = LoggerFactory.getLogger(RenewalScheduler.class);

    private static final int REMINDER_WINDOW_DAYS = 30;

    private final InsurancePolicyRepository policyRepository;
    private final PolicyRenewalRepository renewalRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public RenewalScheduler(InsurancePolicyRepository policyRepository,
                            PolicyRenewalRepository renewalRepository,
                            NotificationService notificationService,
                            AuditLogService auditLogService) {
        this.policyRepository = policyRepository;
        this.renewalRepository = renewalRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    /** Runs on the configured cron (default 09:00 daily). */
    @Scheduled(cron = "${SCHEDULER_CRON:0 0 9 * * *}")
    @Transactional
    public void runRenewalSweep() {
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(REMINDER_WINDOW_DAYS);
        log.info("Renewal sweep started");

        List<InsurancePolicy> expiring = policyRepository.findByExpiryDateBetween(today, horizon);
        for (InsurancePolicy policy : expiring) {
            if (policy.getStatus() == PolicyStatus.CANCELLED) {
                continue;
            }
            ensurePendingRenewal(policy);
            if (policy.getCustomer() != null) {
                notificationService.notify(
                        policy.getCustomer(),
                        policy,
                        "Policy expiring soon: " + policy.getPolicyNumber(),
                        "Your policy " + policy.getPolicyNumber() + " expires on " + policy.getExpiryDate(),
                        NotificationType.RENEWAL);
            }
        }

        List<InsurancePolicy> all = policyRepository.findAll();
        for (InsurancePolicy policy : all) {
            if (policy.getStatus() != PolicyStatus.CANCELLED
                    && policy.getExpiryDate() != null
                    && policy.getExpiryDate().isBefore(today)
                    && policy.getStatus() != PolicyStatus.EXPIRED) {
                policy.setStatus(PolicyStatus.EXPIRED);
                policyRepository.save(policy);
                markStaleRenewalsMissed(policy);
                auditLogService.record(policy.getCustomer(), "EXPIRE", "InsurancePolicy",
                        String.valueOf(policy.getId()),
                        "Policy marked EXPIRED: " + policy.getPolicyNumber(), null);
                if (policy.getCustomer() != null) {
                    notificationService.notify(
                            policy.getCustomer(),
                            policy,
                            "Policy expired: " + policy.getPolicyNumber(),
                            "Your policy " + policy.getPolicyNumber() + " expired on " + policy.getExpiryDate(),
                            NotificationType.POLICY_EXPIRY);
                }
            }
        }
        log.info("Renewal sweep finished: {} expiring, {} total policies", expiring.size(), all.size());
    }

    private void ensurePendingRenewal(InsurancePolicy policy) {
        if (renewalRepository.existsByPolicyIdAndStatus(policy.getId(), RenewalStatus.PENDING)) {
            return;
        }
        PolicyRenewal renewal = new PolicyRenewal();
        renewal.setPolicy(policy);
        renewal.setPreviousExpiryDate(policy.getExpiryDate());
        renewal.setRequestedAt(java.time.LocalDateTime.now());
        renewal.setNewExpiryDate(policy.getExpiryDate());
        renewal.setRenewalPremium(policy.getPremiumAmount());
        renewal.setStatus(RenewalStatus.PENDING);
        renewal.setRemarks("Auto-created by RenewalScheduler");
        renewalRepository.save(renewal);
        auditLogService.record(policy.getCustomer(),
                "CREATE", "PolicyRenewal", String.valueOf(renewal.getId()),
                "Auto renewal created for " + policy.getPolicyNumber(), null);
    }

    private void markStaleRenewalsMissed(InsurancePolicy policy) {
        List<PolicyRenewal> pending = List.of();
        renewalRepository.findFirstByPolicyIdAndStatus(policy.getId(), RenewalStatus.PENDING)
                .ifPresent(r -> {
                    r.setStatus(RenewalStatus.CANCELLED);
                    renewalRepository.save(r);
                });
    }
}
