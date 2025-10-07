package com.meli.inventorymanagement.domain.port;

import com.meli.inventorymanagement.domain.model.Store;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para operaciones de tiendas
 */
public interface StorePort {

    Mono<Store> findById(Long id);

    Mono<Boolean> existsById(Long id);
}

