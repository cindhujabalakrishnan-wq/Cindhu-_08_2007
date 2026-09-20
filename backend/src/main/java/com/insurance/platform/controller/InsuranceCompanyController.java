package com.insurance.platform.controller;

import com.insurance.platform.dto.common.ApiResponse;
import com.insurance.platform.model.entity.InsuranceCompany;
import com.insurance.platform.service.InsuranceCompanyService;
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
 * Insurance company catalog: public reads, admin writes.
 */
@RestController
@RequestMapping({"/api/v1/companies", "/api/v1/insurance-companies"})
public class InsuranceCompanyController {

    private final InsuranceCompanyService companyService;

    public InsuranceCompanyController(InsuranceCompanyService companyService) {
        this.companyService = companyService;
    }

    /** Lists companies with an optional active-only filter. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<InsuranceCompany>>> list(
            @RequestParam(required = false) Boolean activeOnly) {
        return ResponseEntity.ok(ApiResponse.ok(companyService.list(activeOnly)));
    }

    /** Fetches a single company. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InsuranceCompany>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(companyService.get(id)));
    }

    /** Creates a company (admin). */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InsuranceCompany>> create(@RequestBody InsuranceCompany body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Company created", companyService.create(body)));
    }

    /** Updates a company (admin). */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InsuranceCompany>> update(@PathVariable Long id,
                                                                @RequestBody InsuranceCompany body) {
        return ResponseEntity.ok(ApiResponse.ok("Company updated", companyService.update(id, body)));
    }
}
