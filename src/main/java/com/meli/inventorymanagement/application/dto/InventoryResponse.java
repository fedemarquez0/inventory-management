package com.meli.inventorymanagement.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    private String productSku;
    private String productName;
    private Long storeId;
    private String storeName;
    private Integer availableQty;
    private Integer version;
    private LocalDateTime updatedAt;
}