package com.insurance.platform.controller;

import com.insurance.platform.dto.common.ApiResponse;
import com.insurance.platform.dto.renewal.RenewalRequest;
import com.insurance.platform.dto.renewal.RenewalResponse;
import com.insurance.platform.service.RenewalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Renewal endpoints.
 */
@RestController
@RequestMapping("/api/v1/renewals")
public class RenewalController {

    private final RenewalService renewalService;

    public RenewalController(RenewalService renewalService) {
        this.renewalService = renewalService;
    }

    /** Lists renewals for the caller (upcoming by default). */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RenewalResponse>>> list(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(renewalService.upcoming(principal.getUsername())));
    }

    /** Lists pending renewals for the caller. */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<RenewalResponse>>> upcoming(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(renewalService.upcoming(principal.getUsername())));
    }

    /** Lists owned policies expiring within N days (default 30). */
    @GetMapping("/expiring")
    public ResponseEntity<ApiResponse<List<RenewalResponse>>> expiring(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.ok(renewalService.expiringWithinDays(principal.getUsername(), days)));
    }

    /** Lists owned policies that have already expired. */
    @GetMapping("/expired")
    public ResponseEntity<ApiResponse<List<RenewalResponse>>> expired(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(renewalService.expired(principal.getUsername())));
    }

    /** Creates (or dedupes) a renewal for a policy. */
    @PostMapping("/{policyId}")
    public ResponseEntity<ApiResponse<RenewalResponse>> create(@AuthenticationPrincipal UserDetails principal,
                                                               @PathVariable Long policyId,
                                                               @Valid @RequestBody RenewalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Renewal created",
                renewalService.create(principal.getUsername(), policyId, request)));
    }

    /** Completes a renewal and extends the policy expiry. */
    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<RenewalResponse>> complete(@AuthenticationPrincipal UserDetails principal,
                                                                 @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Renewal completed",
                renewalService.complete(principal.getUsername(), id)));
    }
}
