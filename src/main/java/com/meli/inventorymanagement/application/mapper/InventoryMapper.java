package com.meli.inventorymanagement.application.mapper;

import com.meli.inventorymanagement.application.dto.InventoryResponse;
import com.meli.inventorymanagement.domain.model.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryResponse toResponse(Inventory inventory) {
        if (inventory == null) {
            return null;
        }

        return InventoryResponse.builder()
                .id(inventory.getId())
                .productSku(inventory.getProduct() != null ? inventory.getProduct().getSku() : null)
                .productName(inventory.getProduct() != null ? inventory.getProduct().getName() : null)
                .storeId(inventory.getStore() != null ? inventory.getStore().getId() : null)
                .storeName(inventory.getStore() != null ? inventory.getStore().getName() : null)
                .availableQty(inventory.getAvailableQty())
                .version(inventory.getVersion())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
