package com.meli.inventorymanagement.infrastructure.adapter.output.persistence;

import com.meli.inventorymanagement.domain.model.Product;
import com.meli.inventorymanagement.domain.port.ProductPort;
import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.entity.ProductEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductPort {

    private final ProductRepository productRepository;

    @Override
    public Mono<Product> findById(Long id) {
        if (id == null) {
            return Mono.empty();
        }

        return productRepository.findById(id)
                .map(this::toDomain)
                .doOnError(error -> log.error("Error finding product by ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Product> findBySku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            return Mono.empty();
        }

        return productRepository.findBySku(sku)
                .map(this::toDomain)
                .doOnError(error -> log.error("Error finding product by SKU {}: {}", sku, error.getMessage()));
    }

    private Product toDomain(ProductEntity entity) {
        if (entity == null) {
            return null;
        }

        return Product.builder()
                .id(entity.getId())
                .sku(entity.getSku())
                .name(entity.getName())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
