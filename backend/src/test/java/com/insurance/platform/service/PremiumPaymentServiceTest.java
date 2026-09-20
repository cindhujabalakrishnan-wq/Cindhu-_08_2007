package com.insurance.platform.service;

import com.insurance.platform.dto.payment.PaymentRequest;
import com.insurance.platform.dto.payment.PaymentResponse;
import com.insurance.platform.dto.payment.PaymentSummaryResponse;
import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.exception.ForbiddenException;
import com.insurance.platform.model.entity.InsurancePolicy;
import com.insurance.platform.model.entity.PremiumPayment;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.PaymentStatus;
import com.insurance.platform.model.enums.PolicyStatus;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.PremiumPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PremiumPaymentServiceTest {

    @Mock
    private PremiumPaymentRepository paymentRepository;
    @Mock
    private PolicyService policyService;
    @Mock
    private AuditLogService auditLogService;

    private PremiumPaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PremiumPaymentService(paymentRepository, policyService, auditLogService);
    }

    private InsurancePolicy policy(long id, User owner) {
        InsurancePolicy policy = new InsurancePolicy();
        policy.setId(id);
        policy.setPolicyNumber("POL-" + id);
        policy.setCustomer(owner);
        policy.setPolicyName("Health Cover");
        policy.setStartDate(LocalDate.now().minusDays(10));
        policy.setExpiryDate(LocalDate.now().plusDays(355));
        policy.setStatus(PolicyStatus.ACTIVE);
        return policy;
    }

    private User customer() {
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
        user.setRole(Role.ROLE_CUSTOMER);
        return user;
    }

    private PaymentRequest validRequest(BigDecimal amount) {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(amount);
        request.setPaymentDate(LocalDate.now());
        request.setDueDate(LocalDate.now().plusDays(5));
        request.setPaymentMethod("UPI");
        request.setTransactionReference("TXN-1");
        request.setStatus("COMPLETED");
        return request;
    }

    @Test
    void create_negativeAmount_rejected() {
        PaymentRequest request = validRequest(BigDecimal.valueOf(-10));

        assertThrows(BadRequestException.class,
                () -> paymentService.create("jane@example.com", 5L, request));
        verifyNoInteractions(policyService);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void create_nullAmount_rejected() {
        PaymentRequest request = validRequest(null);

        assertThrows(BadRequestException.class,
                () -> paymentService.create("jane@example.com", 5L, request));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void create_zeroAmount_allowedAsBoundary() {
        InsurancePolicy policy = policy(5L, customer());
        when(policyService.requireOwned("jane@example.com", 5L)).thenReturn(policy);
        when(paymentRepository.save(any(PremiumPayment.class)))
                .thenAnswer(inv -> {
                    PremiumPayment p = inv.getArgument(0);
                    p.setId(21L);
                    return p;
                });

        PaymentResponse response =
                paymentService.create("jane@example.com", 5L, validRequest(BigDecimal.ZERO));

        assertEquals(21L, response.getId());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getAmount()));
    }

    @Test
    void create_success_recordsPayment() {
        InsurancePolicy policy = policy(5L, customer());
        when(policyService.requireOwned("jane@example.com", 5L)).thenReturn(policy);
        when(paymentRepository.save(any(PremiumPayment.class)))
                .thenAnswer(inv -> {
                    PremiumPayment p = inv.getArgument(0);
                    p.setId(21L);
                    return p;
                });

        PaymentResponse response = paymentService.create("jane@example.com", 5L,
                validRequest(BigDecimal.valueOf(2500)));

        assertEquals(21L, response.getId());
        assertEquals(5L, response.getPolicyId());
        assertEquals("POL-5", response.getPolicyNumber());
        assertEquals(0, BigDecimal.valueOf(2500).compareTo(response.getAmount()));
        assertEquals("COMPLETED", response.getStatus());
        assertEquals("UPI", response.getPaymentMethod());
        verify(auditLogService).record(any(), eq("CREATE"), eq("PremiumPayment"),
                eq("21"), any(), any());
    }

    @Test
    void create_invalidMethod_rejected() {
        when(policyService.requireOwned("jane@example.com", 5L))
                .thenReturn(policy(5L, customer()));
        PaymentRequest request = validRequest(BigDecimal.valueOf(100));
        request.setPaymentMethod("BITCOIN");

        assertThrows(BadRequestException.class,
                () -> paymentService.create("jane@example.com", 5L, request));
    }

    @Test
    void create_invalidStatus_rejected() {
        when(policyService.requireOwned("jane@example.com", 5L))
                .thenReturn(policy(5L, customer()));
        PaymentRequest request = validRequest(BigDecimal.valueOf(100));
        request.setStatus("SOMETHING_ELSE");

        assertThrows(BadRequestException.class,
                () -> paymentService.create("jane@example.com", 5L, request));
    }

    @Test
    void create_ownershipViolation_propagates() {
        PaymentRequest request = validRequest(BigDecimal.valueOf(100));
        when(policyService.requireOwned("intruder@example.com", 5L))
                .thenThrow(new ForbiddenException("You do not have access to this policy"));

        assertThrows(ForbiddenException.class,
                () -> paymentService.create("intruder@example.com", 5L, request));
    }

    @Test
    void list_returnsPolicyPayments() {
        InsurancePolicy policy = policy(5L, customer());
        when(policyService.requireOwned("jane@example.com", 5L)).thenReturn(policy);
        PremiumPayment payment = new PremiumPayment();
        payment.setId(21L);
        payment.setPolicy(policy);
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findByPolicyId(5L)).thenReturn(List.of(payment));

        List<PaymentResponse> result = paymentService.list("jane@example.com", 5L);

        assertEquals(1, result.size());
        assertEquals(21L, result.get(0).getId());
    }

    @Test
    void summary_aggregatesPaidPendingAndOverdue() {
        InsurancePolicy policy = policy(5L, customer());
        when(policyService.requireOwned("jane@example.com", 5L)).thenReturn(policy);
        PremiumPayment paid1 = payment(BigDecimal.valueOf(100), PaymentStatus.COMPLETED, null);
        PremiumPayment paid2 = payment(BigDecimal.valueOf(200), PaymentStatus.COMPLETED, null);
        PremiumPayment pendingFuture = payment(BigDecimal.valueOf(50), PaymentStatus.PENDING,
                LocalDate.now().plusDays(5));
        PremiumPayment pendingOverdue = payment(BigDecimal.valueOf(30), PaymentStatus.PENDING,
                LocalDate.now().minusDays(2));
        when(paymentRepository.findByPolicyId(anyLong()))
                .thenReturn(List.of(paid1, paid2, pendingFuture, pendingOverdue));

        PaymentSummaryResponse summary = paymentService.summary("jane@example.com", 5L);

        assertEquals(5L, summary.getPolicyId());
        assertEquals(0, BigDecimal.valueOf(300).compareTo(summary.getTotalPaid()));
        assertEquals(0, BigDecimal.valueOf(80).compareTo(summary.getTotalPending()));
        assertEquals(0, BigDecimal.valueOf(30).compareTo(summary.getTotalOverdue()));
        assertEquals(2, summary.getPaidCount());
        assertEquals(2, summary.getPendingCount());
    }

    private PremiumPayment payment(BigDecimal amount, PaymentStatus status, LocalDate dueDate) {
        PremiumPayment payment = new PremiumPayment();
        payment.setAmount(amount);
        payment.setStatus(status);
        payment.setDueDate(dueDate);
        return payment;
    }

    @Test
    void get_success_returnsPayment() {
        InsurancePolicy policy = policy(5L, customer());
        PremiumPayment payment = new PremiumPayment();
        payment.setId(21L);
        payment.setPolicy(policy);
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById(21L)).thenReturn(Optional.of(payment));
        when(policyService.requireOwned("jane@example.com", 5L)).thenReturn(policy);

        assertEquals(21L, paymentService.get("jane@example.com", 21L).getId());
    }
}
