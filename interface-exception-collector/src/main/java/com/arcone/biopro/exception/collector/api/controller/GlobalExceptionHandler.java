package com.arcone.biopro.exception.collector.api.controller;

import com.arcone.biopro.exception.collector.api.dto.ErrorResponse;
import com.arcone.biopro.exception.collector.domain.exception.ExceptionNotFoundException;
import com.arcone.biopro.exception.collector.domain.exception.ExceptionProcessingException;
import com.arcone.biopro.exception.collector.domain.exception.RetryNotAllowedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API endpoints.
 * Provides consistent error responses across all controllers.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        /**
         * Handles validation errors from request body validation.
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationErrors(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {

                log.warn("Validation error in request to {}: {}", request.getRequestURI(), ex.getMessage());

                List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(this::mapFieldError)
                                .collect(Collectors.toList());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("VALIDATION_ERROR")
                                .message("Request validation failed")
                                .path(request.getRequestURI())
                                .timestamp(OffsetDateTime.now())
                                .fieldErrors(fieldErrors)
                                .build();

                return ResponseEntity.badRequest().body(errorResponse);
        }

        /**
         * Handles constraint violation errors from request parameter validation.
         */
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolation(
                        ConstraintViolationException ex, HttpServletRequest request) {

                log.warn("Constraint violation in request to {}: {}", request.getRequestURI(), ex.getMessage());

                List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
                                .stream()
                                .map(this::mapConstraintViolation)
                                .collect(Collectors.toList());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("VALIDATION_ERROR")
                                .message("Request parameter validation failed")
                                .path(request.getRequestURI())
                                .timestamp(OffsetDateTime.now())
                                .fieldErrors(fieldErrors)
                                .build();

                return ResponseEntity.badRequest().body(errorResponse);
        }

        /**
         * Handles missing required request parameters.
         */
        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ErrorResponse> handleMissingParameter(
                        MissingServletRequestParameterException ex, HttpServletRequest request) {

                log.warn("Missing required parameter in request to {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("MISSING_PARAMETER")
                                .message(String.format("Required parameter '%s' is missing", ex.getParameterName()))
                                .path(request.getRequestURI())
                                .timestamp(OffsetDateTime.now())
                                .build();

                return ResponseEntity.badRequest().body(errorResponse);
        }

        /**
         * Handles type mismatch errors (e.g., invalid enum values).
         */
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatch(
                        MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

                log.warn("Type mismatch in request to {}: {}", request.getRequestURI(), ex.getMessage());

                String message = String.format("Invalid value '%s' for parameter '%s'",
                                ex.getValue(), ex.getName());

                // Add specific message for enum types
                if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
                        Object[] enumConstants = ex.getRequiredType().getEnumConstants();
                        String validValues = String.join(", ",
                                        java.util.Arrays.stream(enumConstants)
                                                        .map(Object::toString)
                                                        .toArray(String[]::new));
                        message += String.format(". Valid values are: %s", validValues);
                }

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("INVALID_PARAMETER")
                                .message(message)
                                .path(request.getRequestURI())
                                .timestamp(OffsetDateTime.now())
                                .build();

                return ResponseEntity.badRequest().body(errorResponse);
        }

        /**
         * Handles malformed JSON in request body.
         */
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleMalformedJson(
                        HttpMessageNotReadableException ex, HttpServletRequest request) {

                log.warn("Malformed JSON in request to {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("MALFORMED_JSON")
                                .message("Request body contains invalid JSON")
                                .path(request.getRequestURI())
                                .timestamp(OffsetDateTime.now())
                                .build();

                return ResponseEntity.badRequest().body(errorResponse);
        }

        /**
         * Handles illegal argument exceptions (e.g., invalid business logic
         * parameters).
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(
                        IllegalArgumentException ex, HttpServletRequest request) {

                log.warn("Illegal argument in request to {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("INVALID_ARGUMENT")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .timestamp(OffsetDateTime.now())
                                .build();

                return ResponseEntity.badRequest().body(errorResponse);
        }

        /**
         * Handles exception not found errors (HTTP 404).
         */
        @ExceptionHandler(ExceptionNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleExceptionNotFound(
                        ExceptionNotFoundException ex, HttpServletRequest request) {

                log.warn("Exception not found in request to {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .error("EXCEPTION_NOT_FOUND")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .timestamp(OffsetDateTime.now())
                                .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        /**
         * Handles retry not allowed errors (HTTP 409).
         */
        @ExceptionHandler(RetryNotAllowedException.class)
        public ResponseEntity<ErrorResponse> handleRetryNotAllowed(
                        RetryNotAllowedException ex, HttpServletRequest request) {

                log.warn("Retry not allowed in request to {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.CONFLICT.value())
                                .error("RETRY_NOT_ALLOWED")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .timestamp(OffsetDateTime.now())
                                .build();

                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        /**
         * Handles exception processing errors (HTTP 422).
         */
        @ExceptionHandler(ExceptionProcessingException.class)
        public ResponseEntity<ErrorResponse> handleExceptionProcessing(
                        ExceptionProcessingException ex, HttpServletRequest request) {

                log.error("Exception processing error in request to {}: {}", request.getRequestURI(), ex.getMessage(),
                                ex);

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                                .error("EXCEPTION_PROCESSING_ERROR")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .timestamp(OffsetDateTime.now())
                                .build();

                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
        }

        /**
         * Handles database access errors (HTTP 503).
         */
        @ExceptionHandler(DataAccessException.class)
        public ResponseEntity<ErrorResponse> handleDataAccessException(
                        DataAccessException ex, HttpServletRequest request) {

                log.error("Database access error in request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                                .error("DATABASE_ERROR")
                                .message("Database service is temporarily unavailable")
                                .path(request.getRequestURI())
                                .timestamp(OffsetDateTime.now())
                                .build();

                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }

        /**
         * Handles external service connection errors (HTTP 502).
         */
        @ExceptionHandler(ResourceAccessException.class)
        public ResponseEntity<ErrorResponse> handleResourceAccessException(
                        ResourceAccessException ex, HttpServletRequest request) {

                log.error("External service access error in request to {}: {}", request.getRequestURI(),
                                ex.getMessage(), ex);

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_GATEWAY.value())
                                .error("EXTERNAL_SERVICE_ERROR")
                                .message("External service is temporarily unavailable")
                                .path(request.getRequestURI())
                                .timestamp(OffsetDateTime.now())
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
        }

        /**
         * Handles all other unexpected exceptions.
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(
                        Exception ex, HttpServletRequest request) {

                log.error("Unexpected error in request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .error("INTERNAL_SERVER_ERROR")
                                .message("An unexpected error occurred")
                                .path(request.getRequestURI())
                                .timestamp(OffsetDateTime.now())
                                .build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        /**
         * Maps Spring validation FieldError to our ErrorResponse.FieldError.
         */
        private ErrorResponse.FieldError mapFieldError(FieldError fieldError) {
                return ErrorResponse.FieldError.builder()
                                .field(fieldError.getField())
                                .rejectedValue(fieldError.getRejectedValue())
                                .message(fieldError.getDefaultMessage())
                                .build();
        }

        /**
         * Maps constraint violation to our ErrorResponse.FieldError.
         */
        private ErrorResponse.FieldError mapConstraintViolation(ConstraintViolation<?> violation) {
                String fieldName = violation.getPropertyPath().toString();
                // Extract just the parameter name from the full path
                if (fieldName.contains(".")) {
                        fieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
                }

                return ErrorResponse.FieldError.builder()
                                .field(fieldName)
                                .rejectedValue(violation.getInvalidValue())
                                .message(violation.getMessage())
                                .build();
        }
}