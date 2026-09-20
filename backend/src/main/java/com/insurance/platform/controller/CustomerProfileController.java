package com.insurance.platform.controller;

import com.insurance.platform.dto.common.ApiResponse;
import com.insurance.platform.dto.customer.CustomerProfileRequest;
import com.insurance.platform.dto.customer.CustomerProfileResponse;
import com.insurance.platform.service.CustomerProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Current-customer profile endpoints.
 */
@RestController
@RequestMapping("/api/v1/profile")
public class CustomerProfileController {

    private final CustomerProfileService profileService;

    public CustomerProfileController(CustomerProfileService profileService) {
        this.profileService = profileService;
    }

    /** Returns the current customer's profile. */
    @GetMapping
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> get(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getMyProfile(principal.getUsername())));
    }

    /** Creates or updates the current customer's profile. */
    @PutMapping
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> update(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CustomerProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Profile saved",
                profileService.updateMyProfile(principal.getUsername(), request)));
    }
}
