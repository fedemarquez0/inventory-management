package com.meli.inventorymanagement.application.mapper;

import com.meli.inventorymanagement.application.dto.InventoryResponse;
import com.meli.inventorymanagement.domain.model.Inventory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InventoryMapper {

    public InventoryResponse toResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productSku(inventory.getProduct().getSku())
                .productName(inventory.getProduct().getName())
                .storeId(inventory.getStore().getId())
                .storeName(inventory.getStore().getName())
                .availableQty(inventory.getAvailableQty())
                .version(inventory.getVersion())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    public List<InventoryResponse> toResponseList(List<Inventory> inventories) {
        return inventories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
