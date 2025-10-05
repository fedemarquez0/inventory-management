package com.meli.inventorymanagement.application.service;

import com.meli.inventorymanagement.application.dto.AuthRequest;
import com.meli.inventorymanagement.application.dto.AuthResponse;
import com.meli.inventorymanagement.common.constant.ErrorCode;
import com.meli.inventorymanagement.infrastructure.exception.BusinessException;
import com.meli.inventorymanagement.infrastructure.security.UserDetailsService;
import com.meli.inventorymanagement.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public Mono<AuthResponse> authenticate(AuthRequest request) {
        log.info("Authentication attempt for user: {}", request.getUsername());

        // Validate input parameters
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return Mono.error(new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Username cannot be empty"));
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return Mono.error(new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Password cannot be empty"));
        }

        return userDetailsService.authenticate(request.getUsername().trim(), request.getPassword())
                .flatMap(authenticated -> {
                    if (!authenticated) {
                        log.warn("Authentication failed for user: {}", request.getUsername());
                        return Mono.error(new BusinessException(ErrorCode.INVALID_CREDENTIALS,
                                "Invalid username or password"));
                    }

                    return Mono.fromCallable(() -> jwtUtil.generateToken(request.getUsername().trim()))
                            .onErrorMap(e -> {
                                log.error("Error generating JWT token for user {}: {}", request.getUsername(), e.getMessage(), e);
                                return new BusinessException(ErrorCode.TOKEN_EXTRACTION_ERROR,
                                        "Failed to generate authentication token");
                            })
                            .map(token -> {
                                log.info("Authentication successful for user: {}", request.getUsername());
                                return AuthResponse.builder()
                                        .token(token)
                                        .type("Bearer")
                                        .username(request.getUsername().trim())
                                        .build();
                            });
                })
                .onErrorMap(ex -> !(ex instanceof BusinessException),
                        ex -> {
                            log.error("Unexpected error during authentication for user {}: {}",
                                    request.getUsername(), ex.getMessage(), ex);
                            return new BusinessException(ErrorCode.AUTHENTICATION_FAILED,
                                    "Authentication process failed");
                        });
    }
}
