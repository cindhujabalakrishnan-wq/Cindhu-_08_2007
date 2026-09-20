package com.insurance.platform.service;

import com.insurance.platform.dto.renewal.RenewalRequest;
import com.insurance.platform.dto.renewal.RenewalResponse;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.mapper.RenewalMapper;
import com.insurance.platform.model.entity.CustomerProfile;
import com.insurance.platform.model.entity.InsurancePolicy;
import com.insurance.platform.model.entity.PolicyRenewal;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.PolicyStatus;
import com.insurance.platform.model.enums.RenewalStatus;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.CustomerProfileRepository;
import com.insurance.platform.repository.InsurancePolicyRepository;
import com.insurance.platform.repository.PolicyRenewalRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RenewalServiceTest {

    @Mock
    private PolicyRenewalRepository renewalRepository;
    @Mock
    private InsurancePolicyRepository policyRepository;
    @Mock
    private CustomerProfileRepository profileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PolicyService policyService;
    @Mock
    private AuditLogService auditLogService;

    private RenewalService renewalService;

    @BeforeEach
    void setUp() {
        renewalService = new RenewalService(renewalRepository, policyRepository,
                profileRepository, userRepository, policyService,
                new RenewalMapper(), auditLogService);
    }

    private User customer(long id, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setRole(role);
        user.setEnabled(true);
        return user;
    }

    private InsurancePolicy policy(long id, User owner, LocalDate expiry) {
        InsurancePolicy policy = new InsurancePolicy();
        policy.setId(id);
        policy.setPolicyNumber("POL-" + id);
        policy.setCustomer(owner);
        policy.setPolicyName("Health Cover");
        policy.setStartDate(expiry.minusDays(355));
        policy.setExpiryDate(expiry);
        policy.setStatus(PolicyStatus.ACTIVE);
        return policy;
    }

    private RenewalRequest renewalRequest(LocalDate newExpiry) {
        RenewalRequest request = new RenewalRequest();
        request.setRenewalDate(LocalDate.now());
        request.setNewExpiryDate(newExpiry);
        request.setRenewalPremium(BigDecimal.valueOf(5500));
        request.setNotes("Annual renewal");
        return request;
    }

    @Test
    void create_newRenewal_savesAndReturnsPending() {
        User owner = customer(1L, "jane@example.com", Role.ROLE_CUSTOMER);
        InsurancePolicy policy = policy(5L, owner, LocalDate.now().plusDays(20));
        when(policyService.requireOwned("jane@example.com", 5L)).thenReturn(policy);
        when(renewalRepository.findFirstByPolicyIdAndStatus(5L, RenewalStatus.PENDING))
                .thenReturn(Optional.empty());
        when(renewalRepository.save(any(PolicyRenewal.class))).thenAnswer(inv -> {
            PolicyRenewal r = inv.getArgument(0);
            r.setId(11L);
            return r;
        });

        RenewalResponse response = renewalService.create("jane@example.com", 5L,
                renewalRequest(LocalDate.now().plusDays(385)));

        assertNotNull(response);
        assertEquals(11L, response.getId());
        assertEquals(RenewalStatus.PENDING.name(), response.getStatus());
        assertEquals(5L, response.getPolicyId());
        assertEquals(policy.getExpiryDate(), response.getPreviousExpiryDate());
        verify(renewalRepository).save(any(PolicyRenewal.class));
    }

    @Test
    void create_duplicatePending_returnsExistingWithoutSaving() {
        User owner = customer(1L, "jane@example.com", Role.ROLE_CUSTOMER);
        InsurancePolicy policy = policy(5L, owner, LocalDate.now().plusDays(20));
        when(policyService.requireOwned("jane@example.com", 5L)).thenReturn(policy);
        PolicyRenewal existing = new PolicyRenewal();
        existing.setId(11L);
        existing.setPolicy(policy);
        existing.setPreviousExpiryDate(policy.getExpiryDate());
        existing.setNewExpiryDate(LocalDate.now().plusDays(385));
        existing.setStatus(RenewalStatus.PENDING);
        when(renewalRepository.findFirstByPolicyIdAndStatus(5L, RenewalStatus.PENDING))
                .thenReturn(Optional.of(existing));

        RenewalResponse response = renewalService.create("jane@example.com", 5L,
                renewalRequest(LocalDate.now().plusDays(385)));

        assertEquals(11L, response.getId());
        assertEquals(RenewalStatus.PENDING.name(), response.getStatus());
        verify(renewalRepository, never()).save(any(PolicyRenewal.class));
    }

    @Test
    void complete_updatesPolicyExpiryAndMarksCompleted() {
        User admin = customer(9L, "admin@example.com", Role.ROLE_ADMIN);
        User owner = customer(1L, "jane@example.com", Role.ROLE_CUSTOMER);
        InsurancePolicy policy = policy(5L, owner, LocalDate.now().minusDays(2));
        policy.setStatus(PolicyStatus.EXPIRED);
        PolicyRenewal renewal = new PolicyRenewal();
        renewal.setId(11L);
        renewal.setPolicy(policy);
        renewal.setPreviousExpiryDate(policy.getExpiryDate());
        renewal.setNewExpiryDate(LocalDate.now().plusDays(363));
        renewal.setStatus(RenewalStatus.PENDING);
        when(renewalRepository.findById(11L)).thenReturn(Optional.of(renewal));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(policyService.calculatePolicyStatus(any(InsurancePolicy.class)))
                .thenReturn(PolicyStatus.ACTIVE);
        when(policyRepository.save(any(InsurancePolicy.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(renewalRepository.save(any(PolicyRenewal.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RenewalResponse response = renewalService.complete("admin@example.com", 11L);

        assertEquals(RenewalStatus.COMPLETED.name(), response.getStatus());
        assertEquals(renewal.getNewExpiryDate(), policy.getExpiryDate());
        assertEquals(PolicyStatus.ACTIVE, policy.getStatus());
        verify(policyRepository).save(policy);
    }

    @Test
    void complete_unknownRenewal_rejected() {
        when(renewalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> renewalService.complete("admin@example.com", 99L));
    }

    @Test
    void markMissed_setsCancelled() {
        PolicyRenewal renewal = new PolicyRenewal();
        renewal.setId(11L);
        renewal.setStatus(RenewalStatus.PENDING);
        when(renewalRepository.findById(11L)).thenReturn(Optional.of(renewal));
        when(renewalRepository.save(any(PolicyRenewal.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RenewalResponse response = renewalService.markMissed(11L);

        assertEquals(RenewalStatus.CANCELLED.name(), response.getStatus());
    }

    @Test
    void daysRemaining_futurePositivePastNegativeNullZero() {
        assertTrue(renewalService.daysRemaining(LocalDate.now().plusDays(10)) > 0);
        assertEquals(0, renewalService.daysRemaining(LocalDate.now()));
        assertTrue(renewalService.daysRemaining(LocalDate.now().minusDays(3)) < 0);
        assertEquals(0, renewalService.daysRemaining(null));
    }

    @Test
    void upcoming_returnsOnlyPendingForCustomer() {
        User owner = customer(1L, "jane@example.com", Role.ROLE_CUSTOMER);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(owner));
        CustomerProfile profile = new CustomerProfile();
        profile.setId(1L);
        profile.setUser(owner);
        when(profileRepository.findByUserEmail("jane@example.com"))
                .thenReturn(Optional.of(profile));
        InsurancePolicy policy = policy(5L, owner, LocalDate.now().plusDays(20));
        PolicyRenewal pending = new PolicyRenewal();
        pending.setId(11L);
        pending.setPolicy(policy);
        pending.setStatus(RenewalStatus.PENDING);
        PolicyRenewal completed = new PolicyRenewal();
        completed.setId(12L);
        completed.setPolicy(policy);
        completed.setStatus(RenewalStatus.COMPLETED);
        when(renewalRepository.findByPolicyCustomerId(1L))
                .thenReturn(List.of(pending, completed));

        List<RenewalResponse> result = renewalService.upcoming("jane@example.com");

        assertEquals(1, result.size());
        assertEquals(11L, result.get(0).getId());
    }

    @Test
    void expiringWithinDays_mapsPoliciesForAdmin() {
        User admin = customer(9L, "admin@example.com", Role.ROLE_ADMIN);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        LocalDate expiry = LocalDate.now().plusDays(15);
        InsurancePolicy policy = policy(5L, customer(1L, "jane@example.com", Role.ROLE_CUSTOMER), expiry);
        when(policyRepository.findByExpiryDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(policy));

        List<RenewalResponse> result =
                renewalService.expiringWithinDays("admin@example.com", 30);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getPolicyId());
        assertEquals(15L, result.get(0).getDaysRemaining());
    }
}
