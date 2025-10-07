package com.meli.inventorymanagement.infrastructure.adapter.output.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEntity {

    @Id
    private Long id;

    @Column("product_id")
    private Long productId;

    @Column("store_id")
    private Long storeId;

    @Column("available_qty")
    @Builder.Default
    private Integer availableQty = 0;

    @Version
    @Builder.Default
    private Integer version = 0;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}

