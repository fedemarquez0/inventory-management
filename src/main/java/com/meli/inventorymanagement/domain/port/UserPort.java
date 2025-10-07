package com.meli.inventorymanagement.domain.port;

import com.meli.inventorymanagement.domain.model.User;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para operaciones de usuarios
 */
public interface UserPort {

    Mono<User> findByUsername(String username);
}
