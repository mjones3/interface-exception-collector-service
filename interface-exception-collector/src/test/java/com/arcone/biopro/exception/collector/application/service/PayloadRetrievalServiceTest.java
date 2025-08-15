package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.*;
import com.arcone.biopro.exception.collector.infrastructure.client.SourceServiceClient;
import com.arcone.biopro.exception.collector.infrastructure.client.SourceServiceClientRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PayloadRetrievalService.
 * Tests the new client registry approach.
 */
@ExtendWith(MockitoExtension.class)
class PayloadRetrievalServiceTest {

        @Mock
        private SourceServiceClientRegistry clientRegistry;

        @Mock
        private SourceServiceClient mockClient;

        private PayloadRetrievalService payloadRetrievalService;
        private InterfaceException testException;

        @BeforeEach
        void setUp() {
                payloadRetrievalService = new PayloadRetrievalService(clientRegistry);

                // Create test exception
                testException = InterfaceException.builder()
                                .id(1L)
                                .transactionId("test-transaction-123")
                                .interfaceType(InterfaceType.ORDER)
                                .exceptionReason("Order validation failed")
                                .operation("CREATE_ORDER")
                                .externalId("ORDER-123")
                                .status(ExceptionStatus.NEW)
                                .severity(ExceptionSeverity.MEDIUM)
                                .category(ExceptionCategory.VALIDATION)
                                .retryable(true)
                                .customerId("CUST-001")
                                .locationCode("LOC-001")
                                .timestamp(OffsetDateTime.now())
                                .processedAt(OffsetDateTime.now())
                                .retryCount(0)
                                .build();
        }

        @Test
        void shouldRetrievePayloadSuccessfully() throws Exception {
                // Given
                PayloadResponse expectedResponse = PayloadResponse.builder()
                                .transactionId("test-transaction-123")
                                .interfaceType("ORDER")
                                .sourceService("order-service")
                                .retrieved(true)
                                .payload("test-payload")
                                .build();

                when(clientRegistry.getClient(InterfaceType.ORDER)).thenReturn(mockClient);
                when(mockClient.getOriginalPayload(testException))
                                .thenReturn(CompletableFuture.completedFuture(expectedResponse));

                // When
                CompletableFuture<PayloadResponse> future = payloadRetrievalService.getOriginalPayload(testException);
                PayloadResponse response = future.get();

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getTransactionId()).isEqualTo("test-transaction-123");
                assertThat(response.getInterfaceType()).isEqualTo("ORDER");
                assertThat(response.getPayload()).isEqualTo("test-payload");
                assertThat(response.getSourceService()).isEqualTo("order-service");
                assertThat(response.isRetrieved()).isTrue();
                assertThat(response.getErrorMessage()).isNull();
        }

        @Test
        void shouldHandleServiceFailureGracefully() throws Exception {
                // Given
                PayloadResponse failureResponse = PayloadResponse.builder()
                                .transactionId("test-transaction-123")
                                .interfaceType("ORDER")
                                .sourceService("order-service")
                                .retrieved(false)
                                .errorMessage("Service unavailable")
                                .build();

                when(clientRegistry.getClient(InterfaceType.ORDER)).thenReturn(mockClient);
                when(mockClient.getOriginalPayload(testException))
                                .thenReturn(CompletableFuture.completedFuture(failureResponse));

                // When
                CompletableFuture<PayloadResponse> future = payloadRetrievalService.getOriginalPayload(testException);
                PayloadResponse response = future.get();

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getTransactionId()).isEqualTo("test-transaction-123");
                assertThat(response.getInterfaceType()).isEqualTo("ORDER");
                assertThat(response.getPayload()).isNull();
                assertThat(response.getSourceService()).isEqualTo("order-service");
                assertThat(response.isRetrieved()).isFalse();
                assertThat(response.getErrorMessage()).isEqualTo("Service unavailable");
        }

        @Test
        void shouldSubmitRetrySuccessfully() throws Exception {
                // Given
                Object payload = "test-retry-payload";
                ResponseEntity<Object> expectedResponse = new ResponseEntity<>("success", HttpStatus.OK);

                when(clientRegistry.getClient(InterfaceType.ORDER)).thenReturn(mockClient);
                when(mockClient.submitRetry(testException, payload))
                                .thenReturn(CompletableFuture.completedFuture(expectedResponse));

                // When
                CompletableFuture<ResponseEntity<Object>> future = payloadRetrievalService.submitRetry(testException,
                                payload);
                ResponseEntity<Object> response = future.get();

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo("success");
        }

        @Test
        void shouldHandleUnsupportedInterfaceType() throws Exception {
                // Given
                InterfaceException unsupportedException = InterfaceException.builder()
                                .transactionId("unsupported-123")
                                .interfaceType(InterfaceType.ORDER)
                                .build();

                when(clientRegistry.getClient(InterfaceType.ORDER))
                                .thenThrow(new IllegalArgumentException("Unsupported interface type: ORDER"));

                // When
                CompletableFuture<PayloadResponse> future = payloadRetrievalService
                                .getOriginalPayload(unsupportedException);
                PayloadResponse response = future.get();

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getTransactionId()).isEqualTo("unsupported-123");
                assertThat(response.getInterfaceType()).isEqualTo("ORDER");
                assertThat(response.isRetrieved()).isFalse();
                assertThat(response.getErrorMessage()).contains("No client available for interface type");
        }

        @Test
        void shouldGetOriginalPayloadSynchronously() {
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
                assertThat(result).isNotNull();
                assertThat(result).isEqualTo("sync-payload");
        }

        @Test
        void shouldReturnNullForFailedSynchronousRetrieval() {
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
                assertThat(result).isNull();
        }

        @Test
        void shouldCheckClientAvailability() {
                // Given
                when(clientRegistry.hasClient(InterfaceType.ORDER)).thenReturn(true);
                when(clientRegistry.hasClient(InterfaceType.COLLECTION)).thenReturn(false);

                // When & Then
                assertThat(payloadRetrievalService.hasClientFor(InterfaceType.ORDER)).isTrue();
                assertThat(payloadRetrievalService.hasClientFor(InterfaceType.COLLECTION)).isFalse();
        }
}