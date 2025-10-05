package com.meli.inventorymanagement.controller;

import com.meli.inventorymanagement.application.dto.InventoryAdjustmentRequest;
import com.meli.inventorymanagement.application.dto.InventoryResponse;
import com.meli.inventorymanagement.application.dto.InventoryUpdateRequest;
import com.meli.inventorymanagement.application.service.InventoryService;
import com.meli.inventorymanagement.infrastructure.adapter.input.rest.InventoryController;
import com.meli.inventorymanagement.infrastructure.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@WebFluxTest(
    controllers = InventoryController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@Import(TestSecurityConfig.class)
class InventoryControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private InventoryService inventoryService;

    private InventoryResponse inventoryResponse;

    @BeforeEach
    void setUp() {
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

        // Configuración por defecto para evitar NPE en tests sin mock específico
        when(inventoryService.getInventoryByProductSku(anyString()))
                .thenReturn(Flux.just(inventoryResponse));
    }

    @Test
    void getInventoryByProduct_Success() {
        // Given
        when(inventoryService.getInventoryByProductSku("REM-001-BL-M"))
                .thenReturn(Flux.just(inventoryResponse));

        // When & Then
        webTestClient
                .mutateWith(mockUser().roles("ADMIN"))
                .get()
                .uri("/api/inventory/REM-001-BL-M/stores")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponse.class)
                .hasSize(1)
                .contains(inventoryResponse);
    }

    @Test
    void getInventoryByProductAndStore_Success() {
        // Given
        when(inventoryService.getInventoryByProductSkuAndStore("REM-001-BL-M", 1L))
                .thenReturn(Mono.just(inventoryResponse));

        // When & Then
        webTestClient
                .mutateWith(mockUser().roles("STORE_USER"))
                .get()
                .uri("/api/inventory/REM-001-BL-M/stores/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InventoryResponse.class)
                .isEqualTo(inventoryResponse);
    }

    @Test
    void updateInventory_Success() {
        // Given
        InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                .availableQty(30)
                .build();

        InventoryResponse updatedResponse = InventoryResponse.builder()
                .id(1L)
                .productSku("REM-001-BL-M")
                .productName("Remera Básica Blanca M")
                .storeId(1L)
                .storeName("Shopping Dinosaurio Mall")
                .availableQty(30)
                .version(1)
                .updatedAt(LocalDateTime.now())
                .build();

        when(inventoryService.updateInventory(anyString(), anyLong(), any(InventoryUpdateRequest.class)))
                .thenReturn(Mono.just(updatedResponse));

        // When & Then
        webTestClient
                .mutateWith(mockUser().roles("STORE_USER"))
                .put()
                .uri("/api/inventory/REM-001-BL-M/stores/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InventoryResponse.class)
                .isEqualTo(updatedResponse);
    }

    @Test
    void adjustInventory_Success() {
        // Given
        InventoryAdjustmentRequest request = InventoryAdjustmentRequest.builder()
                .adjustment(5)
                .build();

        InventoryResponse adjustedResponse = InventoryResponse.builder()
                .id(1L)
                .productSku("REM-001-BL-M")
                .productName("Remera Básica Blanca M")
                .storeId(1L)
                .storeName("Shopping Dinosaurio Mall")
                .availableQty(30)
                .version(1)
                .updatedAt(LocalDateTime.now())
                .build();

        when(inventoryService.adjustInventory(anyString(), anyLong(), any(InventoryAdjustmentRequest.class)))
                .thenReturn(Mono.just(adjustedResponse));

        // When & Then
        webTestClient
                .mutateWith(mockUser().roles("STORE_USER"))
                .post()
                .uri("/api/inventory/REM-001-BL-M/stores/1/adjustments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InventoryResponse.class)
                .isEqualTo(adjustedResponse);
    }

    @Test
    void updateInventory_ValidationError_NegativeQuantity() {
        // Given
        InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                .availableQty(-5)
                .build();

        // When & Then
        webTestClient
                .mutateWith(mockUser().roles("STORE_USER"))
                .put()
                .uri("/api/inventory/REM-001-BL-M/stores/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getInventoryByProduct_Unauthorized() {
        // When & Then
        webTestClient.get()
                .uri("/api/inventory/REM-001-BL-M/stores")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
