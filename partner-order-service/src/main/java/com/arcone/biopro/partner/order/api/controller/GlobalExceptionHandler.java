package com.arcone.biopro.partner.order.api.controller;

import com.arcone.biopro.partner.order.api.dto.PartnerOrderResponse;
import com.arcone.biopro.partner.order.application.exception.DuplicateExternalIdException;
import com.arcone.biopro.partner.order.application.exception.PayloadNotFoundException;
import com.arcone.biopro.partner.order.application.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for centralized error handling.
 * Provides consistent error responses across all endpoints.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from JSR-303 annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PartnerOrderResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {

        log.warn("Validation error occurred: {}", ex.getMessage());

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        PartnerOrderResponse response = PartnerOrderResponse.validationError(
                errorMessage,
                extractExternalIdFromRequest(request),
                UUID.randomUUID());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles custom validation exceptions.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<PartnerOrderResponse> handleValidationException(
            ValidationException ex, WebRequest request) {

        log.warn("Custom validation error: {}", ex.getMessage());

        String errorMessage = String.join(", ", ex.getValidationErrors());

        PartnerOrderResponse response = PartnerOrderResponse.validationError(
                errorMessage,
                extractExternalIdFromRequest(request),
                UUID.randomUUID());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles duplicate external ID exceptions.
     */
    @ExceptionHandler(DuplicateExternalIdException.class)
    public ResponseEntity<PartnerOrderResponse> handleDuplicateExternalId(
            DuplicateExternalIdException ex, WebRequest request) {

        log.warn("Duplicate external ID: {}", ex.getExternalId());

        PartnerOrderResponse response = PartnerOrderResponse.duplicateError(
                ex.getExternalId(),
                UUID.randomUUID());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles payload not found exceptions.
     */
    @ExceptionHandler(PayloadNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePayloadNotFound(
            PayloadNotFoundException ex, WebRequest request) {

        log.warn("Payload not found: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "PAYLOAD_NOT_FOUND");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("transactionId", ex.getTransactionId());
        errorResponse.put("timestamp", OffsetDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles illegal argument exceptions (e.g., invalid UUID format).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {

        log.warn("Illegal argument: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "INVALID_ARGUMENT");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("timestamp", OffsetDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles all other unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {

        log.error("Unexpected error occurred", ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "INTERNAL_ERROR");
        errorResponse.put("message", "An unexpected error occurred");
        errorResponse.put("timestamp", OffsetDateTime.now());

        // Don't expose internal error details in production
        if (log.isDebugEnabled()) {
            errorResponse.put("details", ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Extracts external ID from request parameters if available.
     */
    private String extractExternalIdFromRequest(WebRequest request) {
        try {
            String externalId = request.getParameter("externalId");
            return externalId != null ? externalId : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
}