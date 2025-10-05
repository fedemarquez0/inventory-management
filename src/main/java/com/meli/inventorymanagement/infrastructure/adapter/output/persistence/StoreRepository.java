package com.meli.inventorymanagement.infrastructure.adapter.output.persistence;

import com.meli.inventorymanagement.domain.model.Store;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StoreRepository extends R2dbcRepository<Store, Long> {

    Mono<Store> findByName(String name);

    Flux<Store> findByIsActiveTrue();
}
