package com.insurance.platform.controller;

import com.insurance.platform.dto.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Liveness endpoint.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /** Returns service status. */
    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    /** Compatibility alias returning the wrapped envelope. */
    @GetMapping("/wrapped")
    public ResponseEntity<ApiResponse<Map<String, String>>> healthWrapped() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("status", "UP")));
    }
}
