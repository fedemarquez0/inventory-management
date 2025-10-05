package com.meli.inventorymanagement.infrastructure.exception;

import com.meli.inventorymanagement.common.constant.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        log.error("Business exception: {} - {}", ex.getErrorCode().getCode(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode().getCode())
                .message(ex.getErrorCode().getMessage())
                .details(ex.getDetails())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        HttpStatus status = mapErrorCodeToHttpStatus(ex.getErrorCode());
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

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
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
            UsernameNotFoundException ex, HttpServletRequest request) {

        log.error("User not found: {}", ex.getMessage(), ex);

        ErrorCode errorCode = ex.getMessage().contains("not active") ?
            ErrorCode.USER_ACCOUNT_INACTIVE : ErrorCode.USER_NOT_FOUND;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        HttpStatus status = errorCode == ErrorCode.USER_ACCOUNT_INACTIVE ?
            HttpStatus.FORBIDDEN : HttpStatus.NOT_FOUND;

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

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
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockException(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {

        log.error("Optimistic lock failure", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.OPTIMISTIC_LOCK_FAILURE.getCode())
                .message(ErrorCode.OPTIMISTIC_LOCK_FAILURE.getMessage())
                .details("The resource was modified by another transaction. Please retry your operation.")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {

        log.error("Bad credentials", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INVALID_CREDENTIALS.getCode())
                .message(ErrorCode.INVALID_CREDENTIALS.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        log.error("Authentication error", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.AUTHENTICATION_FAILED.getCode())
                .message(ErrorCode.AUTHENTICATION_FAILED.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.error("Missing required parameter: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.MISSING_REQUIRED_PARAMETER.getCode())
                .message(ErrorCode.MISSING_REQUIRED_PARAMETER.getMessage())
                .details("Missing required parameter: " + ex.getParameterName())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ErrorResponse> handleMissingPathVariableException(
            MissingPathVariableException ex, HttpServletRequest request) {

        log.error("Missing path variable: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.MISSING_REQUIRED_PARAMETER.getCode())
                .message(ErrorCode.MISSING_REQUIRED_PARAMETER.getMessage())
                .details("Missing required path variable: " + ex.getVariableName())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.error("Parameter type mismatch: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INVALID_PARAMETER_FORMAT.getCode())
                .message(ErrorCode.INVALID_PARAMETER_FORMAT.getMessage())
                .details(String.format("Invalid format for parameter '%s'. Expected type: %s",
                        ex.getName(), ex.getRequiredType().getSimpleName()))
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.error("JSON parse error: {}", ex.getMessage(), ex);

        ErrorCode errorCode = ex.getMessage().contains("Required request body is missing") ?
            ErrorCode.REQUEST_BODY_MISSING : ErrorCode.JSON_PARSE_ERROR;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .details("Invalid JSON format in request body")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Data integrity violation: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.DATABASE_ERROR.getCode())
                .message(ErrorCode.DATABASE_ERROR.getMessage())
                .details("Data constraint violation occurred")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(
            DataAccessException ex, HttpServletRequest request) {

        log.error("Database access error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.DATABASE_ERROR.getCode())
                .message(ErrorCode.DATABASE_ERROR.getMessage())
                .details("Database operation failed")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSQLException(
            SQLException ex, HttpServletRequest request) {

        log.error("SQL error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.DATABASE_CONNECTION_ERROR.getCode())
                .message(ErrorCode.DATABASE_CONNECTION_ERROR.getMessage())
                .details("Database connection or query failed")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ErrorResponse> handleTransactionException(
            TransactionException ex, HttpServletRequest request) {

        log.error("Transaction error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.TRANSACTION_FAILED.getCode())
                .message(ErrorCode.TRANSACTION_FAILED.getMessage())
                .details("Database transaction could not be completed")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus mapErrorCodeToHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case PRODUCT_NOT_FOUND, STORE_NOT_FOUND, INVENTORY_NOT_FOUND, USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INSUFFICIENT_STOCK, NEGATIVE_QUANTITY_NOT_ALLOWED, INVALID_ADJUSTMENT,
                 PRODUCT_ALREADY_EXISTS, STORE_ALREADY_EXISTS, VALIDATION_ERROR,
                 INVALID_REQUEST, MISSING_REQUIRED_PARAMETER, INVALID_PARAMETER_FORMAT,
                 REQUEST_BODY_MISSING, JSON_PARSE_ERROR, INVALID_SKU_FORMAT -> HttpStatus.BAD_REQUEST;
            case OPTIMISTIC_LOCK_FAILURE -> HttpStatus.CONFLICT;
            case AUTHENTICATION_FAILED, INVALID_TOKEN, SESSION_EXPIRED,
                 UNAUTHORIZED_ACCESS, INVALID_CREDENTIALS, USER_NOT_AUTHENTICATED,
                 TOKEN_EXTRACTION_ERROR -> HttpStatus.UNAUTHORIZED;
            case ACCESS_DENIED_TO_STORE, ADMIN_ACCESS_REQUIRED, STORE_PERMISSION_DENIED,
                 USER_ROLE_INSUFFICIENT, USER_ACCOUNT_INACTIVE -> HttpStatus.FORBIDDEN;
            case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case DATABASE_ERROR, DATABASE_CONNECTION_ERROR, TRANSACTION_FAILED,
                 INVENTORY_OPERATION_FAILED, PASSWORD_ENCODING_ERROR, PERMISSION_CHECK_FAILED,
                 RESOURCE_ACCESS_ERROR, CONFIGURATION_ERROR, EXTERNAL_SERVICE_ERROR,
                 INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
