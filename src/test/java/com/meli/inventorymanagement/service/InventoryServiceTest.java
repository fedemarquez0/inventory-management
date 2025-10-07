package com.meli.inventorymanagement.service;

import com.meli.inventorymanagement.application.dto.InventoryAdjustmentRequest;
import com.meli.inventorymanagement.application.dto.InventoryResponse;
import com.meli.inventorymanagement.application.dto.InventoryUpdateRequest;
import com.meli.inventorymanagement.application.mapper.InventoryMapper;
import com.meli.inventorymanagement.application.service.InventoryService;
import com.meli.inventorymanagement.common.constant.ErrorCode;
import com.meli.inventorymanagement.domain.exception.BusinessException;
import com.meli.inventorymanagement.domain.model.Inventory;
import com.meli.inventorymanagement.domain.model.Product;
import com.meli.inventorymanagement.domain.model.Store;
import com.meli.inventorymanagement.domain.port.InventoryPort;
import com.meli.inventorymanagement.domain.port.ProductPort;
import com.meli.inventorymanagement.domain.port.StorePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryPort inventoryPort;

    @Mock
    private ProductPort productPort;

    @Mock
    private StorePort storePort;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private Store store;
    private Inventory inventory;
    private InventoryResponse inventoryResponse;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .sku("REM-001-BL-M")
                .name("Remera Básica Blanca M")
                .description("Remera de algodón peinado 160gsm")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        store = Store.builder()
                .id(1L)
                .name("Shopping Dinosaurio Mall")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        inventory = Inventory.builder()
                .id(1L)
                .productId(1L)
                .storeId(1L)
                .product(product)
                .store(store)
                .availableQty(25)
                .version(0)
                .updatedAt(LocalDateTime.now())
                .build();

        inventoryResponse = InventoryResponse.builder()
                .id(1L)
                .productSku("REM-001-BL-M")
                .productName("Remera Básica Blanca M")
                .storeId(1L)
                .storeName("Shopping Dinosaurio Mall")
                .availableQty(25)
                .version(0)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getInventoryByProductSku_Success() {
        // Given
        when(productPort.findBySku("REM-001-BL-M")).thenReturn(Mono.just(product));
        when(inventoryPort.findByProductSku("REM-001-BL-M")).thenReturn(Flux.just(inventory));
        when(productPort.findById(1L)).thenReturn(Mono.just(product));
        when(storePort.findById(1L)).thenReturn(Mono.just(store));
        when(inventoryMapper.toResponse(any(Inventory.class))).thenReturn(inventoryResponse);

        // When
        Flux<InventoryResponse> result = inventoryService.getInventoryByProductSku("REM-001-BL-M");

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getProductSku().equals("REM-001-BL-M") &&
                        response.getAvailableQty() == 25
                )
                .verifyComplete();

        verify(productPort).findBySku("REM-001-BL-M");
        verify(inventoryPort).findByProductSku("REM-001-BL-M");
    }

    @Test
    void getInventoryByProductSku_ProductNotFound() {
        // Given
        when(productPort.findBySku("INVALID-SKU")).thenReturn(Mono.empty());

        // When
        Flux<InventoryResponse> result = inventoryService.getInventoryByProductSku("INVALID-SKU");

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getErrorCode() == ErrorCode.PRODUCT_NOT_FOUND
                )
                .verify();

        verify(productPort).findBySku("INVALID-SKU");
        verify(inventoryPort, never()).findByProductSku(anyString());
    }

    @Test
    void getInventoryByProductSkuAndStore_Success() {
        // Given
        when(storePort.existsById(1L)).thenReturn(Mono.just(true));
        when(inventoryPort.findByProductSkuAndStoreId("REM-001-BL-M", 1L))
                .thenReturn(Mono.just(inventory));
        when(productPort.findById(1L)).thenReturn(Mono.just(product));
        when(storePort.findById(1L)).thenReturn(Mono.just(store));
        when(inventoryMapper.toResponse(any(Inventory.class))).thenReturn(inventoryResponse);

        // When
        Mono<InventoryResponse> result = inventoryService.getInventoryByProductSkuAndStore("REM-001-BL-M", 1L);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getProductSku().equals("REM-001-BL-M") &&
                        response.getStoreId() == 1L &&
                        response.getAvailableQty() == 25
                )
                .verifyComplete();

        verify(inventoryPort).findByProductSkuAndStoreId("REM-001-BL-M", 1L);
    }

    @Test
    void getInventoryByProductSkuAndStore_StoreNotFound() {
        // Given
        when(storePort.existsById(999L)).thenReturn(Mono.just(false));

        // When
        Mono<InventoryResponse> result = inventoryService.getInventoryByProductSkuAndStore("REM-001-BL-M", 999L);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getErrorCode() == ErrorCode.STORE_NOT_FOUND
                )
                .verify();

        verify(storePort).existsById(999L);
        verify(inventoryPort, never()).findByProductSkuAndStoreId(anyString(), anyLong());
    }

    @Test
    void updateInventory_CreateNew_Success() {
        // Given
        InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                .availableQty(30)
                .build();

        Inventory newInventory = Inventory.builder()
                .productId(1L)
                .storeId(1L)
                .availableQty(30)
                .version(0)
                .updatedAt(LocalDateTime.now())
                .build();

        when(productPort.findBySku("REM-001-BL-M")).thenReturn(Mono.just(product));
        when(storePort.findById(1L)).thenReturn(Mono.just(store));
        when(inventoryPort.findByProductIdAndStoreId(1L, 1L)).thenReturn(Mono.empty());
        when(inventoryPort.save(any(Inventory.class))).thenReturn(Mono.just(newInventory));
        when(productPort.findById(1L)).thenReturn(Mono.just(product));
        when(storePort.findById(1L)).thenReturn(Mono.just(store));
        when(inventoryMapper.toResponse(any(Inventory.class))).thenReturn(inventoryResponse);

        // When
        Mono<InventoryResponse> result = inventoryService.updateInventory("REM-001-BL-M", 1L, request);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getProductSku().equals("REM-001-BL-M"))
                .verifyComplete();

        verify(inventoryPort).save(any(Inventory.class));
    }

    @Test
    void adjustInventory_Success() {
        // Given
        InventoryAdjustmentRequest request = InventoryAdjustmentRequest.builder()
                .adjustment(5)
                .build();

        Inventory updatedInventory = Inventory.builder()
                .id(1L)
                .productId(1L)
                .storeId(1L)
                .availableQty(30)
                .version(1)
                .updatedAt(LocalDateTime.now())
                .build();

        when(inventoryPort.findByProductSkuAndStoreId("REM-001-BL-M", 1L))
                .thenReturn(Mono.just(inventory));
        when(inventoryPort.save(any(Inventory.class))).thenReturn(Mono.just(updatedInventory));
        when(productPort.findById(1L)).thenReturn(Mono.just(product));
        when(storePort.findById(1L)).thenReturn(Mono.just(store));
        when(inventoryMapper.toResponse(any(Inventory.class))).thenReturn(inventoryResponse);

        // When
        Mono<InventoryResponse> result = inventoryService.adjustInventory("REM-001-BL-M", 1L, request);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getProductSku().equals("REM-001-BL-M"))
                .verifyComplete();

        verify(inventoryPort).save(any(Inventory.class));
    }

    @Test
    void adjustInventory_InsufficientStock() {
        // Given
        InventoryAdjustmentRequest request = InventoryAdjustmentRequest.builder()
                .adjustment(-30)
                .build();

        when(inventoryPort.findByProductSkuAndStoreId("REM-001-BL-M", 1L))
                .thenReturn(Mono.just(inventory));

        // When
        Mono<InventoryResponse> result = inventoryService.adjustInventory("REM-001-BL-M", 1L, request);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getErrorCode() == ErrorCode.INSUFFICIENT_STOCK
                )
                .verify();

        verify(inventoryPort, never()).save(any());
    }

    @Test
    void adjustInventory_InventoryNotFound() {
        // Given
        InventoryAdjustmentRequest request = InventoryAdjustmentRequest.builder()
                .adjustment(5)
                .build();

        when(inventoryPort.findByProductSkuAndStoreId("INVALID-SKU", 1L))
                .thenReturn(Mono.empty());

        // When
        Mono<InventoryResponse> result = inventoryService.adjustInventory("INVALID-SKU", 1L, request);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getErrorCode() == ErrorCode.INVENTORY_NOT_FOUND
                )
                .verify();

        verify(inventoryPort, never()).save(any());
    }

    @Test
    void updateInventory_NegativeQuantity_ShouldFail() {
        // Given
        InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                .availableQty(-5)
                .build();

        // When
        Mono<InventoryResponse> result = inventoryService.updateInventory("REM-001-BL-M", 1L, request);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getErrorCode() == ErrorCode.NEGATIVE_QUANTITY_NOT_ALLOWED
                )
                .verify();

        verify(inventoryPort, never()).save(any());
    }
}
