package com.meli.inventorymanagement.application.service;

import com.meli.inventorymanagement.application.dto.InventoryAdjustmentRequest;
import com.meli.inventorymanagement.application.dto.InventoryResponse;
import com.meli.inventorymanagement.application.dto.InventoryUpdateRequest;
import com.meli.inventorymanagement.application.mapper.InventoryMapper;
import com.meli.inventorymanagement.common.constant.ErrorCode;
import com.meli.inventorymanagement.domain.exception.BusinessException;
import com.meli.inventorymanagement.domain.model.Inventory;
import com.meli.inventorymanagement.domain.model.Product;
import com.meli.inventorymanagement.domain.model.Store;
import com.meli.inventorymanagement.domain.port.InventoryPort;
import com.meli.inventorymanagement.domain.port.ProductPort;
import com.meli.inventorymanagement.domain.port.StorePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryPort inventoryPort;
    private final ProductPort productPort;
    private final StorePort storePort;
    private final InventoryMapper inventoryMapper;

    public Flux<InventoryResponse> getInventoryByProductSku(String productSku) {
        log.info("Fetching inventory for product SKU: {}", productSku);

        // Validate SKU format
        if (productSku == null || productSku.trim().isEmpty()) {
            return Flux.error(new BusinessException(ErrorCode.INVALID_SKU_FORMAT, "Product SKU cannot be null or empty"));
        }

        return productPort.findBySku(productSku)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                        "Product with SKU " + productSku + " not found")))
                .flatMapMany(product -> inventoryPort.findByProductSku(productSku))
                .flatMap(this::enrichInventoryWithRelations)
                .map(inventoryMapper::toResponse)
                .doOnError(error -> log.error("Database error while fetching inventory for SKU {}: {}",
                        productSku, error.getMessage(), error))
                .onErrorMap(ex -> !(ex instanceof BusinessException),
                        ex -> new BusinessException(ErrorCode.DATABASE_ERROR, "Error accessing inventory data"));
    }

    public Mono<InventoryResponse> getInventoryByProductSkuAndStore(String productSku, Long storeId) {
        log.info("Fetching inventory for product SKU: {} in store: {}", productSku, storeId);

        // Validate input parameters
        if (productSku == null || productSku.trim().isEmpty()) {
            return Mono.error(new BusinessException(ErrorCode.INVALID_SKU_FORMAT, "Product SKU cannot be null or empty"));
        }
        if (storeId == null || storeId <= 0) {
            return Mono.error(new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT, "Store ID must be a positive number"));
        }

        return validateStoreExists(storeId)
                .then(Mono.defer(() -> inventoryPort.findByProductSkuAndStoreId(productSku, storeId)))
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.INVENTORY_NOT_FOUND,
                        String.format("Inventory not found for product %s in store %d", productSku, storeId))))
                .flatMap(this::enrichInventoryWithRelations)
                .map(inventoryMapper::toResponse)
                .doOnError(error -> log.error("Database error while fetching inventory for SKU {} and store {}: {}",
                        productSku, storeId, error.getMessage(), error))
                .onErrorMap(ex -> !(ex instanceof BusinessException),
                        ex -> new BusinessException(ErrorCode.DATABASE_ERROR, "Error accessing inventory data"));
    }

    public Mono<InventoryResponse> updateInventory(String productSku, Long storeId, InventoryUpdateRequest request) {
        log.info("Updating inventory for product SKU: {} in store: {} with quantity: {}",
                productSku, storeId, request.getAvailableQty());

        // Validate input parameters
        if (productSku == null || productSku.trim().isEmpty()) {
            return Mono.error(new BusinessException(ErrorCode.INVALID_SKU_FORMAT, "Product SKU cannot be null or empty"));
        }
        if (storeId == null || storeId <= 0) {
            return Mono.error(new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT, "Store ID must be a positive number"));
        }
        if (request.getAvailableQty() < 0) {
            return Mono.error(new BusinessException(ErrorCode.NEGATIVE_QUANTITY_NOT_ALLOWED,
                    "Quantity cannot be negative: " + request.getAvailableQty()));
        }

        return Mono.zip(
                productPort.findBySku(productSku)
                        .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                                "Product with SKU " + productSku + " not found"))),
                storePort.findById(storeId)
                        .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.STORE_NOT_FOUND,
                                "Store with ID " + storeId + " not found")))
        )
        .flatMap(tuple -> {
            Product product = tuple.getT1();
            Store store = tuple.getT2();

            return inventoryPort.findByProductIdAndStoreId(product.getId(), store.getId())
                    .flatMap(existingInventory -> {
                        existingInventory.setAvailableQty(request.getAvailableQty());
                        existingInventory.setUpdatedAt(LocalDateTime.now());
                        log.info("Updating existing inventory ID: {}", existingInventory.getId());
                        return inventoryPort.save(existingInventory);
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        Inventory newInventory = Inventory.builder()
                                .productId(product.getId())
                                .storeId(store.getId())
                                .availableQty(request.getAvailableQty())
                                .updatedAt(LocalDateTime.now())
                                .build();
                        log.info("Creating new inventory entry");
                        return inventoryPort.save(newInventory);
                    }));
        })
        .flatMap(this::enrichInventoryWithRelations)
        .map(inventoryMapper::toResponse)
        .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                .filter(throwable -> throwable instanceof OptimisticLockingFailureException)
                .doBeforeRetry(signal -> log.warn("Optimistic lock failure, retrying... attempt: {}",
                        signal.totalRetries() + 1)))
        .doOnError(error -> log.error("Error updating inventory for SKU {} and store {}: {}",
                productSku, storeId, error.getMessage(), error))
        .onErrorMap(ex -> !(ex instanceof BusinessException),
                ex -> new BusinessException(ErrorCode.INVENTORY_OPERATION_FAILED,
                        "Failed to update inventory due to database error"));
    }

    public Mono<InventoryResponse> adjustInventory(String productSku, Long storeId, InventoryAdjustmentRequest request) {
        log.info("Adjusting inventory for product SKU: {} in store: {} by: {}",
                productSku, storeId, request.getAdjustment());

        // Validate input parameters
        if (productSku == null || productSku.trim().isEmpty()) {
            return Mono.error(new BusinessException(ErrorCode.INVALID_SKU_FORMAT, "Product SKU cannot be null or empty"));
        }
        if (storeId == null || storeId <= 0) {
            return Mono.error(new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT, "Store ID must be a positive number"));
        }
        if (request.getAdjustment() == 0) {
            return Mono.error(new BusinessException(ErrorCode.INVALID_ADJUSTMENT, "Adjustment value cannot be zero"));
        }

        return inventoryPort.findByProductSkuAndStoreId(productSku, storeId)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.INVENTORY_NOT_FOUND,
                        String.format("Inventory not found for product %s in store %d", productSku, storeId))))
                .flatMap(inventory -> {
                    int newQuantity = inventory.getAvailableQty() + request.getAdjustment();

                    if (newQuantity < 0) {
                        return Mono.error(new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                                String.format("Insufficient stock. Current: %d, Adjustment: %d, Result would be: %d",
                                        inventory.getAvailableQty(), request.getAdjustment(), newQuantity)));
                    }

                    inventory.setAvailableQty(newQuantity);
                    inventory.setUpdatedAt(LocalDateTime.now());
                    return inventoryPort.save(inventory)
                            .doOnSuccess(saved -> log.info("Inventory adjusted successfully. New quantity: {}, Version: {}",
                                    saved.getAvailableQty(), saved.getVersion()));
                })
                .flatMap(this::enrichInventoryWithRelations)
                .map(inventoryMapper::toResponse)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                        .filter(throwable -> throwable instanceof OptimisticLockingFailureException)
                        .doBeforeRetry(signal -> log.warn("Optimistic lock failure on adjustment, retrying... attempt: {}",
                                signal.totalRetries() + 1)))
                .doOnError(error -> log.error("Error adjusting inventory for SKU {} and store {}: {}",
                        productSku, storeId, error.getMessage(), error))
                .onErrorMap(ex -> !(ex instanceof BusinessException),
                        ex -> new BusinessException(ErrorCode.INVENTORY_OPERATION_FAILED,
                                "Failed to adjust inventory due to database error"));
    }

    private Mono<Void> validateStoreExists(Long storeId) {
        return storePort.existsById(storeId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new BusinessException(ErrorCode.STORE_NOT_FOUND,
                                "Store with ID " + storeId + " not found"));
                    }
                    return Mono.empty();
                })
                .then()
                .onErrorResume(ex -> {
                    if (ex instanceof BusinessException) {
                        return Mono.error(ex);
                    }
                    return Mono.error(new BusinessException(ErrorCode.DATABASE_ERROR, "Error validating store existence"));
                });
    }

    private Mono<Inventory> enrichInventoryWithRelations(Inventory inventory) {
        return Mono.zip(
                productPort.findById(inventory.getProductId()).defaultIfEmpty(new Product()),
                storePort.findById(inventory.getStoreId()).defaultIfEmpty(new Store())
        )
        .map(tuple -> {
            inventory.setProduct(tuple.getT1());
            inventory.setStore(tuple.getT2());
            return inventory;
        });
    }
}
