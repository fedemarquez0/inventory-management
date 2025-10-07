package com.meli.inventorymanagement.infrastructure.adapter.output.persistence;

import com.meli.inventorymanagement.domain.model.Inventory;
import com.meli.inventorymanagement.domain.port.InventoryPort;
import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.entity.InventoryEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryPersistenceAdapter implements InventoryPort {

    private final InventoryRepository inventoryRepository;

    @Override
    public Flux<Inventory> findByProductSku(String sku) {
        return inventoryRepository.findByProductSku(sku)
                .map(this::toDomain)
                .doOnError(error -> log.error("Error finding inventory by SKU {}: {}", sku, error.getMessage()));
    }

    @Override
    public Mono<Inventory> findByProductSkuAndStoreId(String sku, Long storeId) {
        return inventoryRepository.findByProductSkuAndStoreId(sku, storeId)
                .map(this::toDomain)
                .doOnError(error -> log.error("Error finding inventory by SKU {} and store {}: {}",
                        sku, storeId, error.getMessage()));
    }

    @Override
    public Mono<Inventory> findByProductIdAndStoreId(Long productId, Long storeId) {
        return inventoryRepository.findByProductIdAndStoreId(productId, storeId)
                .map(this::toDomain)
                .doOnError(error -> log.error("Error finding inventory by product {} and store {}: {}",
                        productId, storeId, error.getMessage()));
    }

    @Override
    public Mono<Inventory> save(Inventory inventory) {
        if (inventory == null) {
            return Mono.error(new IllegalArgumentException("Inventory cannot be null"));
        }

        return inventoryRepository.save(toEntity(inventory))
                .map(this::toDomain)
                .doOnSuccess(saved -> log.debug("Saved inventory with ID: {}", saved.getId()))
                .doOnError(error -> log.error("Error saving inventory: {}", error.getMessage()));
    }

    private Inventory toDomain(InventoryEntity entity) {
        if (entity == null) {
            return null;
        }

        return Inventory.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .storeId(entity.getStoreId())
                .availableQty(entity.getAvailableQty())
                .version(entity.getVersion())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private InventoryEntity toEntity(Inventory domain) {
        if (domain == null) {
            return null;
        }

        return InventoryEntity.builder()
                .id(domain.getId())
                .productId(domain.getProductId())
                .storeId(domain.getStoreId())
                .availableQty(domain.getAvailableQty())
                .version(domain.getVersion())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
