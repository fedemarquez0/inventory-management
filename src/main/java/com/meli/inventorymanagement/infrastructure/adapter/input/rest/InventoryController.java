package com.meli.inventorymanagement.infrastructure.adapter.input.rest;

import com.meli.inventorymanagement.application.dto.InventoryAdjustmentRequest;
import com.meli.inventorymanagement.application.dto.InventoryResponse;
import com.meli.inventorymanagement.application.dto.InventoryUpdateRequest;
import com.meli.inventorymanagement.application.service.InventoryService;
import com.meli.inventorymanagement.infrastructure.security.RequireStorePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Inventory", description = "Inventory Management API")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Get product inventory in all stores",
            description = "Returns the stock of a product across all stores - Admin only")
    @GetMapping("/{productSku}/stores")
    @RequireStorePermission(adminOnly = true)
    public ResponseEntity<List<InventoryResponse>> getInventoryByProduct(
            @PathVariable String productSku,
            HttpServletRequest request) {

        String username = getCurrentUsername();
        String clientIp = getClientIpAddress(request);

        log.info("GET /api/inventory/{}/stores - User: {} - IP: {} - Admin inventory query for all stores",
                productSku, username, clientIp);

        try {
            List<InventoryResponse> response = inventoryService.getInventoryByProductSku(productSku);

            log.info("Successfully retrieved inventory for product {} across {} stores - User: {} - IP: {}",
                    productSku, response.size(), username, clientIp);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to retrieve inventory for product {} - User: {} - IP: {} - Error: {}",
                     productSku, username, clientIp, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Get product inventory in specific store",
            description = "Returns the stock of a product in a specific store")
    @GetMapping("/{productSku}/stores/{storeId}")
    @RequireStorePermission(storeIdParam = "storeId")
    public ResponseEntity<InventoryResponse> getInventoryByProductAndStore(
            @PathVariable String productSku,
            @PathVariable Long storeId,
            HttpServletRequest request) {

        String username = getCurrentUsername();
        String clientIp = getClientIpAddress(request);

        log.info("GET /api/inventory/{}/stores/{} - User: {} - IP: {} - Store-specific inventory query",
                productSku, storeId, username, clientIp);

        try {
            InventoryResponse response = inventoryService.getInventoryByProductSkuAndStore(productSku, storeId);

            log.info("Successfully retrieved inventory for product {} in store {} - Quantity: {} - User: {} - IP: {}",
                    productSku, storeId, response.getAvailableQty(), username, clientIp);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to retrieve inventory for product {} in store {} - User: {} - IP: {} - Error: {}",
                     productSku, storeId, username, clientIp, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Set absolute stock quantity",
            description = "Sets the absolute stock quantity for a product in a specific store")
    @PutMapping("/{productSku}/stores/{storeId}")
    @RequireStorePermission(storeIdParam = "storeId")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable String productSku,
            @PathVariable Long storeId,
            @Valid @RequestBody InventoryUpdateRequest request,
            HttpServletRequest httpRequest) {

        String username = getCurrentUsername();
        String clientIp = getClientIpAddress(httpRequest);

        log.info("PUT /api/inventory/{}/stores/{} - User: {} - IP: {} - Setting absolute quantity to: {}",
                productSku, storeId, username, clientIp, request.getAvailableQty());

        try {
            InventoryResponse response = inventoryService.updateInventory(productSku, storeId, request);

            log.info("Successfully updated inventory for product {} in store {} - New quantity: {} - User: {} - IP: {}",
                    productSku, storeId, response.getAvailableQty(), username, clientIp);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update inventory for product {} in store {} to quantity {} - User: {} - IP: {} - Error: {}",
                     productSku, storeId, request.getAvailableQty(), username, clientIp, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Adjust inventory quantity",
            description = "Adjusts the inventory quantity (positive for additions, negative for sales)")
    @PostMapping("/{productSku}/stores/{storeId}/adjustments")
    @RequireStorePermission(storeIdParam = "storeId")
    public ResponseEntity<InventoryResponse> adjustInventory(
            @PathVariable String productSku,
            @PathVariable Long storeId,
            @Valid @RequestBody InventoryAdjustmentRequest request,
            HttpServletRequest httpRequest) {

        String username = getCurrentUsername();
        String clientIp = getClientIpAddress(httpRequest);

        log.info("POST /api/inventory/{}/stores/{}/adjustments - User: {} - IP: {} - Adjusting by: {}",
                productSku, storeId, username, clientIp, request.getAdjustment());

        try {
            InventoryResponse response = inventoryService.adjustInventory(productSku, storeId, request);

            log.info("Successfully adjusted inventory for product {} in store {} by {} - Final quantity: {} - User: {} - IP: {}",
                    productSku, storeId, request.getAdjustment(), response.getAvailableQty(), username, clientIp);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to adjust inventory for product {} in store {} by {} - User: {} - IP: {} - Error: {}",
                     productSku, storeId, request.getAdjustment(), username, clientIp, e.getMessage());
            throw e;
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymous";
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
