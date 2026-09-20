package com.insurance.platform.service;

import com.insurance.platform.dto.customer.CustomerProfileRequest;
import com.insurance.platform.dto.customer.CustomerProfileResponse;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.model.entity.CustomerProfile;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.CustomerProfileRepository;
import com.insurance.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceTest {

    @Mock
    private CustomerProfileRepository profileRepository;
    @Mock
    private UserRepository userRepository;

    private CustomerProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new CustomerProfileService(profileRepository, userRepository);
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setRole(Role.ROLE_CUSTOMER);
        return user;
    }

    private CustomerProfile profile(User owner) {
        CustomerProfile profile = new CustomerProfile();
        profile.setId(2L);
        profile.setUser(owner);
        profile.setCity("Chennai");
        return profile;
    }

    @Test
    void getMyProfile_existing_returnsMapped() {
        when(profileRepository.findByUserEmail("jane@example.com"))
                .thenReturn(Optional.of(profile(user())));

        CustomerProfileResponse response = profileService.getMyProfile("jane@example.com");

        assertEquals(2L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals("jane@example.com", response.getEmail());
        assertEquals("Chennai", response.getCity());
    }

    @Test
    void getMyProfile_missing_createsLazily() {
        User owner = user();
        when(profileRepository.findByUserEmail("jane@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(owner));
        when(profileRepository.save(any(CustomerProfile.class)))
                .thenAnswer(inv -> {
                    CustomerProfile p = inv.getArgument(0);
                    p.setId(2L);
                    return p;
                });

        CustomerProfileResponse response = profileService.getMyProfile("jane@example.com");

        assertEquals(2L, response.getId());
        assertEquals("jane@example.com", response.getEmail());
        verify(profileRepository).save(any(CustomerProfile.class));
    }

    @Test
    void getMyProfile_missingUser_rejected() {
        when(profileRepository.findByUserEmail("ghost@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> profileService.getMyProfile("ghost@example.com"));
    }

    @Test
    void updateMyProfile_updatesFields() {
        CustomerProfile existing = profile(user());
        when(profileRepository.findByUserEmail("jane@example.com"))
                .thenReturn(Optional.of(existing));
        when(profileRepository.save(any(CustomerProfile.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CustomerProfileRequest request = new CustomerProfileRequest();
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setAddress("1 Main St");
        request.setCity("Bengaluru");
        request.setState("Karnataka");
        request.setPostalCode("560001");

        CustomerProfileResponse response =
                profileService.updateMyProfile("jane@example.com", request);

        assertEquals(LocalDate.of(1990, 1, 1), response.getDateOfBirth());
        assertEquals("Bengaluru", response.getCity());
        assertEquals("560001", response.getPostalCode());
    }
}
