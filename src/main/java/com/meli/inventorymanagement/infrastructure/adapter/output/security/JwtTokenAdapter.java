package com.meli.inventorymanagement.infrastructure.adapter.output.security;

import com.meli.inventorymanagement.domain.port.TokenGeneratorPort;
import com.meli.inventorymanagement.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenAdapter implements TokenGeneratorPort {

    private final JwtUtil jwtUtil;

    @Override
    public String generateToken(String username) {
        return jwtUtil.generateToken(username);
    }

    @Override
    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }

    @Override
    public boolean validateToken(String token, String username) {
        return jwtUtil.validateToken(token, username);
    }
}
