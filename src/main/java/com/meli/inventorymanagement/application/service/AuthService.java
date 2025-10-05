package com.meli.inventorymanagement.application.service;

import com.meli.inventorymanagement.application.dto.AuthRequest;
import com.meli.inventorymanagement.application.dto.AuthResponse;
import com.meli.inventorymanagement.common.constant.ErrorCode;
import com.meli.inventorymanagement.infrastructure.exception.BusinessException;
import com.meli.inventorymanagement.infrastructure.security.CustomUserDetailsService;
import com.meli.inventorymanagement.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public AuthResponse authenticate(AuthRequest request) {
        log.info("Authentication attempt for user: {}", request.getUsername());

        // Validate input parameters
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Username cannot be empty");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Password cannot be empty");
        }

        try {
            boolean authenticated = userDetailsService.authenticate(
                    request.getUsername().trim(),
                    request.getPassword()
            );

            if (!authenticated) {
                log.warn("Authentication failed for user: {}", request.getUsername());
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS,
                        "Invalid username or password");
            }

            String token;
            try {
                token = jwtUtil.generateToken(request.getUsername().trim());
            } catch (Exception e) {
                log.error("Error generating JWT token for user {}: {}", request.getUsername(), e.getMessage(), e);
                throw new BusinessException(ErrorCode.TOKEN_EXTRACTION_ERROR,
                        "Failed to generate authentication token");
            }

            log.info("Authentication successful for user: {}", request.getUsername());

            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .username(request.getUsername().trim())
                    .build();

        } catch (BusinessException e) {
            throw e; // Re-throw business exceptions as-is
        } catch (DataAccessException e) {
            log.error("Database error during authentication for user {}: {}",
                     request.getUsername(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR,
                    "Authentication service temporarily unavailable");
        } catch (Exception e) {
            log.error("Unexpected error during authentication for user {}: {}",
                     request.getUsername(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED,
                    "Authentication process failed");
        }
    }
}
