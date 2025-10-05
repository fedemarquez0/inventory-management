package com.meli.inventorymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meli.inventorymanagement.application.dto.InventoryAdjustmentRequest;
import com.meli.inventorymanagement.application.dto.InventoryResponse;
import com.meli.inventorymanagement.application.dto.InventoryUpdateRequest;
import com.meli.inventorymanagement.application.service.InventoryService;
import com.meli.inventorymanagement.infrastructure.adapter.input.rest.InventoryController;
import com.meli.inventorymanagement.infrastructure.config.SecurityConfig;
import com.meli.inventorymanagement.infrastructure.security.CustomUserDetailsService;
import com.meli.inventorymanagement.infrastructure.security.JwtAuthenticationFilter;
import com.meli.inventorymanagement.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
@Import(SecurityConfig.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private InventoryResponse inventoryResponse;

    @BeforeEach
    void setUp() {
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
    @WithMockUser
    void getInventoryByProduct_Success() throws Exception {
        when(inventoryService.getInventoryByProductSku("LAPTOP-001"))
                .thenReturn(Arrays.asList(inventoryResponse));

        mockMvc.perform(get("/api/inventory/LAPTOP-001/stores")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productSku").value("LAPTOP-001"))
                .andExpect(jsonPath("$[0].availableQty").value(10));
    }

    @Test
    @WithMockUser
    void getInventoryByProductAndStore_Success() throws Exception {
        when(inventoryService.getInventoryByProductSkuAndStore("LAPTOP-001", 1L))
                .thenReturn(inventoryResponse);

        mockMvc.perform(get("/api/inventory/LAPTOP-001/stores/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productSku").value("LAPTOP-001"))
                .andExpect(jsonPath("$.storeId").value(1))
                .andExpect(jsonPath("$.availableQty").value(10));
    }

    @Test
    @WithMockUser
    void updateInventory_Success() throws Exception {
        InventoryUpdateRequest request = new InventoryUpdateRequest(20);

        when(inventoryService.updateInventory(anyString(), anyLong(), any(InventoryUpdateRequest.class)))
                .thenReturn(inventoryResponse);

        mockMvc.perform(put("/api/inventory/LAPTOP-001/stores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productSku").value("LAPTOP-001"));
    }

    @Test
    @WithMockUser
    void adjustInventory_Success() throws Exception {
        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest(5);

        when(inventoryService.adjustInventory(anyString(), anyLong(), any(InventoryAdjustmentRequest.class)))
                .thenReturn(inventoryResponse);

        mockMvc.perform(post("/api/inventory/LAPTOP-001/stores/1/adjustments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productSku").value("LAPTOP-001"));
    }

    @Test
    @WithMockUser
    void updateInventory_ValidationError_NegativeQuantity() throws Exception {
        InventoryUpdateRequest request = new InventoryUpdateRequest(-5);

        mockMvc.perform(put("/api/inventory/LAPTOP-001/stores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
