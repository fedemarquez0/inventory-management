package com.meli.inventorymanagement.domain.port;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para operaciones de autenticación
 */
public interface AuthenticationPort {

    Mono<Boolean> authenticate(String username, String password);

    Mono<Boolean> hasStorePermission(String username, Long storeId);
}

