package com.tradexchange.controller;

import com.tradexchange.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Application health status check")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<ApiResponse<Void>> checkHealth() {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus("OK");
        return ResponseEntity.ok(response);
    }
}
