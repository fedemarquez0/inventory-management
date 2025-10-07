package com.meli.inventorymanagement.infrastructure.adapter.output.persistence;

import com.meli.inventorymanagement.domain.model.Store;
import com.meli.inventorymanagement.domain.port.StorePort;
import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.entity.StoreEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorePersistenceAdapter implements StorePort {

    private final StoreRepository storeRepository;

    @Override
    public Mono<Store> findById(Long id) {
        if (id == null) {
            return Mono.empty();
        }

        return storeRepository.findById(id)
                .map(this::toDomain)
                .doOnError(error -> log.error("Error finding store by ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Boolean> existsById(Long id) {
        if (id == null) {
            return Mono.just(false);
        }

        return storeRepository.existsById(id)
                .doOnError(error -> log.error("Error checking if store exists by ID {}: {}", id, error.getMessage()))
                .onErrorReturn(false);
    }

    private Store toDomain(StoreEntity entity) {
        if (entity == null) {
            return null;
        }

        return Store.builder()
                .id(entity.getId())
                .name(entity.getName())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
