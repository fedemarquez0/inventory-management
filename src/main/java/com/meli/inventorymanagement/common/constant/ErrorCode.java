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
    INVALID_SKU_FORMAT("INV-010", "Invalid SKU format provided"),
    INVENTORY_OPERATION_FAILED("INV-011", "Inventory operation could not be completed"),

    AUTHENTICATION_FAILED("AUTH-001", "Authentication failed"),
    INVALID_TOKEN("AUTH-002", "Invalid or expired token"),
    SESSION_EXPIRED("AUTH-003", "Session has expired"),
    UNAUTHORIZED_ACCESS("AUTH-004", "Unauthorized access"),
    INVALID_CREDENTIALS("AUTH-005", "Invalid username or password"),
    USER_NOT_FOUND("AUTH-006", "User not found"),
    USER_NOT_AUTHENTICATED("AUTH-007", "User not authenticated"),
    ACCESS_DENIED_TO_STORE("AUTH-008", "Access denied to the specified store"),
    ADMIN_ACCESS_REQUIRED("AUTH-009", "Administrator access required for this operation"),
    STORE_PERMISSION_DENIED("AUTH-010", "Insufficient permissions for store operations"),
    TOKEN_EXTRACTION_ERROR("AUTH-011", "Error processing authentication token"),
    USER_ROLE_INSUFFICIENT("AUTH-012", "User role insufficient for this operation"),
    USER_ACCOUNT_INACTIVE("AUTH-013", "User account is inactive"),
    PASSWORD_ENCODING_ERROR("AUTH-014", "Error processing password"),
    PERMISSION_CHECK_FAILED("AUTH-015", "Error checking user permissions"),

    VALIDATION_ERROR("VAL-001", "Validation error"),
    INVALID_REQUEST("VAL-002", "Invalid request parameters"),
    MISSING_REQUIRED_PARAMETER("VAL-003", "Required parameter is missing"),
    INVALID_PARAMETER_FORMAT("VAL-004", "Parameter format is invalid"),
    REQUEST_BODY_MISSING("VAL-005", "Request body is required"),
    JSON_PARSE_ERROR("VAL-006", "Invalid JSON format in request"),

    INTERNAL_SERVER_ERROR("SYS-001", "Internal server error"),
    DATABASE_ERROR("SYS-002", "Database operation failed"),
    SERVICE_UNAVAILABLE("SYS-003", "Service temporarily unavailable"),
    RESOURCE_ACCESS_ERROR("SYS-004", "Error accessing system resource"),
    CONFIGURATION_ERROR("SYS-005", "System configuration error"),
    DATABASE_CONNECTION_ERROR("SYS-006", "Database connection failed"),
    TRANSACTION_FAILED("SYS-007", "Database transaction failed"),
    EXTERNAL_SERVICE_ERROR("SYS-008", "External service communication failed");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
