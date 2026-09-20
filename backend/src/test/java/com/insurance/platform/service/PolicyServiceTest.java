package com.insurance.platform.service;

import com.insurance.platform.dto.policy.DashboardSummaryResponse;
import com.insurance.platform.dto.policy.PolicyRequest;
import com.insurance.platform.dto.policy.PolicyResponse;
import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.exception.ForbiddenException;
import com.insurance.platform.mapper.PolicyMapper;
import com.insurance.platform.model.entity.InsuranceCompany;
import com.insurance.platform.model.entity.InsurancePolicy;
import com.insurance.platform.model.entity.PolicyType;
import com.insurance.platform.model.entity.PremiumPayment;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.PaymentStatus;
import com.insurance.platform.model.enums.PolicyStatus;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.CustomerProfileRepository;
import com.insurance.platform.repository.InsuranceCompanyRepository;
import com.insurance.platform.repository.InsurancePolicyRepository;
import com.insurance.platform.repository.PolicyTypeRepository;
import com.insurance.platform.repository.PremiumPaymentRepository;
import com.insurance.platform.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private InsurancePolicyRepository policyRepository;
    @Mock
    private CustomerProfileRepository profileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InsuranceCompanyRepository companyRepository;
    @Mock
    private PolicyTypeRepository policyTypeRepository;
    @Mock
    private PremiumPaymentRepository paymentRepository;
    @Mock
    private AuditLogService auditLogService;

    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        policyService = new PolicyService(policyRepository, profileRepository, userRepository,
                companyRepository, policyTypeRepository, paymentRepository,
                new PolicyMapper(), auditLogService);
    }

    private User customer(long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setRole(Role.ROLE_CUSTOMER);
        user.setEnabled(true);
        return user;
    }

    private InsurancePolicy policy(long id, User owner, LocalDate start, LocalDate expiry,
                                   PolicyStatus status) {
        InsurancePolicy policy = new InsurancePolicy();
        policy.setId(id);
        policy.setPolicyNumber("POL-" + id);
        policy.setCustomer(owner);
        policy.setPolicyName("Test Policy");
        policy.setStartDate(start);
        policy.setExpiryDate(expiry);
        policy.setPremiumAmount(BigDecimal.valueOf(1000));
        policy.setStatus(status);
        return policy;
    }

    private PolicyRequest validRequest() {
        PolicyRequest request = new PolicyRequest();
        request.setPolicyName("Health Cover");
        request.setInsuranceCompanyId(1L);
        request.setPolicyTypeId(2L);
        request.setStartDate(LocalDate.now().minusDays(10));
        request.setExpiryDate(LocalDate.now().plusDays(355));
        request.setPremiumAmount(BigDecimal.valueOf(5000));
        request.setPremiumFrequency("YEARLY");
        request.setCoverageAmount(BigDecimal.valueOf(500000));
        return request;
    }

    // ---- status calculation (ACTIVE / EXPIRING_SOON / EXPIRED / CANCELLED+RENEWED-sticky) ----

    @Test
    void calculatePolicyStatus_activeForInForcePolicy() {
        InsurancePolicy policy = policy(null, LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(60), PolicyStatus.ACTIVE);
        assertEquals(PolicyStatus.ACTIVE, policyService.calculatePolicyStatus(policy));
    }

    private InsurancePolicy policy(Void unused, LocalDate start, LocalDate expiry, PolicyStatus status) {
        InsurancePolicy policy = new InsurancePolicy();
        policy.setStartDate(start);
        policy.setExpiryDate(expiry);
        policy.setStatus(status);
        return policy;
    }

    @Test
    void calculatePolicyStatus_expiredForPastExpiry() {
        InsurancePolicy policy = policy(null, LocalDate.now().minusDays(400),
                LocalDate.now().minusDays(5), PolicyStatus.ACTIVE);
        assertEquals(PolicyStatus.EXPIRED, policyService.calculatePolicyStatus(policy));
    }

    @Test
    void calculatePolicyStatus_expiringSoonWithin30Days() {
        InsurancePolicy policy = policy(null, LocalDate.now().minusDays(335),
                LocalDate.now().plusDays(15), PolicyStatus.ACTIVE);
        assertEquals(PolicyStatus.EXPIRING_SOON, policyService.calculatePolicyStatus(policy));
    }

    @Test
    void calculatePolicyStatus_activeForFutureStart() {
        InsurancePolicy policy = policy(null, LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(375), PolicyStatus.ACTIVE);
        assertEquals(PolicyStatus.ACTIVE, policyService.calculatePolicyStatus(policy));
    }

    @Test
    void calculatePolicyStatus_renewedIsSticky() {
        InsurancePolicy policy = policy(null, LocalDate.now().minusDays(400),
                LocalDate.now().minusDays(5), PolicyStatus.RENEWED);
        assertEquals(PolicyStatus.RENEWED, policyService.calculatePolicyStatus(policy));
    }

    @Test
    void calculatePolicyStatus_cancelledIsSticky() {
        InsurancePolicy policy = policy(null, LocalDate.now().minusDays(400),
                LocalDate.now().minusDays(5), PolicyStatus.CANCELLED);
        assertEquals(PolicyStatus.CANCELLED, policyService.calculatePolicyStatus(policy));
    }

    // ---- creation validation ----

    @Test
    void create_rejectsExpiryBeforeStart() {
        PolicyRequest request = validRequest();
        request.setStartDate(LocalDate.of(2026, 6, 1));
        request.setExpiryDate(LocalDate.of(2026, 5, 1));

        assertThrows(BadRequestException.class,
                () -> policyService.create("jane@example.com", request));
        verify(policyRepository, never()).save(any());
    }

    @Test
    void create_rejectsExpiryEqualToStart() {
        PolicyRequest request = validRequest();
        request.setStartDate(LocalDate.of(2026, 6, 1));
        request.setExpiryDate(LocalDate.of(2026, 6, 1));

        assertThrows(BadRequestException.class,
                () -> policyService.create("jane@example.com", request));
    }

    @Test
    void create_success_persistsAndMaps() {
        User owner = customer(1L, "jane@example.com");
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(owner));
        InsuranceCompany company = new InsuranceCompany();
        company.setId(1L);
        company.setName("Acme Insurance");
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        PolicyType type = new PolicyType();
        type.setId(2L);
        type.setName("Health Plus");
        when(policyTypeRepository.findById(2L)).thenReturn(Optional.of(type));
        when(policyRepository.save(any(InsurancePolicy.class))).thenAnswer(inv -> {
            InsurancePolicy p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        PolicyResponse response = policyService.create("jane@example.com", validRequest());

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertNotNull(response.getPolicyNumber());
        assertTrue(response.getPolicyNumber().startsWith("POL-"));
        assertEquals("jane@example.com", response.getCustomerEmail());
        assertEquals("Acme Insurance", response.getInsuranceCompanyName());
        verify(auditLogService).record(eq(owner), eq("CREATE"), eq("InsurancePolicy"),
                eq("10"), any(), any());
    }

    // ---- ownership ----

    @Test
    void getById_forbiddenForNonOwner() {
        User owner = customer(1L, "owner@example.com");
        User intruder = customer(2L, "intruder@example.com");
        InsurancePolicy policy = policy(5L, owner, LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(60), PolicyStatus.ACTIVE);
        when(policyRepository.findById(5L)).thenReturn(Optional.of(policy));
        when(userRepository.findByEmail("intruder@example.com"))
                .thenReturn(Optional.of(intruder));

        assertThrows(ForbiddenException.class,
                () -> policyService.getById("intruder@example.com", 5L));
    }

    @Test
    void getById_adminBypassesOwnership() {
        User owner = customer(1L, "owner@example.com");
        User admin = customer(9L, "admin@example.com");
        admin.setRole(Role.ROLE_ADMIN);
        InsurancePolicy policy = policy(5L, owner, LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(60), PolicyStatus.ACTIVE);
        when(policyRepository.findById(5L)).thenReturn(Optional.of(policy));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        PolicyResponse response = policyService.getById("admin@example.com", 5L);
        assertNotNull(response);
        assertEquals(5L, response.getId());
    }

    // ---- delete rules ----

    @Test
    void delete_blockedWhenActiveWithCompletedPayments() {
        User owner = customer(1L, "jane@example.com");
        InsurancePolicy policy = policy(5L, owner, LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(60), PolicyStatus.ACTIVE);
        when(policyRepository.findById(5L)).thenReturn(Optional.of(policy));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(owner));
        when(paymentRepository.existsByPolicyIdAndStatus(5L, PaymentStatus.COMPLETED))
                .thenReturn(true);

        assertThrows(ForbiddenException.class,
                () -> policyService.delete("jane@example.com", 5L));
        verify(policyRepository, never()).delete(any());
    }

    @Test
    void delete_allowedWhenActiveWithoutCompletedPayments() {
        User owner = customer(1L, "jane@example.com");
        InsurancePolicy policy = policy(5L, owner, LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(60), PolicyStatus.ACTIVE);
        when(policyRepository.findById(5L)).thenReturn(Optional.of(policy));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(owner));
        when(paymentRepository.existsByPolicyIdAndStatus(5L, PaymentStatus.COMPLETED))
                .thenReturn(false);

        policyService.delete("jane@example.com", 5L);
        verify(policyRepository).delete(policy);
    }

    @Test
    void delete_allowedWhenExpiredEvenWithPayments() {
        User owner = customer(1L, "jane@example.com");
        InsurancePolicy policy = policy(5L, owner, LocalDate.now().minusDays(400),
                LocalDate.now().minusDays(5), PolicyStatus.EXPIRED);
        when(policyRepository.findById(5L)).thenReturn(Optional.of(policy));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(owner));
        when(paymentRepository.existsByPolicyIdAndStatus(5L, PaymentStatus.COMPLETED))
                .thenReturn(true);

        policyService.delete("jane@example.com", 5L);
        verify(policyRepository).delete(policy);
    }

    // ---- dashboard ----

    @Test
    void dashboardSummary_countsAndTotals() {
        User owner = customer(1L, "jane@example.com");
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(owner));
        InsurancePolicy active = policy(1L, owner, LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(60), PolicyStatus.ACTIVE);
        InsurancePolicy expiringSoon = policy(2L, owner, LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(10), PolicyStatus.ACTIVE);
        InsurancePolicy expired = policy(3L, owner, LocalDate.now().minusDays(400),
                LocalDate.now().minusDays(5), PolicyStatus.EXPIRED);
        when(policyRepository.findByCustomerId(1L))
                .thenReturn(List.of(active, expiringSoon, expired));

        PremiumPayment paid = new PremiumPayment();
        paid.setAmount(BigDecimal.valueOf(100));
        paid.setStatus(PaymentStatus.COMPLETED);
        PremiumPayment pending = new PremiumPayment();
        pending.setAmount(BigDecimal.valueOf(50));
        pending.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByPolicyIdAndStatus(anyLong(), eq(PaymentStatus.COMPLETED)))
                .thenReturn(List.of(paid));
        when(paymentRepository.findByPolicyIdAndStatus(anyLong(), eq(PaymentStatus.PENDING)))
                .thenReturn(List.of(pending));

        DashboardSummaryResponse summary = policyService.dashboardSummary("jane@example.com");

        assertEquals(3, summary.getTotalPolicies());
        assertEquals(1, summary.getActivePolicies());
        assertEquals(1, summary.getExpiredPolicies());
        assertEquals(1, summary.getExpiringSoon());
        assertEquals(0, BigDecimal.valueOf(300).compareTo(summary.getTotalPremiumPaid()));
        assertEquals(0, BigDecimal.valueOf(150).compareTo(summary.getTotalPendingAmount()));
    }
}
