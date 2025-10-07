package com.meli.inventorymanagement.domain.port;

/**
 * Puerto de salida para operaciones de generación de tokens
 */
public interface TokenGeneratorPort {

    String generateToken(String username);

    String extractUsername(String token);

    boolean validateToken(String token, String username);
}

