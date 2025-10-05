package com.meli.inventorymanagement.application.service;

import com.meli.inventorymanagement.application.dto.AuthRequest;
import com.meli.inventorymanagement.application.dto.AuthResponse;
import com.meli.inventorymanagement.common.constant.ErrorCode;
import com.meli.inventorymanagement.infrastructure.exception.BusinessException;
import com.meli.inventorymanagement.infrastructure.security.CustomUserDetailsService;
import com.meli.inventorymanagement.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public AuthResponse authenticate(AuthRequest request) {
        log.info("Authentication attempt for user: {}", request.getUsername());

        boolean authenticated = userDetailsService.authenticate(
                request.getUsername(),
                request.getPassword()
        );

        if (!authenticated) {
            log.warn("Authentication failed for user: {}", request.getUsername());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = jwtUtil.generateToken(request.getUsername());

        log.info("Authentication successful for user: {}", request.getUsername());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .username(request.getUsername())
                .build();
    }
}
