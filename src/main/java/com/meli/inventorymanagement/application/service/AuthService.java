package com.meli.inventorymanagement.application.service;

import com.meli.inventorymanagement.application.dto.AuthRequest;
import com.meli.inventorymanagement.application.dto.AuthResponse;
import com.meli.inventorymanagement.common.constant.ErrorCode;
import com.meli.inventorymanagement.domain.exception.BusinessException;
import com.meli.inventorymanagement.domain.port.AuthenticationPort;
import com.meli.inventorymanagement.domain.port.TokenGeneratorPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenGeneratorPort tokenGeneratorPort;
    private final AuthenticationPort authenticationPort;

    public Mono<AuthResponse> authenticate(AuthRequest request) {
        log.info("Authentication attempt for user: {}", request.getUsername());

        return validateRequest(request)
                .then(authenticationPort.authenticate(request.getUsername().trim(), request.getPassword()))
                .flatMap(authenticated -> {
                    if (!authenticated) {
                        log.warn("Authentication failed for user: {}", request.getUsername());
                        return Mono.error(new BusinessException(ErrorCode.INVALID_CREDENTIALS,
                                "Invalid username or password"));
                    }

                    return generateTokenResponse(request.getUsername().trim());
                })
                .onErrorResume(ex -> {
                    if (ex instanceof BusinessException) {
                        return Mono.error(ex);
                    }
                    log.error("Unexpected error during authentication for user {}: {}",
                            request.getUsername(), ex.getMessage(), ex);
                    return Mono.error(new BusinessException(ErrorCode.AUTHENTICATION_FAILED,
                            "Authentication process failed"));
                });
    }

    private Mono<Void> validateRequest(AuthRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return Mono.error(new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Username cannot be empty"));
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return Mono.error(new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Password cannot be empty"));
        }

        return Mono.empty();
    }

    private Mono<AuthResponse> generateTokenResponse(String username) {
        return Mono.fromCallable(() -> tokenGeneratorPort.generateToken(username))
                .onErrorMap(e -> {
                    log.error("Error generating JWT token for user {}: {}", username, e.getMessage(), e);
                    return new BusinessException(ErrorCode.TOKEN_EXTRACTION_ERROR,
                            "Failed to generate authentication token");
                })
                .map(token -> {
                    log.info("Authentication successful for user: {}", username);
                    return AuthResponse.builder()
                            .token(token)
                            .type("Bearer")
                            .username(username)
                            .build();
                });
    }
}
