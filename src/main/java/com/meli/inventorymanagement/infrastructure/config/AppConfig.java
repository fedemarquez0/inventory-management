package com.meli.inventorymanagement.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración principal de la aplicación que consolida todas las configuraciones
 * para mantener el código limpio y profesional
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.meli.inventorymanagement.infrastructure.adapter.output.persistence")
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableRetry
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
