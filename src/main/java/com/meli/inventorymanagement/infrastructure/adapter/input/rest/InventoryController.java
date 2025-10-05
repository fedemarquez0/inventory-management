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
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    public Flux<InventoryResponse> getInventoryByProduct(
            @PathVariable String productSku,
            ServerWebExchange exchange) {

        return exchange.getPrincipal()
                .map(principal -> principal.getName())
                .defaultIfEmpty("anonymous")
                .flatMapMany(username -> {
                    String clientIp = getClientIpAddress(exchange);

                    log.info("GET /api/inventory/{}/stores - User: {} - IP: {} - Admin inventory query for all stores",
                            productSku, username, clientIp);

                    return inventoryService.getInventoryByProductSku(productSku)
                            .doOnComplete(() -> log.info("Successfully retrieved inventory for product {} - User: {} - IP: {}",
                                    productSku, username, clientIp))
                            .doOnError(e -> log.error("Failed to retrieve inventory for product {} - User: {} - IP: {} - Error: {}",
                                    productSku, username, clientIp, e.getMessage()));
                });
    }

    @Operation(summary = "Get product inventory in specific store",
            description = "Returns the stock of a product in a specific store")
    @GetMapping("/{productSku}/stores/{storeId}")
    @RequireStorePermission
    public Mono<InventoryResponse> getInventoryByProductAndStore(
            @PathVariable String productSku,
            @PathVariable Long storeId,
            ServerWebExchange exchange) {

        return exchange.getPrincipal()
                .map(principal -> principal.getName())
                .defaultIfEmpty("anonymous")
                .flatMap(username -> {
                    String clientIp = getClientIpAddress(exchange);

                    log.info("GET /api/inventory/{}/stores/{} - User: {} - IP: {} - Store-specific inventory query",
                            productSku, storeId, username, clientIp);

                    return inventoryService.getInventoryByProductSkuAndStore(productSku, storeId)
                            .doOnSuccess(response -> log.info("Successfully retrieved inventory for product {} in store {} - Quantity: {} - User: {} - IP: {}",
                                    productSku, storeId, response.getAvailableQty(), username, clientIp))
                            .doOnError(e -> log.error("Failed to retrieve inventory for product {} in store {} - User: {} - IP: {} - Error: {}",
                                    productSku, storeId, username, clientIp, e.getMessage()));
                });
    }

    @Operation(summary = "Set absolute stock quantity",
            description = "Sets the absolute stock quantity for a product in a specific store")
    @PutMapping("/{productSku}/stores/{storeId}")
    @RequireStorePermission
    public Mono<InventoryResponse> updateInventory(
            @PathVariable String productSku,
            @PathVariable Long storeId,
            @Valid @RequestBody InventoryUpdateRequest request,
            ServerWebExchange exchange) {

        return exchange.getPrincipal()
                .map(principal -> principal.getName())
                .defaultIfEmpty("anonymous")
                .flatMap(username -> {
                    String clientIp = getClientIpAddress(exchange);

                    log.info("PUT /api/inventory/{}/stores/{} - User: {} - IP: {} - Setting absolute quantity to: {}",
                            productSku, storeId, username, clientIp, request.getAvailableQty());

                    return inventoryService.updateInventory(productSku, storeId, request)
                            .doOnSuccess(response -> log.info("Successfully updated inventory for product {} in store {} - New quantity: {} - User: {} - IP: {}",
                                    productSku, storeId, response.getAvailableQty(), username, clientIp))
                            .doOnError(e -> log.error("Failed to update inventory for product {} in store {} to quantity {} - User: {} - IP: {} - Error: {}",
                                    productSku, storeId, request.getAvailableQty(), username, clientIp, e.getMessage()));
                });
    }

    @Operation(summary = "Adjust inventory quantity",
            description = "Adjusts the inventory quantity (positive for additions, negative for sales)")
    @PostMapping("/{productSku}/stores/{storeId}/adjustments")
    @RequireStorePermission
    public Mono<InventoryResponse> adjustInventory(
            @PathVariable String productSku,
            @PathVariable Long storeId,
            @Valid @RequestBody InventoryAdjustmentRequest request,
            ServerWebExchange exchange) {

        return exchange.getPrincipal()
                .map(principal -> principal.getName())
                .defaultIfEmpty("anonymous")
                .flatMap(username -> {
                    String clientIp = getClientIpAddress(exchange);

                    log.info("POST /api/inventory/{}/stores/{}/adjustments - User: {} - IP: {} - Adjusting by: {}",
                            productSku, storeId, username, clientIp, request.getAdjustment());

                    return inventoryService.adjustInventory(productSku, storeId, request)
                            .doOnSuccess(response -> log.info("Successfully adjusted inventory for product {} in store {} by {} - Final quantity: {} - User: {} - IP: {}",
                                    productSku, storeId, request.getAdjustment(), response.getAvailableQty(), username, clientIp))
                            .doOnError(e -> log.error("Failed to adjust inventory for product {} in store {} by {} - User: {} - IP: {} - Error: {}",
                                    productSku, storeId, request.getAdjustment(), username, clientIp, e.getMessage()));
                });
    }

    private String getClientIpAddress(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }
}
