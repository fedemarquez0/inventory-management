package com.meli.inventorymanagement.common.constant;

import lombok.Getter;

@Getter
public enum ErrorCode {

    PRODUCT_NOT_FOUND("INV-001", "Product not found"),
    STORE_NOT_FOUND("INV-002", "Store not found"),
    INVENTORY_NOT_FOUND("INV-003", "Inventory not found"),
    INSUFFICIENT_STOCK("INV-004", "Insufficient stock available"),
    NEGATIVE_QUANTITY_NOT_ALLOWED("INV-005", "Negative quantity not allowed"),
    OPTIMISTIC_LOCK_FAILURE("INV-006", "Concurrent modification detected. Please retry"),
    PRODUCT_ALREADY_EXISTS("INV-007", "Product with this SKU already exists"),
    STORE_ALREADY_EXISTS("INV-008", "Store with this name already exists"),
    INVALID_ADJUSTMENT("INV-009", "Invalid inventory adjustment"),

    AUTHENTICATION_FAILED("AUTH-001", "Authentication failed"),
    INVALID_TOKEN("AUTH-002", "Invalid or expired token"),
    SESSION_EXPIRED("AUTH-003", "Session has expired"),
    UNAUTHORIZED_ACCESS("AUTH-004", "Unauthorized access"),
    INVALID_CREDENTIALS("AUTH-005", "Invalid username or password"),

    VALIDATION_ERROR("VAL-001", "Validation error"),
    INVALID_REQUEST("VAL-002", "Invalid request parameters"),

    INTERNAL_SERVER_ERROR("SYS-001", "Internal server error"),
    DATABASE_ERROR("SYS-002", "Database operation failed"),
    SERVICE_UNAVAILABLE("SYS-003", "Service temporarily unavailable");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
