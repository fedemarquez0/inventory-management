package com.meli.inventorymanagement.infrastructure.adapter.output.persistence;

import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.entity.StoreEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StoreRepository extends R2dbcRepository<StoreEntity, Long> {

    Mono<StoreEntity> findByName(String name);

    Flux<StoreEntity> findByIsActiveTrue();
}
