package com.meli.inventorymanagement.infrastructure.adapter.input.rest;

import com.meli.inventorymanagement.application.dto.AuthRequest;
import com.meli.inventorymanagement.application.dto.AuthResponse;
import com.meli.inventorymanagement.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Tag(name = "Authentication", description = "Authentication API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Authenticate user", description = "Authenticate and get JWT token")
    @PostMapping("/login")
    public Mono<AuthResponse> login(@Valid @RequestBody AuthRequest request,
                                     ServerWebExchange exchange) {

        String clientIp = getClientIpAddress(exchange);
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");

        log.info("POST /api/auth/login - Username: {} - IP: {} - User-Agent: {} - Authentication attempt",
                request.getUsername(), clientIp, userAgent);

        return authService.authenticate(request)
                .doOnSuccess(response -> log.info("Authentication successful - Username: {} - IP: {} - Token generated successfully",
                        request.getUsername(), clientIp))
                .doOnError(e -> log.warn("Authentication failed - Username: {} - IP: {} - User-Agent: {} - Error: {}",
                        request.getUsername(), clientIp, userAgent, e.getMessage()));
    }

    private String getClientIpAddress(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }
}
