package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.client.SourceServiceClient;
import com.arcone.biopro.exception.collector.infrastructure.client.SourceServiceClientRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Integration tests for PayloadRetrievalService with the new client
 * architecture.
 */
@ExtendWith(MockitoExtension.class)
class PayloadRetrievalServiceIntegrationTest {

    @Mock
    private SourceServiceClientRegistry clientRegistry;

    @Mock
    private SourceServiceClient mockClient;

    private PayloadRetrievalService payloadRetrievalService;

    @BeforeEach
    void setUp() {
        payloadRetrievalService = new PayloadRetrievalService(clientRegistry);
    }

    @Test
    void testGetOriginalPayload_Success() throws ExecutionException, InterruptedException {
        // Given
        InterfaceException exception = InterfaceException.builder()
                .transactionId("test-123")
                .interfaceType(InterfaceType.ORDER)
                .build();

        PayloadResponse expectedResponse = PayloadResponse.builder()
                .transactionId("test-123")
                .interfaceType("ORDER")
                .sourceService("order-service")
                .retrieved(true)
                .payload("test-payload")
                .build();

        when(clientRegistry.getClient(InterfaceType.ORDER)).thenReturn(mockClient);
        when(mockClient.getOriginalPayload(exception))
                .thenReturn(CompletableFuture.completedFuture(expectedResponse));

        // When
        CompletableFuture<PayloadResponse> future = payloadRetrievalService.getOriginalPayload(exception);
        PayloadResponse response = future.get();

        // Then
        assertNotNull(response);
        assertTrue(response.isRetrieved());
        assertEquals("test-123", response.getTransactionId());
        assertEquals("ORDER", response.getInterfaceType());
        assertEquals("order-service", response.getSourceService());
        assertEquals("test-payload", response.getPayload());
    }

    @Test
    void testGetOriginalPayload_NoClientAvailable() throws ExecutionException, InterruptedException {
        // Given
        InterfaceException exception = InterfaceException.builder()
                .transactionId("test-456")
                .interfaceType(InterfaceType.ORDER)
                .build();

        when(clientRegistry.getClient(InterfaceType.ORDER))
                .thenThrow(new IllegalArgumentException("Unsupported interface type: ORDER"));

        // When
        CompletableFuture<PayloadResponse> future = payloadRetrievalService.getOriginalPayload(exception);
        PayloadResponse response = future.get();

        // Then
        assertNotNull(response);
        assertFalse(response.isRetrieved());
        assertEquals("test-456", response.getTransactionId());
        assertEquals("ORDER", response.getInterfaceType());
        assertEquals("unknown", response.getSourceService());
        assertNotNull(response.getErrorMessage());
        assertTrue(response.getErrorMessage().contains("No client available"));
    }

    @Test
    void testSubmitRetry_Success() throws ExecutionException, InterruptedException {
        // Given
        InterfaceException exception = InterfaceException.builder()
                .transactionId("retry-123")
                .interfaceType(InterfaceType.COLLECTION)
                .operation("CREATE_COLLECTION")
                .build();

        Object payload = "test-retry-payload";
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("success", HttpStatus.OK);

        when(clientRegistry.getClient(InterfaceType.COLLECTION)).thenReturn(mockClient);
        when(mockClient.submitRetry(exception, payload))
                .thenReturn(CompletableFuture.completedFuture(expectedResponse));

        // When
        CompletableFuture<ResponseEntity<Object>> future = payloadRetrievalService.submitRetry(exception, payload);
        ResponseEntity<Object> response = future.get();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody());
    }

    @Test
    void testSubmitRetry_NoClientAvailable() {
        // Given
        InterfaceException exception = InterfaceException.builder()
                .transactionId("retry-fail")
                .interfaceType(InterfaceType.DISTRIBUTION)
                .build();

        Object payload = "test-payload";

        when(clientRegistry.getClient(InterfaceType.DISTRIBUTION))
                .thenThrow(new IllegalArgumentException("Unsupported interface type: DISTRIBUTION"));

        // When
        CompletableFuture<ResponseEntity<Object>> future = payloadRetrievalService.submitRetry(exception, payload);

        // Then
        assertThrows(ExecutionException.class, future::get);
    }

    @Test
    void testGetOriginalPayload_Synchronous() {
        // Given
        PayloadResponse mockResponse = PayloadResponse.builder()
                .transactionId("sync-123")
                .interfaceType("ORDER")
                .retrieved(true)
                .payload("sync-payload")
                .build();

        when(clientRegistry.getClient(InterfaceType.ORDER)).thenReturn(mockClient);
        when(mockClient.getOriginalPayload(any(InterfaceException.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When
        Object result = payloadRetrievalService.getOriginalPayload("sync-123", "ORDER");

        // Then
        assertNotNull(result);
        assertEquals("sync-payload", result);
    }

    @Test
    void testGetOriginalPayload_Synchronous_NotRetrieved() {
        // Given
        PayloadResponse mockResponse = PayloadResponse.builder()
                .transactionId("sync-fail")
                .interfaceType("ORDER")
                .retrieved(false)
                .errorMessage("Service unavailable")
                .build();

        when(clientRegistry.getClient(InterfaceType.ORDER)).thenReturn(mockClient);
        when(mockClient.getOriginalPayload(any(InterfaceException.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When
        Object result = payloadRetrievalService.getOriginalPayload("sync-fail", "ORDER");

        // Then
        assertNull(result);
    }

    @Test
    void testHasClientFor() {
        // Given
        when(clientRegistry.hasClient(InterfaceType.ORDER)).thenReturn(true);
        when(clientRegistry.hasClient(InterfaceType.COLLECTION)).thenReturn(false);

        // When & Then
        assertTrue(payloadRetrievalService.hasClientFor(InterfaceType.ORDER));
        assertFalse(payloadRetrievalService.hasClientFor(InterfaceType.COLLECTION));
    }
}