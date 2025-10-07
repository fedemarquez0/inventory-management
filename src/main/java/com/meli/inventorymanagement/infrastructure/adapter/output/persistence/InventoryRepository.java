package com.meli.inventorymanagement.infrastructure.adapter.output.persistence;

import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.entity.InventoryEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface InventoryRepository extends R2dbcRepository<InventoryEntity, Long> {

    @Query("SELECT i.* FROM inventory i " +
           "JOIN products p ON i.product_id = p.id " +
           "WHERE p.sku = :sku")
    Flux<InventoryEntity> findByProductSku(@Param("sku") String sku);

    @Query("SELECT i.* FROM inventory i " +
           "JOIN products p ON i.product_id = p.id " +
           "WHERE p.sku = :sku AND i.store_id = :storeId")
    Mono<InventoryEntity> findByProductSkuAndStoreId(@Param("sku") String sku, @Param("storeId") Long storeId);

    @Query("SELECT i.* FROM inventory i " +
           "WHERE i.product_id = :productId AND i.store_id = :storeId")
    Mono<InventoryEntity> findByProductIdAndStoreId(@Param("productId") Long productId, @Param("storeId") Long storeId);
}
