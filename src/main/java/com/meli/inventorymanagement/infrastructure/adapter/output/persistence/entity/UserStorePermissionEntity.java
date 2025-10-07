package com.meli.inventorymanagement.infrastructure.adapter.output.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_store_permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStorePermissionEntity {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("store_id")
    private Long storeId;
}

