package com.meli.inventorymanagement.infrastructure.adapter.input.rest;

import com.meli.inventorymanagement.application.dto.AuthRequest;
import com.meli.inventorymanagement.application.dto.AuthResponse;
import com.meli.inventorymanagement.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Authentication API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Authenticate user", description = "Authenticate and get JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request,
                                            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        log.info("POST /api/auth/login - Username: {} - IP: {} - User-Agent: {} - Authentication attempt",
                request.getUsername(), clientIp, userAgent);

        try {
            AuthResponse response = authService.authenticate(request);

            log.info("Authentication successful - Username: {} - IP: {} - Token generated successfully",
                    request.getUsername(), clientIp);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Authentication failed - Username: {} - IP: {} - User-Agent: {} - Error: {}",
                    request.getUsername(), clientIp, userAgent, e.getMessage());
            throw e;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
