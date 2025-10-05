package com.meli.inventorymanagement.service;

import com.meli.inventorymanagement.application.dto.InventoryAdjustmentRequest;
import com.meli.inventorymanagement.application.dto.InventoryResponse;
import com.meli.inventorymanagement.application.dto.InventoryUpdateRequest;
import com.meli.inventorymanagement.application.mapper.InventoryMapper;
import com.meli.inventorymanagement.application.service.InventoryService;
import com.meli.inventorymanagement.common.constant.ErrorCode;
import com.meli.inventorymanagement.domain.model.Inventory;
import com.meli.inventorymanagement.domain.model.Product;
import com.meli.inventorymanagement.domain.model.Store;
import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.InventoryRepository;
import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.ProductRepository;
import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.StoreRepository;
import com.meli.inventorymanagement.infrastructure.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoreRepository storeRepository;

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
                .sku("LAPTOP-001")
                .name("Gaming Laptop")
                .description("High performance gaming laptop")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        store = Store.builder()
                .id(1L)
                .name("Store Downtown")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        inventory = Inventory.builder()
                .id(1L)
                .product(product)
                .store(store)
                .availableQty(10)
                .version(0)
                .updatedAt(LocalDateTime.now())
                .build();

        inventoryResponse = InventoryResponse.builder()
                .id(1L)
                .productSku("LAPTOP-001")
                .productName("Gaming Laptop")
                .storeId(1L)
                .storeName("Store Downtown")
                .availableQty(10)
                .version(0)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getInventoryByProductSku_Success() {
        when(productRepository.findBySku("LAPTOP-001")).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductSku("LAPTOP-001")).thenReturn(Arrays.asList(inventory));
        when(inventoryMapper.toResponseList(any())).thenReturn(Arrays.asList(inventoryResponse));

        List<InventoryResponse> result = inventoryService.getInventoryByProductSku("LAPTOP-001");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("LAPTOP-001", result.get(0).getProductSku());
        verify(productRepository).findBySku("LAPTOP-001");
        verify(inventoryRepository).findByProductSku("LAPTOP-001");
    }

    @Test
    void getInventoryByProductSku_ProductNotFound() {
        when(productRepository.findBySku("INVALID-SKU")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> inventoryService.getInventoryByProductSku("INVALID-SKU"));

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
        verify(productRepository).findBySku("INVALID-SKU");
        verify(inventoryRepository, never()).findByProductSku(anyString());
    }

    @Test
    void getInventoryByProductSkuAndStore_Success() {
        when(storeRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductSkuAndStoreId("LAPTOP-001", 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryMapper.toResponse(inventory)).thenReturn(inventoryResponse);

        InventoryResponse result = inventoryService.getInventoryByProductSkuAndStore("LAPTOP-001", 1L);

        assertNotNull(result);
        assertEquals("LAPTOP-001", result.getProductSku());
        assertEquals(1L, result.getStoreId());
        verify(inventoryRepository).findByProductSkuAndStoreId("LAPTOP-001", 1L);
    }

    @Test
    void getInventoryByProductSkuAndStore_StoreNotFound() {
        when(storeRepository.existsById(999L)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> inventoryService.getInventoryByProductSkuAndStore("LAPTOP-001", 999L));

        assertEquals(ErrorCode.STORE_NOT_FOUND, exception.getErrorCode());
        verify(storeRepository).existsById(999L);
        verify(inventoryRepository, never()).findByProductSkuAndStoreId(anyString(), anyLong());
    }

    @Test
    void updateInventory_Success() {
        InventoryUpdateRequest request = new InventoryUpdateRequest(20);

        when(productRepository.findBySku("LAPTOP-001")).thenReturn(Optional.of(product));
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(inventoryRepository.findByProductIdAndStoreId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMapper.toResponse(inventory)).thenReturn(inventoryResponse);

        InventoryResponse result = inventoryService.updateInventory("LAPTOP-001", 1L, request);

        assertNotNull(result);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void adjustInventory_Success() {
        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest(5);

        when(inventoryRepository.findByProductSkuAndStoreId("LAPTOP-001", 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMapper.toResponse(inventory)).thenReturn(inventoryResponse);

        InventoryResponse result = inventoryService.adjustInventory("LAPTOP-001", 1L, request);

        assertNotNull(result);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void adjustInventory_InsufficientStock() {
        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest(-20);

        when(inventoryRepository.findByProductSkuAndStoreId("LAPTOP-001", 1L))
                .thenReturn(Optional.of(inventory));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> inventoryService.adjustInventory("LAPTOP-001", 1L, request));

        assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void adjustInventory_InventoryNotFound() {
        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest(5);

        when(inventoryRepository.findByProductSkuAndStoreId("LAPTOP-001", 999L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> inventoryService.adjustInventory("LAPTOP-001", 999L, request));

        assertEquals(ErrorCode.INVENTORY_NOT_FOUND, exception.getErrorCode());
        verify(inventoryRepository, never()).save(any());
    }
}
