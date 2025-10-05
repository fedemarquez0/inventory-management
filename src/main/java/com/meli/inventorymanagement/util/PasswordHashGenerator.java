package com.meli.inventorymanagement.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "12345";

        // Generar hash
        String hashedPassword = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hashedPassword);

        // Verificar que funciona
        boolean matches = encoder.matches(password, hashedPassword);
        System.out.println("Verification test: " + matches);

        // Generar varios para mostrar que siempre son diferentes pero válidos
        System.out.println("\nMultiple hashes for same password (all valid):");
        for (int i = 0; i < 3; i++) {
            String hash = encoder.encode(password);
            boolean valid = encoder.matches(password, hash);
            System.out.println("Hash " + (i+1) + ": " + hash + " (valid: " + valid + ")");
        }
    }
}
