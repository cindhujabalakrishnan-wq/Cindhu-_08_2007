package com.insurance.platform.controller;

import com.insurance.platform.dto.common.ApiResponse;
import com.insurance.platform.dto.policy.DashboardSummaryResponse;
import com.insurance.platform.dto.policy.PolicyRequest;
import com.insurance.platform.dto.policy.PolicyResponse;
import com.insurance.platform.dto.policy.PolicySearchParams;
import com.insurance.platform.service.PolicyService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Policy CRUD plus filtering and the per-customer dashboard summary.
 */
@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    /** Lists/filters the caller's policies (status, category, expiringWithin, search). */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PolicyResponse>>> list(
            @AuthenticationPrincipal UserDetails principal,
            @ParameterObject PolicySearchParams params,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.search(principal.getUsername(), params, pageable)));
    }

    /** Returns the per-customer dashboard summary. */
    @GetMapping("/dashboard-summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> dashboardSummary(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.dashboardSummary(principal.getUsername())));
    }

    /** Fetches a single owned policy. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PolicyResponse>> get(@AuthenticationPrincipal UserDetails principal,
                                                           @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.getById(principal.getUsername(), id)));
    }

    /** Creates a policy for the current user. */
    @PostMapping
    public ResponseEntity<ApiResponse<PolicyResponse>> create(@AuthenticationPrincipal UserDetails principal,
                                                              @Valid @RequestBody PolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Policy created", policyService.create(principal.getUsername(), request)));
    }

    /** Updates an owned policy. */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PolicyResponse>> update(@AuthenticationPrincipal UserDetails principal,
                                                              @PathVariable Long id,
                                                              @Valid @RequestBody PolicyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Policy updated",
                policyService.update(principal.getUsername(), id, request)));
    }

    /** Deletes an eligible policy (blocked for ACTIVE policies with PAID payments). */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserDetails principal,
                                                    @PathVariable Long id) {
        policyService.delete(principal.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("Policy deleted"));
    }
}
