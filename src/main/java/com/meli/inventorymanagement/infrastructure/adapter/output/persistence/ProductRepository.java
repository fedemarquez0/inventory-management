package com.meli.inventorymanagement.infrastructure.adapter.output.persistence;

import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.entity.ProductEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends R2dbcRepository<ProductEntity, Long> {

    Mono<ProductEntity> findBySku(String sku);

    Mono<Boolean> existsBySku(String sku);
}
