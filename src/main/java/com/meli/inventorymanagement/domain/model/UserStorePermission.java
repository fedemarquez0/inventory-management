package com.meli.inventorymanagement.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStorePermission {

    private Long id;
    private Long userId;
    private Long storeId;
}
