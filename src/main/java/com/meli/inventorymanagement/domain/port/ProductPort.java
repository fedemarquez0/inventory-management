package com.meli.inventorymanagement.domain.port;

import com.meli.inventorymanagement.domain.model.Product;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para operaciones de productos
 */
public interface ProductPort {

    Mono<Product> findById(Long id);

    Mono<Product> findBySku(String sku);
}

