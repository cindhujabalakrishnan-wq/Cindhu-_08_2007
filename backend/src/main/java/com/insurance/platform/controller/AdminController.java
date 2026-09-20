package com.insurance.platform.controller;

import com.insurance.platform.dto.admin.AuditLogResponse;
import com.insurance.platform.dto.admin.DashboardStatsResponse;
import com.insurance.platform.dto.common.ApiResponse;
import com.insurance.platform.dto.policy.PolicyResponse;
import com.insurance.platform.dto.policy.PolicySearchParams;
import com.insurance.platform.dto.renewal.RenewalResponse;
import com.insurance.platform.dto.user.UserResponse;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.service.AdminService;
import com.insurance.platform.service.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin-only read models.
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    /** Platform-wide dashboard statistics. */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.dashboardStats()));
    }

    /** Paged users. */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> users(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.users(pageable)));
    }

    /** Paged policies with optional filters. */
    @GetMapping("/policies")
    public ResponseEntity<ApiResponse<Page<PolicyResponse>>> policies(@ParameterObject PolicySearchParams params,
                                                                      @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.policies(params, pageable)));
    }

    /** Paged renewals. */
    @GetMapping("/renewals")
    public ResponseEntity<ApiResponse<Page<RenewalResponse>>> renewals(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.renewals(pageable)));
    }

    /** Paged audit logs. */
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> auditLogs(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.auditLogs(pageable)));
    }

    /** Changes a user's role. */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(@PathVariable Long id,
                                                                @RequestBody Map<String, String> body) {
        Role role = Role.ROLE_CUSTOMER;
        String raw = body != null ? body.get("role") : null;
        if (raw != null && !raw.isBlank()) {
            String normalized = raw.trim().toUpperCase();
            if (!normalized.startsWith("ROLE_")) {
                normalized = "ROLE_" + normalized;
            }
            role = Role.valueOf(normalized);
        }
        return ResponseEntity.ok(ApiResponse.ok("Role updated", userService.setRole(id, role)));
    }

    /** Enables or disables a user account. */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(@PathVariable Long id,
                                                                  @RequestBody Map<String, Object> body) {
        boolean enabled = true;
        if (body != null && body.get("enabled") instanceof Boolean b) {
            enabled = b;
        } else if (body != null && body.get("status") != null) {
            enabled = !"DISABLED".equalsIgnoreCase(String.valueOf(body.get("status")));
        }
        return ResponseEntity.ok(ApiResponse.ok("Status updated", userService.setEnabled(id, enabled)));
    }

    /** Deletes a user account. */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted"));
    }
}
