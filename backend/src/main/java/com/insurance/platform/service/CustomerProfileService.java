package com.insurance.platform.service;

import com.insurance.platform.dto.customer.CustomerProfileRequest;
import com.insurance.platform.dto.customer.CustomerProfileResponse;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.model.entity.CustomerProfile;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.repository.CustomerProfileRepository;
import com.insurance.platform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages the customer profile attached to a user account.
 */
@Service
public class CustomerProfileService {

    private static final Logger log = LoggerFactory.getLogger(CustomerProfileService.class);

    private final CustomerProfileRepository profileRepository;
    private final UserRepository userRepository;

    public CustomerProfileService(CustomerProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    /** Returns the current user's profile, creating one lazily if missing. */
    @Transactional
    public CustomerProfileResponse getMyProfile(String email) {
        return toResponse(requireProfile(email));
    }

    /** Creates or updates the current user's profile. */
    @Transactional
    public CustomerProfileResponse updateMyProfile(String email, CustomerProfileRequest request) {
        CustomerProfile profile = requireProfile(email);
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setAddress(request.getAddress());
        profile.setCity(request.getCity());
        profile.setState(request.getState());
        profile.setPostalCode(request.getPostalCode());
        CustomerProfile saved = profileRepository.save(profile);
        log.info("Profile updated for {}", email);
        return toResponse(saved);
    }

    /** Loads the profile entity for ownership checks. */
    @Transactional(readOnly = true)
    public CustomerProfile requireProfileEntity(String email) {
        return requireProfile(email);
    }

    private CustomerProfile requireProfile(String email) {
        return profileRepository.findByUserEmail(email).orElseGet(() -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
            CustomerProfile created = new CustomerProfile();
            created.setUser(user);
            return profileRepository.save(created);
        });
    }

    private CustomerProfileResponse toResponse(CustomerProfile profile) {
        CustomerProfileResponse dto = new CustomerProfileResponse();
        dto.setId(profile.getId());
        if (profile.getUser() != null) {
            dto.setUserId(profile.getUser().getId());
            dto.setEmail(profile.getUser().getEmail());
        }
        dto.setDateOfBirth(profile.getDateOfBirth());
        dto.setAddress(profile.getAddress());
        dto.setCity(profile.getCity());
        dto.setState(profile.getState());
        dto.setPostalCode(profile.getPostalCode());
        return dto;
    }
}
