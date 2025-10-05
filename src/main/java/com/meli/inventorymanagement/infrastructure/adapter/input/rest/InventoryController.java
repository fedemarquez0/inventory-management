package com.meli.inventorymanagement.infrastructure.adapter.input.rest;

import com.meli.inventorymanagement.application.dto.InventoryAdjustmentRequest;
import com.meli.inventorymanagement.application.dto.InventoryResponse;
import com.meli.inventorymanagement.application.dto.InventoryUpdateRequest;
import com.meli.inventorymanagement.application.service.InventoryService;
import com.meli.inventorymanagement.infrastructure.security.RequireStorePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Inventory", description = "Inventory Management API")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Get product inventory in all stores",
            description = "Returns the stock of a product across all stores - Admin only")
    @GetMapping("/{productSku}/stores")
    @RequireStorePermission(adminOnly = true)
    public ResponseEntity<List<InventoryResponse>> getInventoryByProduct(
            @PathVariable String productSku) {
        List<InventoryResponse> response = inventoryService.getInventoryByProductSku(productSku);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get product inventory in specific store",
            description = "Returns the stock of a product in a specific store")
    @GetMapping("/{productSku}/stores/{storeId}")
    @RequireStorePermission(storeIdParam = "storeId")
    public ResponseEntity<InventoryResponse> getInventoryByProductAndStore(
            @PathVariable String productSku,
            @PathVariable Long storeId) {
        InventoryResponse response = inventoryService.getInventoryByProductSkuAndStore(productSku, storeId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Set absolute stock quantity",
            description = "Sets the absolute stock quantity for a product in a specific store")
    @PutMapping("/{productSku}/stores/{storeId}")
    @RequireStorePermission(storeIdParam = "storeId")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable String productSku,
            @PathVariable Long storeId,
            @Valid @RequestBody InventoryUpdateRequest request) {
        InventoryResponse response = inventoryService.updateInventory(productSku, storeId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Adjust inventory quantity",
            description = "Adjusts the inventory quantity (positive for additions, negative for sales)")
    @PostMapping("/{productSku}/stores/{storeId}/adjustments")
    @RequireStorePermission(storeIdParam = "storeId")
    public ResponseEntity<InventoryResponse> adjustInventory(
            @PathVariable String productSku,
            @PathVariable Long storeId,
            @Valid @RequestBody InventoryAdjustmentRequest request) {
        InventoryResponse response = inventoryService.adjustInventory(productSku, storeId, request);
        return ResponseEntity.ok(response);
    }
}
