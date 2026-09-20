package com.insurance.platform.service;

import com.insurance.platform.dto.payment.PaymentRequest;
import com.insurance.platform.dto.payment.PaymentResponse;
import com.insurance.platform.dto.payment.PaymentSummaryResponse;
import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.model.entity.InsurancePolicy;
import com.insurance.platform.model.entity.PremiumPayment;
import com.insurance.platform.model.enums.PaymentMethod;
import com.insurance.platform.model.enums.PaymentStatus;
import com.insurance.platform.repository.PremiumPaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Premium payment recording with ownership checks and totals.
 */
@Service
public class PremiumPaymentService {

    private static final Logger log = LoggerFactory.getLogger(PremiumPaymentService.class);

    private final PremiumPaymentRepository paymentRepository;
    private final PolicyService policyService;
    private final AuditLogService auditLogService;

    public PremiumPaymentService(PremiumPaymentRepository paymentRepository,
                                 PolicyService policyService,
                                 AuditLogService auditLogService) {
        this.paymentRepository = paymentRepository;
        this.policyService = policyService;
        this.auditLogService = auditLogService;
    }

    /** Records a payment against an owned policy. */
    @Transactional
    public PaymentResponse create(String email, Long policyId, PaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Amount must be >= 0");
        }
        InsurancePolicy policy = policyService.requireOwned(email, policyId);
        PremiumPayment payment = new PremiumPayment();
        payment.setPolicy(policy);
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setDueDate(request.getDueDate());
        payment.setPaymentMethod(parseMethod(request.getPaymentMethod()));
        payment.setTransactionReference(request.getTransactionReference());
        payment.setStatus(parseStatus(request.getStatus()));
        payment.setNotes(request.getNotes());
        PremiumPayment saved = paymentRepository.save(payment);
        auditLogService.record(policy.getCustomer(), "CREATE", "PremiumPayment",
                String.valueOf(saved.getId()), "Payment recorded for " + policy.getPolicyNumber(), null);
        log.info("Payment {} recorded for policy {}", saved.getId(), policy.getPolicyNumber());
        return toResponse(saved);
    }

    /** Lists payments for an owned policy. */
    @Transactional(readOnly = true)
    public List<PaymentResponse> list(String email, Long policyId) {
        InsurancePolicy policy = policyService.requireOwned(email, policyId);
        return paymentRepository.findByPolicyId(policy.getId()).stream().map(this::toResponse).toList();
    }

    /** Aggregates paid/pending/overdue totals for an owned policy. */
    @Transactional(readOnly = true)
    public PaymentSummaryResponse summary(String email, Long policyId) {
        InsurancePolicy policy = policyService.requireOwned(email, policyId);
        List<PremiumPayment> payments = paymentRepository.findByPolicyId(policy.getId());
        LocalDate today = LocalDate.now();
        PaymentSummaryResponse summary = new PaymentSummaryResponse();
        summary.setPolicyId(policy.getId());
        summary.setTotalPaid(total(payments, PaymentStatus.COMPLETED));
        summary.setTotalPending(total(payments, PaymentStatus.PENDING));
        summary.setTotalOverdue(payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING
                        && p.getDueDate() != null && p.getDueDate().isBefore(today))
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setPaidCount(payments.stream().filter(p -> p.getStatus() == PaymentStatus.COMPLETED).count());
        summary.setPendingCount(payments.stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).count());
        return summary;
    }

    /** Fetches a single payment after verifying policy ownership. */
    @Transactional(readOnly = true)
    public PaymentResponse get(String email, Long paymentId) {
        PremiumPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        policyService.requireOwned(email, payment.getPolicy().getId());
        return toResponse(payment);
    }

    private BigDecimal total(List<PremiumPayment> payments, PaymentStatus status) {
        return payments.stream()
                .filter(p -> p.getStatus() == status)
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PaymentResponse toResponse(PremiumPayment payment) {
        PaymentResponse dto = new PaymentResponse();
        dto.setId(payment.getId());
        if (payment.getPolicy() != null) {
            dto.setPolicyId(payment.getPolicy().getId());
            dto.setPolicyNumber(payment.getPolicy().getPolicyNumber());
        }
        dto.setAmount(payment.getAmount());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setDueDate(payment.getDueDate());
        dto.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
        dto.setTransactionReference(payment.getTransactionReference());
        dto.setStatus(payment.getStatus() != null ? payment.getStatus().name() : null);
        dto.setNotes(payment.getNotes());
        return dto;
    }

    private PaymentMethod parseMethod(String raw) {
        try {
            return PaymentMethod.valueOf(raw.trim().toUpperCase());
        } catch (Exception ex) {
            throw new BadRequestException("Invalid payment method: " + raw);
        }
    }

    private PaymentStatus parseStatus(String raw) {
        try {
            return PaymentStatus.valueOf(raw.trim().toUpperCase());
        } catch (Exception ex) {
            throw new BadRequestException("Invalid payment status: " + raw);
        }
    }
}
