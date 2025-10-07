package com.meli.inventorymanagement.infrastructure.exception;

import com.meli.inventorymanagement.common.constant.ErrorCode;
import com.meli.inventorymanagement.domain.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(-2)
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                   WebProperties webProperties,
                                   ApplicationContext applicationContext,
                                   ServerCodecConfigurer configurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);
        log.error("Error occurred: {}", error.getMessage(), error);

        return switch (error) {
            case BusinessException ex -> handleBusinessException(ex, request);
            case AccessDeniedException ex -> handleAccessDeniedException(ex, request);
            case UsernameNotFoundException ex -> handleUsernameNotFoundException(ex, request);
            case WebExchangeBindException ex -> handleValidationException(ex, request);
            case OptimisticLockingFailureException ex -> handleOptimisticLockException(ex, request);
            case DataAccessException ex -> handleDataAccessException(ex, request);
            case ServerWebInputException ex -> handleServerWebInputException(ex, request);
            default -> handleGenericException(error, request);
        };
    }

    private Mono<ServerResponse> handleBusinessException(BusinessException ex, ServerRequest request) {
        log.error("Business exception: {} - {}", ex.getErrorCode().getCode(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode().getCode())
                .message(ex.getErrorCode().getMessage())
                .details(ex.getDetails())
                .timestamp(LocalDateTime.now())
                .path(request.path())
                .build();

        HttpStatus status = mapErrorCodeToHttpStatus(ex.getErrorCode());
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }

    private Mono<ServerResponse> handleAccessDeniedException(AccessDeniedException ex, ServerRequest request) {
        log.error("Access denied: {}", ex.getMessage(), ex);

        ErrorCode errorCode;
        if (ex.getMessage().contains("User not authenticated")) {
            errorCode = ErrorCode.USER_NOT_AUTHENTICATED;
        } else if (ex.getMessage().contains("User not found")) {
            errorCode = ErrorCode.USER_NOT_FOUND;
        } else if (ex.getMessage().contains("Admin access required")) {
            errorCode = ErrorCode.ADMIN_ACCESS_REQUIRED;
        } else if (ex.getMessage().contains("Access denied to store")) {
            errorCode = ErrorCode.ACCESS_DENIED_TO_STORE;
        } else {
            errorCode = ErrorCode.STORE_PERMISSION_DENIED;
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.path())
                .build();

        return ServerResponse.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }

    private Mono<ServerResponse> handleUsernameNotFoundException(UsernameNotFoundException ex, ServerRequest request) {
        log.error("User not found: {}", ex.getMessage(), ex);

        ErrorCode errorCode = ex.getMessage().contains("not active") ?
                ErrorCode.USER_ACCOUNT_INACTIVE : ErrorCode.USER_NOT_FOUND;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.path())
                .build();

        HttpStatus status = errorCode == ErrorCode.USER_ACCOUNT_INACTIVE ?
                HttpStatus.FORBIDDEN : HttpStatus.NOT_FOUND;

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }

    private Mono<ServerResponse> handleValidationException(WebExchangeBindException ex, ServerRequest request) {
        log.error("Validation error", ex);

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ErrorCode.VALIDATION_ERROR.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.path())
                .validationErrors(validationErrors)
                .build();

        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }

    private Mono<ServerResponse> handleOptimisticLockException(OptimisticLockingFailureException ex, ServerRequest request) {
        log.error("Optimistic lock failure", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.OPTIMISTIC_LOCK_FAILURE.getCode())
                .message(ErrorCode.OPTIMISTIC_LOCK_FAILURE.getMessage())
                .details("The resource was modified by another transaction. Please retry your operation.")
                .timestamp(LocalDateTime.now())
                .path(request.path())
                .build();

        return ServerResponse.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }

    private Mono<ServerResponse> handleDataAccessException(DataAccessException ex, ServerRequest request) {
        log.error("Database error", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.DATABASE_ERROR.getCode())
                .message(ErrorCode.DATABASE_ERROR.getMessage())
                .details("A database error occurred while processing your request")
                .timestamp(LocalDateTime.now())
                .path(request.path())
                .build();

        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }

    private Mono<ServerResponse> handleServerWebInputException(ServerWebInputException ex, ServerRequest request) {
        log.error("Invalid input", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INVALID_PARAMETER_FORMAT.getCode())
                .message(ErrorCode.INVALID_PARAMETER_FORMAT.getMessage())
                .details(ex.getReason())
                .timestamp(LocalDateTime.now())
                .path(request.path())
                .build();

        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }

    private Mono<ServerResponse> handleGenericException(Throwable ex, ServerRequest request) {
        log.error("Unexpected error", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.path())
                .build();

        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }

    private HttpStatus mapErrorCodeToHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case PRODUCT_NOT_FOUND, STORE_NOT_FOUND, INVENTORY_NOT_FOUND, USER_NOT_FOUND ->
                    HttpStatus.NOT_FOUND;
            case INVALID_SKU_FORMAT, INVALID_PARAMETER_FORMAT, NEGATIVE_QUANTITY_NOT_ALLOWED,
                 INVALID_ADJUSTMENT, VALIDATION_ERROR ->
                    HttpStatus.BAD_REQUEST;
            case INSUFFICIENT_STOCK, INVENTORY_OPERATION_FAILED ->
                    HttpStatus.CONFLICT;
            case INVALID_CREDENTIALS, AUTHENTICATION_FAILED, TOKEN_EXTRACTION_ERROR ->
                    HttpStatus.UNAUTHORIZED;
            case STORE_PERMISSION_DENIED, USER_NOT_AUTHENTICATED, ADMIN_ACCESS_REQUIRED,
                 ACCESS_DENIED_TO_STORE, USER_ACCOUNT_INACTIVE ->
                    HttpStatus.FORBIDDEN;
            case OPTIMISTIC_LOCK_FAILURE ->
                    HttpStatus.CONFLICT;
            default ->
                    HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
