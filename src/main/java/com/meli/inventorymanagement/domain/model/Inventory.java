package com.meli.inventorymanagement.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    private Long id;
    private Long productId;
    private Long storeId;
    @Builder.Default
    private Integer availableQty = 0;
    @Builder.Default
    private Integer version = 0;
    private LocalDateTime updatedAt;

    // Relaciones
    private Product product;
    private Store store;
}
