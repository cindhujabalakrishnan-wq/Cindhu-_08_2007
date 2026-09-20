package com.insurance.platform.controller;

import com.insurance.platform.dto.auth.AuthResponse;
import com.insurance.platform.dto.auth.LoginRequest;
import com.insurance.platform.dto.auth.RegisterRequest;
import com.insurance.platform.dto.auth.UserInfo;
import com.insurance.platform.dto.common.ApiResponse;
import com.insurance.platform.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public authentication endpoints plus current-user lookup.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Registers a new customer account. */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Registered successfully", authService.register(request)));
    }

    /** Authenticates and returns a JWT. */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Logged in successfully", authService.login(request)));
    }

    /** Returns the current authenticated user. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfo>> me(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(authService.me(principal.getUsername())));
    }
}
