package com.meli.inventorymanagement.application.service;

import com.meli.inventorymanagement.application.dto.InventoryAdjustmentRequest;
import com.meli.inventorymanagement.application.dto.InventoryResponse;
import com.meli.inventorymanagement.application.dto.InventoryUpdateRequest;
import com.meli.inventorymanagement.application.mapper.InventoryMapper;
import com.meli.inventorymanagement.common.constant.ErrorCode;
import com.meli.inventorymanagement.domain.model.Inventory;
import com.meli.inventorymanagement.domain.model.Product;
import com.meli.inventorymanagement.domain.model.Store;
import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.InventoryRepository;
import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.ProductRepository;
import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.StoreRepository;
import com.meli.inventorymanagement.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final InventoryMapper inventoryMapper;

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByProductSku(String productSku) {
        log.info("Fetching inventory for product SKU: {}", productSku);

        // Validate SKU format
        if (productSku == null || productSku.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_SKU_FORMAT, "Product SKU cannot be null or empty");
        }

        try {
            Product product = productRepository.findBySku(productSku)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                            "Product with SKU " + productSku + " not found"));

            List<Inventory> inventories = inventoryRepository.findByProductSku(productSku);
            return inventoryMapper.toResponseList(inventories);
        } catch (DataAccessException e) {
            log.error("Database error while fetching inventory for SKU {}: {}", productSku, e.getMessage(), e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "Error accessing inventory data");
        }
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductSkuAndStore(String productSku, Long storeId) {
        log.info("Fetching inventory for product SKU: {} in store: {}", productSku, storeId);

        // Validate input parameters
        if (productSku == null || productSku.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_SKU_FORMAT, "Product SKU cannot be null or empty");
        }
        if (storeId == null || storeId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT, "Store ID must be a positive number");
        }

        try {
            validateStoreExists(storeId);

            Inventory inventory = inventoryRepository.findByProductSkuAndStoreId(productSku, storeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND,
                            String.format("Inventory not found for product %s in store %d", productSku, storeId)));

            return inventoryMapper.toResponse(inventory);
        } catch (BusinessException e) {
            throw e; // Re-throw business exceptions as-is
        } catch (DataAccessException e) {
            log.error("Database error while fetching inventory for SKU {} and store {}: {}",
                     productSku, storeId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "Error accessing inventory data");
        }
    }

    @Transactional
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public InventoryResponse updateInventory(String productSku, Long storeId, InventoryUpdateRequest request) {
        log.info("Updating inventory for product SKU: {} in store: {} with quantity: {}",
                productSku, storeId, request.getAvailableQty());

        // Validate input parameters
        if (productSku == null || productSku.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_SKU_FORMAT, "Product SKU cannot be null or empty");
        }
        if (storeId == null || storeId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT, "Store ID must be a positive number");
        }
        if (request.getAvailableQty() < 0) {
            throw new BusinessException(ErrorCode.NEGATIVE_QUANTITY_NOT_ALLOWED,
                    "Quantity cannot be negative: " + request.getAvailableQty());
        }

        try {
            Product product = productRepository.findBySku(productSku)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                            "Product with SKU " + productSku + " not found"));

            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND,
                            "Store with ID " + storeId + " not found"));

            Optional<Inventory> existingInventory = inventoryRepository
                    .findByProductIdAndStoreId(product.getId(), store.getId());

            Inventory inventory;
            if (existingInventory.isPresent()) {
                inventory = existingInventory.get();
                inventory.setAvailableQty(request.getAvailableQty());
                log.info("Updated existing inventory ID: {}", inventory.getId());
            } else {
                inventory = Inventory.builder()
                        .product(product)
                        .store(store)
                        .availableQty(request.getAvailableQty())
                        .build();
                log.info("Created new inventory entry");
            }

            Inventory savedInventory = inventoryRepository.save(inventory);
            return inventoryMapper.toResponse(savedInventory);

        } catch (BusinessException e) {
            throw e; // Re-throw business exceptions as-is
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic lock failure for SKU {} in store {}: {}", productSku, storeId, e.getMessage());
            throw e; // Let @Retryable handle this
        } catch (DataAccessException e) {
            log.error("Database error while updating inventory for SKU {} and store {}: {}",
                     productSku, storeId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INVENTORY_OPERATION_FAILED,
                    "Failed to update inventory due to database error");
        } catch (Exception e) {
            log.error("Unexpected error while updating inventory for SKU {} and store {}: {}",
                     productSku, storeId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INVENTORY_OPERATION_FAILED,
                    "Inventory update operation failed");
        }
    }

    @Transactional
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public InventoryResponse adjustInventory(String productSku, Long storeId, InventoryAdjustmentRequest request) {
        log.info("Adjusting inventory for product SKU: {} in store: {} by: {}",
                productSku, storeId, request.getAdjustment());

        // Validate input parameters
        if (productSku == null || productSku.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_SKU_FORMAT, "Product SKU cannot be null or empty");
        }
        if (storeId == null || storeId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT, "Store ID must be a positive number");
        }
        if (request.getAdjustment() == 0) {
            throw new BusinessException(ErrorCode.INVALID_ADJUSTMENT, "Adjustment value cannot be zero");
        }

        try {
            Inventory inventory = inventoryRepository.findByProductSkuAndStoreId(productSku, storeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND,
                            String.format("Inventory not found for product %s in store %d", productSku, storeId)));

            int newQuantity = inventory.getAvailableQty() + request.getAdjustment();

            if (newQuantity < 0) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                        String.format("Insufficient stock. Current: %d, Adjustment: %d, Result would be: %d",
                                inventory.getAvailableQty(), request.getAdjustment(), newQuantity));
            }

            inventory.setAvailableQty(newQuantity);
            Inventory savedInventory = inventoryRepository.save(inventory);

            log.info("Inventory adjusted successfully. New quantity: {}, Version: {}",
                    savedInventory.getAvailableQty(), savedInventory.getVersion());

            return inventoryMapper.toResponse(savedInventory);

        } catch (BusinessException e) {
            throw e; // Re-throw business exceptions as-is
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic lock failure for adjustment SKU {} in store {}: {}",
                    productSku, storeId, e.getMessage());
            throw e; // Let @Retryable handle this
        } catch (DataAccessException e) {
            log.error("Database error while adjusting inventory for SKU {} and store {}: {}",
                     productSku, storeId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INVENTORY_OPERATION_FAILED,
                    "Failed to adjust inventory due to database error");
        } catch (Exception e) {
            log.error("Unexpected error while adjusting inventory for SKU {} and store {}: {}",
                     productSku, storeId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INVENTORY_OPERATION_FAILED,
                    "Inventory adjustment operation failed");
        }
    }

    private void validateStoreExists(Long storeId) {
        try {
            if (!storeRepository.existsById(storeId)) {
                throw new BusinessException(ErrorCode.STORE_NOT_FOUND,
                        "Store with ID " + storeId + " not found");
            }
        } catch (DataAccessException e) {
            log.error("Database error while validating store {}: {}", storeId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "Error validating store existence");
        }
    }
}
