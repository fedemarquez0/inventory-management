package com.meli.inventorymanagement.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración principal de la aplicación reactiva
 * Configuración para R2DBC y programación reactiva
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "com.meli.inventorymanagement.infrastructure.adapter.output.persistence")
@EnableAspectJAutoProxy
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
