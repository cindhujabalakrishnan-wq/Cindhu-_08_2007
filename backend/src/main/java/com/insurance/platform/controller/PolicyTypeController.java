package com.insurance.platform.controller;

import com.insurance.platform.dto.common.ApiResponse;
import com.insurance.platform.model.entity.PolicyType;
import com.insurance.platform.model.enums.PolicyCategory;
import com.insurance.platform.service.PolicyTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Policy type catalog: public reads, admin writes.
 */
@RestController
@RequestMapping("/api/v1/policy-types")
public class PolicyTypeController {

    private final PolicyTypeService policyTypeService;

    public PolicyTypeController(PolicyTypeService policyTypeService) {
        this.policyTypeService = policyTypeService;
    }

    /** Lists policy types with optional filters. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PolicyType>>> list(
            @RequestParam(required = false) PolicyCategory category,
            @RequestParam(required = false) Boolean activeOnly) {
        return ResponseEntity.ok(ApiResponse.ok(policyTypeService.list(category, activeOnly)));
    }

    /** Fetches a single policy type. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PolicyType>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(policyTypeService.get(id)));
    }

    /** Creates a policy type (admin). */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PolicyType>> create(@RequestBody PolicyType body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Policy type created", policyTypeService.create(body)));
    }

    /** Updates a policy type (admin). */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PolicyType>> update(@PathVariable Long id, @RequestBody PolicyType body) {
        return ResponseEntity.ok(ApiResponse.ok("Policy type updated", policyTypeService.update(id, body)));
    }
}
