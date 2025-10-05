package com.meli.inventorymanagement.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.meli.inventorymanagement.infrastructure.adapter.output.persistence")
@EnableTransactionManagement
public class DatabaseConfig {
}

