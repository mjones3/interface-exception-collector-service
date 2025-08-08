package com.arcone.biopro.exception.collector.api.controller;

import com.arcone.biopro.exception.collector.api.dto.ErrorResponse;
import com.arcone.biopro.exception.collector.domain.exception.ExceptionNotFoundException;
import com.arcone.biopro.exception.collector.domain.exception.ExceptionProcessingException;
import com.arcone.biopro.exception.collector.domain.exception.RetryNotAllowedException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests error handling for various exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/v1/exceptions/test");
    }

    @Test
    void handleExceptionNotFound_ShouldReturn404() {
        // Given
        ExceptionNotFoundException exception = new ExceptionNotFoundException("TXN-123");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleExceptionNotFound(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("EXCEPTION_NOT_FOUND");
        assertThat(response.getBody().getMessage()).contains("TXN-123");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/exceptions/test");
    }

    @Test
    void handleRetryNotAllowed_ShouldReturn409() {
        // Given
        RetryNotAllowedException exception = new RetryNotAllowedException("TXN-123");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRetryNotAllowed(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("RETRY_NOT_ALLOWED");
        assertThat(response.getBody().getMessage()).contains("TXN-123");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/exceptions/test");
    }

    @Test
    void handleExceptionProcessing_ShouldReturn422() {
        // Given
        ExceptionProcessingException exception = new ExceptionProcessingException("Processing failed");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleExceptionProcessing(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(422);
        assertThat(response.getBody().getError()).isEqualTo("EXCEPTION_PROCESSING_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Processing failed");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/exceptions/test");
    }

    @Test
    void handleDataAccessException_ShouldReturn503() {
        // Given
        DataAccessException exception = new DataAccessException("Database connection failed") {
        };

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataAccessException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(503);
        assertThat(response.getBody().getError()).isEqualTo("DATABASE_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Database service is temporarily unavailable");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/exceptions/test");
    }

    @Test
    void handleResourceAccessException_ShouldReturn502() {
        // Given
        ResourceAccessException exception = new ResourceAccessException("External service unavailable");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceAccessException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(502);
        assertThat(response.getBody().getError()).isEqualTo("EXTERNAL_SERVICE_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("External service is temporarily unavailable");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/exceptions/test");
    }

    @Test
    void handleGenericException_ShouldReturn500() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/exceptions/test");
    }
}