package com.insurance.platform.controller;

import com.insurance.platform.dto.common.ApiResponse;
import com.insurance.platform.dto.payment.PaymentRequest;
import com.insurance.platform.dto.payment.PaymentResponse;
import com.insurance.platform.dto.payment.PaymentSummaryResponse;
import com.insurance.platform.service.PremiumPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Premium payments nested under policies.
 */
@RestController
@RequestMapping("/api/v1/policies/{policyId}/payments")
public class PremiumPaymentController {

    private final PremiumPaymentService paymentService;

    public PremiumPaymentController(PremiumPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** Lists payments for an owned policy. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> list(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long policyId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.list(principal.getUsername(), policyId)));
    }

    /** Returns payment totals for an owned policy. */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PaymentSummaryResponse>> summary(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long policyId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.summary(principal.getUsername(), policyId)));
    }

    /** Records a payment for an owned policy. */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> create(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long policyId,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Payment recorded",
                paymentService.create(principal.getUsername(), policyId, request)));
    }
}
