package com.meli.inventorymanagement.domain.port;

import com.meli.inventorymanagement.domain.model.Inventory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para operaciones de inventario
 */
public interface InventoryPort {

    Flux<Inventory> findByProductSku(String sku);

    Mono<Inventory> findByProductSkuAndStoreId(String sku, Long storeId);

    Mono<Inventory> findByProductIdAndStoreId(Long productId, Long storeId);

    Mono<Inventory> save(Inventory inventory);
}

