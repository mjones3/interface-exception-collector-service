package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.event.inbound.CollectionRejectedEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.DistributionFailedEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderCancelledEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.ValidationErrorEvent;
import com.arcone.biopro.exception.collector.infrastructure.client.SourceServiceClient;
import com.arcone.biopro.exception.collector.infrastructure.client.SourceServiceClientRegistry;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ExceptionProcessingService.
 * Tests business logic for exception categorization, severity assignment,
 * duplicate detection, and lifecycle state management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExceptionProcessingService Tests")
class ExceptionProcessingServiceTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private CacheEvictionService cacheEvictionService;

    @Mock
    private MetricsService metricsService;

    @Mock
    private SourceServiceClientRegistry clientRegistry;

    @Mock
    private SourceServiceClient sourceServiceClient;

    @InjectMocks
    private ExceptionProcessingService exceptionProcessingService;

    private OffsetDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = OffsetDateTime.now();
    }

    @Nested
    @DisplayName("Order Exception Processing")
    class OrderExceptionProcessingTests {

        @Test
        @DisplayName("Should process OrderRejected event and create new exception")
        void shouldProcessOrderRejectedEventAndCreateNewException() {
            // Given
            String transactionId = "txn-123";
            OrderRejectedEvent event = createOrderRejectedEvent(transactionId, "Order already exists", "CREATE_ORDER");

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class))).thenAnswer(invocation -> {
                InterfaceException exception = invocation.getArgument(0);
                exception.setId(1L);
                return exception;
            });

            // When
            InterfaceException result = exceptionProcessingService.processOrderRejectedEvent(event);

            // Then
            ArgumentCaptor<InterfaceException> captor = ArgumentCaptor.forClass(InterfaceException.class);
            verify(exceptionRepository).save(captor.capture());

            InterfaceException savedException = captor.getValue();
            assertThat(savedException.getTransactionId()).isEqualTo(transactionId);
            assertThat(savedException.getInterfaceType()).isEqualTo(InterfaceType.ORDER);
            assertThat(savedException.getExceptionReason()).isEqualTo("Order already exists");
            assertThat(savedException.getOperation()).isEqualTo("CREATE_ORDER");
            assertThat(savedException.getStatus()).isEqualTo(ExceptionStatus.NEW);
            assertThat(savedException.getCategory()).isEqualTo(ExceptionCategory.BUSINESS_RULE);
            assertThat(savedException.getSeverity()).isEqualTo(ExceptionSeverity.MEDIUM);
            assertThat(savedException.getRetryable()).isFalse(); // "already exists" should not be retryable
            assertThat(savedException.getRetryCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should update existing exception when duplicate OrderRejected event received")
        void shouldUpdateExistingExceptionWhenDuplicateOrderRejectedEventReceived() {
            // Given
            String transactionId = "txn-123";
            OrderRejectedEvent event = createOrderRejectedEvent(transactionId, "Updated rejection reason",
                    "CREATE_ORDER");

            InterfaceException existingException = InterfaceException.builder()
                    .id(1L)
                    .transactionId(transactionId)
                    .exceptionReason("Original rejection reason")
                    .processedAt(testTimestamp.minusHours(1))
                    .build();

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(existingException));
            when(exceptionRepository.save(any(InterfaceException.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            InterfaceException result = exceptionProcessingService.processOrderRejectedEvent(event);

            // Then
            verify(exceptionRepository).save(existingException);
            assertThat(result.getExceptionReason()).isEqualTo("Updated rejection reason");
            assertThat(result.getProcessedAt()).isAfter(testTimestamp.minusHours(1));
        }

        @Test
        @DisplayName("Should process OrderCancelled event and create new exception")
        void shouldProcessOrderCancelledEventAndCreateNewException() {
            // Given
            String transactionId = "txn-456";
            OrderCancelledEvent event = createOrderCancelledEvent(transactionId, "Customer requested cancellation");

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class))).thenAnswer(invocation -> {
                InterfaceException exception = invocation.getArgument(0);
                exception.setId(2L);
                return exception;
            });

            // When
            InterfaceException result = exceptionProcessingService.processOrderCancelledEvent(event);

            // Then
            ArgumentCaptor<InterfaceException> captor = ArgumentCaptor.forClass(InterfaceException.class);
            verify(exceptionRepository).save(captor.capture());

            InterfaceException savedException = captor.getValue();
            assertThat(savedException.getTransactionId()).isEqualTo(transactionId);
            assertThat(savedException.getInterfaceType()).isEqualTo(InterfaceType.ORDER);
            assertThat(savedException.getExceptionReason()).isEqualTo("Customer requested cancellation");
            assertThat(savedException.getOperation()).isEqualTo("CANCEL_ORDER");
            assertThat(savedException.getStatus()).isEqualTo(ExceptionStatus.NEW);
            assertThat(savedException.getCategory()).isEqualTo(ExceptionCategory.BUSINESS_RULE);
        }

        @Test
        @DisplayName("Should categorize order exceptions correctly based on rejection reason")
        void shouldCategorizeOrderExceptionsCorrectlyBasedOnRejectionReason() {
            // Test different rejection reasons and their expected categories
            testOrderExceptionCategorization("Order already exists", ExceptionCategory.BUSINESS_RULE);
            testOrderExceptionCategorization("Invalid customer data", ExceptionCategory.VALIDATION);
            testOrderExceptionCategorization("Connection timeout", ExceptionCategory.NETWORK_ERROR);
            testOrderExceptionCategorization("Unauthorized access", ExceptionCategory.AUTHORIZATION);
            testOrderExceptionCategorization("Authentication failed", ExceptionCategory.AUTHENTICATION);
            testOrderExceptionCategorization("System internal error", ExceptionCategory.SYSTEM_ERROR);
        }

        @Test
        @DisplayName("Should assign severity correctly based on rejection reason")
        void shouldAssignSeverityCorrectlyBasedOnRejectionReason() {
            // Test different rejection reasons and their expected severities
            testOrderExceptionSeverity("System error occurred", ExceptionSeverity.CRITICAL);
            testOrderExceptionSeverity("Connection timeout", ExceptionSeverity.HIGH);
            testOrderExceptionSeverity("Invalid order data", ExceptionSeverity.MEDIUM);
            testOrderExceptionSeverity("Customer not found", ExceptionSeverity.HIGH); // Customer-related = HIGH for
                                                                                      // orders
        }

        @Test
        @DisplayName("Should determine retryability correctly based on rejection reason")
        void shouldDetermineRetryabilityCorrectlyBasedOnRejectionReason() {
            // Test non-retryable conditions
            testOrderExceptionRetryability("Order already exists", false);
            testOrderExceptionRetryability("Invalid format", false);
            testOrderExceptionRetryability("Authentication failed", false);

            // Test retryable conditions
            testOrderExceptionRetryability("Connection timeout", true);
            testOrderExceptionRetryability("Service unavailable", true);
            testOrderExceptionRetryability("General business rule violation", true);
        }

        private void testOrderExceptionCategorization(String rejectionReason, ExceptionCategory expectedCategory) {
            String transactionId = "txn-" + System.nanoTime();
            OrderRejectedEvent event = createOrderRejectedEvent(transactionId, rejectionReason, "CREATE_ORDER");

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InterfaceException result = exceptionProcessingService.processOrderRejectedEvent(event);

            assertThat(result.getCategory()).isEqualTo(expectedCategory);
        }

        private void testOrderExceptionSeverity(String rejectionReason, ExceptionSeverity expectedSeverity) {
            String transactionId = "txn-" + System.nanoTime();
            OrderRejectedEvent event = createOrderRejectedEvent(transactionId, rejectionReason, "CREATE_ORDER");

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InterfaceException result = exceptionProcessingService.processOrderRejectedEvent(event);

            assertThat(result.getSeverity()).isEqualTo(expectedSeverity);
        }

        private void testOrderExceptionRetryability(String rejectionReason, boolean expectedRetryable) {
            String transactionId = "txn-" + System.nanoTime();
            OrderRejectedEvent event = createOrderRejectedEvent(transactionId, rejectionReason, "CREATE_ORDER");

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InterfaceException result = exceptionProcessingService.processOrderRejectedEvent(event);

            assertThat(result.getRetryable()).isEqualTo(expectedRetryable);
        }

        @Test
        @DisplayName("Should retrieve and store order data when processing OrderRejected event")
        void shouldRetrieveAndStoreOrderDataWhenProcessingOrderRejectedEvent() {
            // Given
            String transactionId = "txn-order-data-123";
            String externalId = "TEST-ORDER-1";
            OrderRejectedEvent event = createOrderRejectedEventWithExternalId(transactionId, "Order validation failed", "CREATE_ORDER", externalId);

            // Mock order data that would be returned from the mock RSocket server
            Object mockOrderData = createMockOrderData(externalId);
            PayloadResponse successfulResponse = PayloadResponse.builder()
                    .transactionId(transactionId)
                    .interfaceType(InterfaceType.ORDER.name())
                    .payload(mockOrderData)
                    .sourceService("mock-rsocket-server")
                    .retrieved(true)
                    .build();

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class))).thenAnswer(invocation -> {
                InterfaceException exception = invocation.getArgument(0);
                exception.setId(1L);
                return exception;
            });
            when(clientRegistry.hasClient(InterfaceType.ORDER)).thenReturn(true);
            when(clientRegistry.getClient(InterfaceType.ORDER)).thenReturn(sourceServiceClient);
            when(sourceServiceClient.getOriginalPayload(any(InterfaceException.class)))
                    .thenReturn(CompletableFuture.completedFuture(successfulResponse));

            // When
            InterfaceException result = exceptionProcessingService.processOrderRejectedEvent(event);

            // Then
            ArgumentCaptor<InterfaceException> captor = ArgumentCaptor.forClass(InterfaceException.class);
            verify(exceptionRepository).save(captor.capture());

            InterfaceException savedException = captor.getValue();
            assertThat(savedException.getOrderRetrievalAttempted()).isTrue();
            assertThat(savedException.getOrderReceived()).isEqualTo(mockOrderData);
            assertThat(savedException.getOrderRetrievedAt()).isNotNull();
            assertThat(savedException.getOrderRetrievalError()).isNull();
            assertThat(savedException.getRetryable()).isTrue();
            
            verify(clientRegistry).hasClient(InterfaceType.ORDER);
            verify(clientRegistry).getClient(InterfaceType.ORDER);
            verify(sourceServiceClient).getOriginalPayload(any(InterfaceException.class));
        }

        @Test
        @DisplayName("Should handle order data retrieval failure gracefully")
        void shouldHandleOrderDataRetrievalFailureGracefully() {
            // Given
            String transactionId = "txn-order-data-fail-123";
            String externalId = "NOTFOUND-ORDER-1";
            OrderRejectedEvent event = createOrderRejectedEventWithExternalId(transactionId, "Order validation failed", "CREATE_ORDER", externalId);

            PayloadResponse failedResponse = PayloadResponse.builder()
                    .transactionId(transactionId)
                    .interfaceType(InterfaceType.ORDER.name())
                    .sourceService("mock-rsocket-server")
                    .retrieved(false)
                    .errorMessage("Order not found")
                    .build();

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class))).thenAnswer(invocation -> {
                InterfaceException exception = invocation.getArgument(0);
                exception.setId(1L);
                return exception;
            });
            when(clientRegistry.hasClient(InterfaceType.ORDER)).thenReturn(true);
            when(clientRegistry.getClient(InterfaceType.ORDER)).thenReturn(sourceServiceClient);
            when(sourceServiceClient.getOriginalPayload(any(InterfaceException.class)))
                    .thenReturn(CompletableFuture.completedFuture(failedResponse));

            // When
            InterfaceException result = exceptionProcessingService.processOrderRejectedEvent(event);

            // Then
            ArgumentCaptor<InterfaceException> captor = ArgumentCaptor.forClass(InterfaceException.class);
            verify(exceptionRepository).save(captor.capture());

            InterfaceException savedException = captor.getValue();
            assertThat(savedException.getOrderRetrievalAttempted()).isTrue();
            assertThat(savedException.getOrderReceived()).isNull();
            assertThat(savedException.getOrderRetrievedAt()).isNull();
            assertThat(savedException.getOrderRetrievalError()).isEqualTo("Order not found");
            assertThat(savedException.getRetryable()).isFalse();
        }

        @Test
        @DisplayName("Should handle missing source service client gracefully")
        void shouldHandleMissingSourceServiceClientGracefully() {
            // Given
            String transactionId = "txn-no-client-123";
            String externalId = "TEST-ORDER-2";
            OrderRejectedEvent event = createOrderRejectedEventWithExternalId(transactionId, "Order validation failed", "CREATE_ORDER", externalId);

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class))).thenAnswer(invocation -> {
                InterfaceException exception = invocation.getArgument(0);
                exception.setId(1L);
                return exception;
            });
            when(clientRegistry.hasClient(InterfaceType.ORDER)).thenReturn(false);

            // When
            InterfaceException result = exceptionProcessingService.processOrderRejectedEvent(event);

            // Then
            ArgumentCaptor<InterfaceException> captor = ArgumentCaptor.forClass(InterfaceException.class);
            verify(exceptionRepository).save(captor.capture());

            InterfaceException savedException = captor.getValue();
            assertThat(savedException.getOrderRetrievalAttempted()).isTrue();
            assertThat(savedException.getOrderReceived()).isNull();
            assertThat(savedException.getOrderRetrievedAt()).isNull();
            assertThat(savedException.getOrderRetrievalError()).isEqualTo("No source service client available");
            assertThat(savedException.getRetryable()).isFalse();
        }

        @Test
        @DisplayName("Should handle timeout during order data retrieval")
        void shouldHandleTimeoutDuringOrderDataRetrieval() {
            // Given
            String transactionId = "txn-timeout-123";
            String externalId = "TIMEOUT-ORDER-1";
            OrderRejectedEvent event = createOrderRejectedEventWithExternalId(transactionId, "Order validation failed", "CREATE_ORDER", externalId);

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class))).thenAnswer(invocation -> {
                InterfaceException exception = invocation.getArgument(0);
                exception.setId(1L);
                return exception;
            });
            when(clientRegistry.hasClient(InterfaceType.ORDER)).thenReturn(true);
            when(clientRegistry.getClient(InterfaceType.ORDER)).thenReturn(sourceServiceClient);
            
            // Mock timeout scenario
            CompletableFuture<PayloadResponse> timeoutFuture = new CompletableFuture<>();
            timeoutFuture.completeExceptionally(new java.util.concurrent.TimeoutException("Request timeout"));
            when(sourceServiceClient.getOriginalPayload(any(InterfaceException.class)))
                    .thenReturn(timeoutFuture);

            // When
            InterfaceException result = exceptionProcessingService.processOrderRejectedEvent(event);

            // Then
            ArgumentCaptor<InterfaceException> captor = ArgumentCaptor.forClass(InterfaceException.class);
            verify(exceptionRepository).save(captor.capture());

            InterfaceException savedException = captor.getValue();
            assertThat(savedException.getOrderRetrievalAttempted()).isTrue();
            assertThat(savedException.getOrderReceived()).isNull();
            assertThat(savedException.getOrderRetrievedAt()).isNull();
            assertThat(savedException.getOrderRetrievalError()).contains("timeout");
            assertThat(savedException.getRetryable()).isFalse();
        }

        @Test
        @DisplayName("Should handle circuit breaker open during order data retrieval")
        void shouldHandleCircuitBreakerOpenDuringOrderDataRetrieval() {
            // Given
            String transactionId = "txn-cb-open-123";
            String externalId = "CB-OPEN-ORDER-1";
            OrderRejectedEvent event = createOrderRejectedEventWithExternalId(transactionId, "Order validation failed", "CREATE_ORDER", externalId);

            PayloadResponse circuitBreakerResponse = PayloadResponse.builder()
                    .transactionId(transactionId)
                    .interfaceType(InterfaceType.ORDER.name())
                    .sourceService("mock-rsocket-server")
                    .retrieved(false)
                    .errorMessage("Mock RSocket server unavailable - circuit breaker open")
                    .build();

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class))).thenAnswer(invocation -> {
                InterfaceException exception = invocation.getArgument(0);
                exception.setId(1L);
                return exception;
            });
            when(clientRegistry.hasClient(InterfaceType.ORDER)).thenReturn(true);
            when(clientRegistry.getClient(InterfaceType.ORDER)).thenReturn(sourceServiceClient);
            when(sourceServiceClient.getOriginalPayload(any(InterfaceException.class)))
                    .thenReturn(CompletableFuture.completedFuture(circuitBreakerResponse));

            // When
            InterfaceException result = exceptionProcessingService.processOrderRejectedEvent(event);

            // Then
            ArgumentCaptor<InterfaceException> captor = ArgumentCaptor.forClass(InterfaceException.class);
            verify(exceptionRepository).save(captor.capture());

            InterfaceException savedException = captor.getValue();
            assertThat(savedException.getOrderRetrievalAttempted()).isTrue();
            assertThat(savedException.getOrderReceived()).isNull();
            assertThat(savedException.getOrderRetrievedAt()).isNull();
            assertThat(savedException.getOrderRetrievalError()).contains("circuit breaker open");
            assertThat(savedException.getRetryable()).isFalse();
        }

        @Test
        @DisplayName("Should handle partial order data retrieval")
        void shouldHandlePartialOrderDataRetrieval() {
            // Given
            String transactionId = "txn-partial-123";
            String externalId = "PARTIAL-ORDER-1";
            OrderRejectedEvent event = createOrderRejectedEventWithExternalId(transactionId, "Order validation failed", "CREATE_ORDER", externalId);

            // Mock partial order data (missing some fields)
            Object partialOrderData = createPartialMockOrderData(externalId);
            PayloadResponse partialResponse = PayloadResponse.builder()
                    .transactionId(transactionId)
                    .interfaceType(InterfaceType.ORDER.name())
                    .payload(partialOrderData)
                    .sourceService("mock-rsocket-server")
                    .retrieved(true)
                    .build();

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class))).thenAnswer(invocation -> {
                InterfaceException exception = invocation.getArgument(0);
                exception.setId(1L);
                return exception;
            });
            when(clientRegistry.hasClient(InterfaceType.ORDER)).thenReturn(true);
            when(clientRegistry.getClient(InterfaceType.ORDER)).thenReturn(sourceServiceClient);
            when(sourceServiceClient.getOriginalPayload(any(InterfaceException.class)))
                    .thenReturn(CompletableFuture.completedFuture(partialResponse));

            // When
            InterfaceException result = exceptionProcessingService.processOrderRejectedEvent(event);

            // Then
            ArgumentCaptor<InterfaceException> captor = ArgumentCaptor.forClass(InterfaceException.class);
            verify(exceptionRepository).save(captor.capture());

            InterfaceException savedException = captor.getValue();
            assertThat(savedException.getOrderRetrievalAttempted()).isTrue();
            assertThat(savedException.getOrderReceived()).isEqualTo(partialOrderData);
            assertThat(savedException.getOrderRetrievedAt()).isNotNull();
            assertThat(savedException.getOrderRetrievalError()).isNull();
            assertThat(savedException.getRetryable()).isTrue();
        }

        @Test
        @DisplayName("Should retry order data retrieval for existing exceptions without order data")
        void shouldRetryOrderDataRetrievalForExistingExceptions() {
            // Given
            String transactionId = "txn-retry-123";
            String externalId = "RETRY-ORDER-1";
            OrderRejectedEvent event = createOrderRejectedEventWithExternalId(transactionId, "Updated rejection reason", "CREATE_ORDER", externalId);

            // Existing exception without order data
            InterfaceException existingException = InterfaceException.builder()
                    .id(1L)
                    .transactionId(transactionId)
                    .externalId(externalId)
                    .exceptionReason("Original rejection reason")
                    .orderRetrievalAttempted(false)
                    .orderReceived(null)
                    .processedAt(testTimestamp.minusHours(1))
                    .build();

            Object mockOrderData = createMockOrderData(externalId);
            PayloadResponse successfulResponse = PayloadResponse.builder()
                    .transactionId(transactionId)
                    .interfaceType(InterfaceType.ORDER.name())
                    .payload(mockOrderData)
                    .sourceService("mock-rsocket-server")
                    .retrieved(true)
                    .build();

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(existingException));
            when(exceptionRepository.save(any(InterfaceException.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(clientRegistry.hasClient(InterfaceType.ORDER)).thenReturn(true);
            when(clientRegistry.getClient(InterfaceType.ORDER)).thenReturn(sourceServiceClient);
            when(sourceServiceClient.getOriginalPayload(any(InterfaceException.class)))
                    .thenReturn(CompletableFuture.completedFuture(successfulResponse));

            // When
            InterfaceException result = exceptionProcessingService.processOrderRejectedEvent(event);

            // Then
            verify(exceptionRepository).save(existingException);
            assertThat(result.getExceptionReason()).isEqualTo("Updated rejection reason");
            assertThat(result.getOrderRetrievalAttempted()).isTrue();
            assertThat(result.getOrderReceived()).isEqualTo(mockOrderData);
            assertThat(result.getOrderRetrievedAt()).isNotNull();
            assertThat(result.getRetryable()).isTrue();
        }

        private Object createPartialMockOrderData(String externalId) {
            return java.util.Map.of(
                    "externalId", externalId,
                    "customerId", "CUST001"
                    // Missing some fields like locationCode, status, etc.
            );
        }
    }

    @Nested
    @DisplayName("Collection Exception Processing")
    class CollectionExceptionProcessingTests {

        @Test
        @DisplayName("Should process CollectionRejected event and create new exception")
        void shouldProcessCollectionRejectedEventAndCreateNewException() {
            // Given
            String transactionId = "txn-collection-123";
            CollectionRejectedEvent event = createCollectionRejectedEvent(transactionId, "Invalid donor information");

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class))).thenAnswer(invocation -> {
                InterfaceException exception = invocation.getArgument(0);
                exception.setId(3L);
                return exception;
            });

            // When
            InterfaceException result = exceptionProcessingService.processCollectionRejectedEvent(event);

            // Then
            ArgumentCaptor<InterfaceException> captor = ArgumentCaptor.forClass(InterfaceException.class);
            verify(exceptionRepository).save(captor.capture());

            InterfaceException savedException = captor.getValue();
            assertThat(savedException.getTransactionId()).isEqualTo(transactionId);
            assertThat(savedException.getInterfaceType()).isEqualTo(InterfaceType.COLLECTION);
            assertThat(savedException.getExceptionReason()).isEqualTo("Invalid donor information");
            assertThat(savedException.getOperation()).isEqualTo("CREATE_COLLECTION");
            assertThat(savedException.getStatus()).isEqualTo(ExceptionStatus.NEW);
            assertThat(savedException.getCategory()).isEqualTo(ExceptionCategory.VALIDATION);
        }

        @Test
        @DisplayName("Should categorize collection exceptions correctly")
        void shouldCategorizeCollectionExceptionsCorrectly() {
            testCollectionExceptionCategorization("Invalid donor data", ExceptionCategory.VALIDATION);
            testCollectionExceptionCategorization("Donor not eligible", ExceptionCategory.BUSINESS_RULE);
            testCollectionExceptionCategorization("System timeout", ExceptionCategory.NETWORK_ERROR);
            testCollectionExceptionCategorization("Internal system error", ExceptionCategory.SYSTEM_ERROR);
        }

        private void testCollectionExceptionCategorization(String rejectionReason, ExceptionCategory expectedCategory) {
            String transactionId = "txn-" + System.nanoTime();
            CollectionRejectedEvent event = createCollectionRejectedEvent(transactionId, rejectionReason);

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InterfaceException result = exceptionProcessingService.processCollectionRejectedEvent(event);

            assertThat(result.getCategory()).isEqualTo(expectedCategory);
        }
    }

    @Nested
    @DisplayName("Distribution Exception Processing")
    class DistributionExceptionProcessingTests {

        @Test
        @DisplayName("Should process DistributionFailed event and create new exception")
        void shouldProcessDistributionFailedEventAndCreateNewException() {
            // Given
            String transactionId = "txn-distribution-123";
            DistributionFailedEvent event = createDistributionFailedEvent(transactionId,
                    "Destination location not found");

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class))).thenAnswer(invocation -> {
                InterfaceException exception = invocation.getArgument(0);
                exception.setId(4L);
                return exception;
            });

            // When
            InterfaceException result = exceptionProcessingService.processDistributionFailedEvent(event);

            // Then
            ArgumentCaptor<InterfaceException> captor = ArgumentCaptor.forClass(InterfaceException.class);
            verify(exceptionRepository).save(captor.capture());

            InterfaceException savedException = captor.getValue();
            assertThat(savedException.getTransactionId()).isEqualTo(transactionId);
            assertThat(savedException.getInterfaceType()).isEqualTo(InterfaceType.DISTRIBUTION);
            assertThat(savedException.getExceptionReason()).isEqualTo("Destination location not found");
            assertThat(savedException.getOperation()).isEqualTo("CREATE_DISTRIBUTION");
            assertThat(savedException.getStatus()).isEqualTo(ExceptionStatus.NEW);
            assertThat(savedException.getCategory()).isEqualTo(ExceptionCategory.BUSINESS_RULE);
        }

        @Test
        @DisplayName("Should categorize distribution exceptions correctly")
        void shouldCategorizeDistributionExceptionsCorrectly() {
            testDistributionExceptionCategorization("Invalid destination data", ExceptionCategory.VALIDATION);
            testDistributionExceptionCategorization("Location not available", ExceptionCategory.BUSINESS_RULE);
            testDistributionExceptionCategorization("External service error", ExceptionCategory.EXTERNAL_SERVICE);
            testDistributionExceptionCategorization("Connection timeout", ExceptionCategory.NETWORK_ERROR);
        }

        private void testDistributionExceptionCategorization(String failureReason, ExceptionCategory expectedCategory) {
            String transactionId = "txn-" + System.nanoTime();
            DistributionFailedEvent event = createDistributionFailedEvent(transactionId, failureReason);

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InterfaceException result = exceptionProcessingService.processDistributionFailedEvent(event);

            assertThat(result.getCategory()).isEqualTo(expectedCategory);
        }
    }

    @Nested
    @DisplayName("Validation Error Processing")
    class ValidationErrorProcessingTests {

        @Test
        @DisplayName("Should process ValidationError event and create new exception with aggregated errors")
        void shouldProcessValidationErrorEventAndCreateNewExceptionWithAggregatedErrors() {
            // Given
            String transactionId = "txn-validation-123";
            ValidationErrorEvent event = createValidationErrorEvent(transactionId, "ORDER");

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());
            when(exceptionRepository.save(any(InterfaceException.class))).thenAnswer(invocation -> {
                InterfaceException exception = invocation.getArgument(0);
                exception.setId(5L);
                return exception;
            });

            // When
            InterfaceException result = exceptionProcessingService.processValidationErrorEvent(event);

            // Then
            ArgumentCaptor<InterfaceException> captor = ArgumentCaptor.forClass(InterfaceException.class);
            verify(exceptionRepository).save(captor.capture());

            InterfaceException savedException = captor.getValue();
            assertThat(savedException.getTransactionId()).isEqualTo(transactionId);
            assertThat(savedException.getInterfaceType()).isEqualTo(InterfaceType.ORDER);
            assertThat(savedException.getExceptionReason()).contains("Field 'customerId': Customer ID is required");
            assertThat(savedException.getExceptionReason()).contains("Field 'orderDate': Order date is invalid");
            assertThat(savedException.getOperation()).isEqualTo("VALIDATION");
            assertThat(savedException.getStatus()).isEqualTo(ExceptionStatus.NEW);
            assertThat(savedException.getCategory()).isEqualTo(ExceptionCategory.VALIDATION);
            assertThat(savedException.getSeverity()).isEqualTo(ExceptionSeverity.MEDIUM);
            assertThat(savedException.getRetryable()).isTrue();
        }
    }

    @Nested
    @DisplayName("Exception Status Management")
    class ExceptionStatusManagementTests {

        @Test
        @DisplayName("Should update exception status from NEW to ACKNOWLEDGED")
        void shouldUpdateExceptionStatusFromNewToAcknowledged() {
            // Given
            String transactionId = "txn-status-123";
            InterfaceException exception = InterfaceException.builder()
                    .id(1L)
                    .transactionId(transactionId)
                    .status(ExceptionStatus.NEW)
                    .build();

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(exception));
            when(exceptionRepository.save(any(InterfaceException.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            InterfaceException result = exceptionProcessingService.updateExceptionStatus(
                    transactionId, ExceptionStatus.ACKNOWLEDGED, "test-user");

            // Then
            assertThat(result.getStatus()).isEqualTo(ExceptionStatus.ACKNOWLEDGED);
            assertThat(result.getAcknowledgedBy()).isEqualTo("test-user");
            assertThat(result.getAcknowledgedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should update exception status from ACKNOWLEDGED to RESOLVED")
        void shouldUpdateExceptionStatusFromAcknowledgedToResolved() {
            // Given
            String transactionId = "txn-status-456";
            InterfaceException exception = InterfaceException.builder()
                    .id(2L)
                    .transactionId(transactionId)
                    .status(ExceptionStatus.ACKNOWLEDGED)
                    .build();

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(exception));
            when(exceptionRepository.save(any(InterfaceException.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            InterfaceException result = exceptionProcessingService.updateExceptionStatus(
                    transactionId, ExceptionStatus.RESOLVED, "test-user");

            // Then
            assertThat(result.getStatus()).isEqualTo(ExceptionStatus.RESOLVED);
            assertThat(result.getResolvedBy()).isEqualTo("test-user");
            assertThat(result.getResolvedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should increment retry count when status updated to RETRIED_FAILED")
        void shouldIncrementRetryCountWhenStatusUpdatedToRetriedFailed() {
            // Given
            String transactionId = "txn-retry-123";
            InterfaceException exception = InterfaceException.builder()
                    .id(3L)
                    .transactionId(transactionId)
                    .status(ExceptionStatus.ACKNOWLEDGED)
                    .retryCount(1)
                    .build();

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(exception));
            when(exceptionRepository.save(any(InterfaceException.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            InterfaceException result = exceptionProcessingService.updateExceptionStatus(
                    transactionId, ExceptionStatus.RETRIED_FAILED, "system");

            // Then
            assertThat(result.getStatus()).isEqualTo(ExceptionStatus.RETRIED_FAILED);
            assertThat(result.getRetryCount()).isEqualTo(2);
            assertThat(result.getLastRetryAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception for invalid status transition")
        void shouldThrowExceptionForInvalidStatusTransition() {
            // Given
            String transactionId = "txn-invalid-123";
            InterfaceException exception = InterfaceException.builder()
                    .id(4L)
                    .transactionId(transactionId)
                    .status(ExceptionStatus.CLOSED)
                    .build();

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(exception));

            // When & Then
            assertThatThrownBy(() -> exceptionProcessingService.updateExceptionStatus(
                    transactionId, ExceptionStatus.NEW, "test-user"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot transition from CLOSED status");
        }

        @Test
        @DisplayName("Should throw exception when transaction ID not found")
        void shouldThrowExceptionWhenTransactionIdNotFound() {
            // Given
            String transactionId = "non-existent-txn";
            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> exceptionProcessingService.updateExceptionStatus(
                    transactionId, ExceptionStatus.ACKNOWLEDGED, "test-user"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Exception not found for transaction: " + transactionId);
        }
    }

    @Nested
    @DisplayName("Retry Count Management")
    class RetryCountManagementTests {

        @Test
        @DisplayName("Should increment retry count successfully")
        void shouldIncrementRetryCountSuccessfully() {
            // Given
            String transactionId = "txn-retry-increment-123";
            InterfaceException exception = InterfaceException.builder()
                    .id(1L)
                    .transactionId(transactionId)
                    .retryCount(2)
                    .build();

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(exception));
            when(exceptionRepository.save(any(InterfaceException.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            InterfaceException result = exceptionProcessingService.incrementRetryCount(transactionId);

            // Then
            assertThat(result.getRetryCount()).isEqualTo(3);
            assertThat(result.getLastRetryAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when incrementing retry count for non-existent transaction")
        void shouldThrowExceptionWhenIncrementingRetryCountForNonExistentTransaction() {
            // Given
            String transactionId = "non-existent-txn";
            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> exceptionProcessingService.incrementRetryCount(transactionId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Exception not found for transaction: " + transactionId);
        }
    }

    // Helper methods to create test events
    private OrderRejectedEvent createOrderRejectedEvent(String transactionId, String rejectedReason, String operation) {
        return OrderRejectedEvent.builder()
                .eventId("event-" + transactionId)
                .eventType("OrderRejected")
                .eventVersion("1.0")
                .occurredOn(testTimestamp)
                .source("order-service")
                .correlationId("corr-" + transactionId)
                .payload(OrderRejectedEvent.OrderRejectedPayload.builder()
                        .transactionId(transactionId)
                        .externalId("EXT-" + transactionId)
                        .operation(operation)
                        .rejectedReason(rejectedReason)
                        .customerId("CUST-123")
                        .locationCode("LOC-001")
                        .build())
                .build();
    }

    private OrderRejectedEvent createOrderRejectedEventWithExternalId(String transactionId, String rejectedReason, String operation, String externalId) {
        return OrderRejectedEvent.builder()
                .eventId("event-" + transactionId)
                .eventType("OrderRejected")
                .eventVersion("1.0")
                .occurredOn(testTimestamp)
                .source("order-service")
                .correlationId("corr-" + transactionId)
                .payload(OrderRejectedEvent.OrderRejectedPayload.builder()
                        .transactionId(transactionId)
                        .externalId(externalId)
                        .operation(operation)
                        .rejectedReason(rejectedReason)
                        .customerId("CUST-123")
                        .locationCode("LOC-001")
                        .build())
                .build();
    }

    private Object createMockOrderData(String externalId) {
        return java.util.Map.of(
                "externalId", externalId,
                "customerId", "CUST001",
                "locationCode", "LOC001",
                "orderDate", "2025-01-15T10:30:00Z",
                "status", "PENDING",
                "totalAmount", 1250.00,
                "currency", "USD",
                "orderItems", java.util.List.of(
                        java.util.Map.of(
                                "itemId", "ITEM001",
                                "productCode", "PROD-ABC123",
                                "bloodType", "O_POS",
                                "productFamily", "RED_BLOOD_CELLS",
                                "quantity", 2,
                                "unitPrice", 500.00,
                                "totalPrice", 1000.00
                        ),
                        java.util.Map.of(
                                "itemId", "ITEM002",
                                "productCode", "PROD-XYZ789",
                                "bloodType", "A_NEG",
                                "productFamily", "PLATELETS",
                                "quantity", 1,
                                "unitPrice", 250.00,
                                "totalPrice", 250.00
                        )
                ),
                "customerInfo", java.util.Map.of(
                        "name", "Test Customer Hospital",
                        "email", "orders@testcustomer.com",
                        "phone", "+1-555-0123",
                        "contactPerson", "Dr. Jane Smith"
                ),
                "metadata", java.util.Map.of(
                        "priority", "URGENT",
                        "deliveryDate", "2025-01-16T08:00:00Z",
                        "specialHandling", true,
                        "temperatureControlled", true
                )
        );
    }

    private OrderCancelledEvent createOrderCancelledEvent(String transactionId, String cancelReason) {
        return OrderCancelledEvent.builder()
                .eventId("event-" + transactionId)
                .eventType("OrderCancelled")
                .eventVersion("1.0")
                .occurredOn(testTimestamp)
                .source("order-service")
                .correlationId("corr-" + transactionId)
                .payload(OrderCancelledEvent.OrderCancelledPayload.builder()
                        .transactionId(transactionId)
                        .externalId("EXT-" + transactionId)
                        .cancelReason(cancelReason)
                        .cancelledBy("customer")
                        .customerId("CUST-123")
                        .build())
                .build();
    }

    private CollectionRejectedEvent createCollectionRejectedEvent(String transactionId, String rejectedReason) {
        return CollectionRejectedEvent.builder()
                .eventId("event-" + transactionId)
                .eventType("CollectionRejected")
                .eventVersion("1.0")
                .occurredOn(testTimestamp)
                .source("collection-service")
                .correlationId("corr-" + transactionId)
                .payload(CollectionRejectedEvent.CollectionRejectedPayload.builder()
                        .transactionId(transactionId)
                        .collectionId("COLL-" + transactionId)
                        .operation("CREATE_COLLECTION")
                        .rejectedReason(rejectedReason)
                        .donorId("DONOR-123")
                        .locationCode("LOC-002")
                        .build())
                .build();
    }

    private DistributionFailedEvent createDistributionFailedEvent(String transactionId, String failureReason) {
        return DistributionFailedEvent.builder()
                .eventId("event-" + transactionId)
                .eventType("DistributionFailed")
                .eventVersion("1.0")
                .occurredOn(testTimestamp)
                .source("distribution-service")
                .correlationId("corr-" + transactionId)
                .payload(DistributionFailedEvent.DistributionFailedPayload.builder()
                        .transactionId(transactionId)
                        .distributionId("DIST-" + transactionId)
                        .operation("CREATE_DISTRIBUTION")
                        .failureReason(failureReason)
                        .customerId("CUST-123")
                        .destinationLocation("DEST-001")
                        .build())
                .build();
    }

    private ValidationErrorEvent createValidationErrorEvent(String transactionId, String interfaceType) {
        List<ValidationErrorEvent.ValidationError> validationErrors = Arrays.asList(
                ValidationErrorEvent.ValidationError.builder()
                        .field("customerId")
                        .rejectedValue(null)
                        .message("Customer ID is required")
                        .errorCode("REQUIRED_FIELD")
                        .build(),
                ValidationErrorEvent.ValidationError.builder()
                        .field("orderDate")
                        .rejectedValue("invalid-date")
                        .message("Order date is invalid")
                        .errorCode("INVALID_FORMAT")
                        .build());

        return ValidationErrorEvent.builder()
                .eventId("event-" + transactionId)
                .eventType("ValidationError")
                .eventVersion("1.0")
                .occurredOn(testTimestamp)
                .source(interfaceType.toLowerCase() + "-service")
                .correlationId("corr-" + transactionId)
                .payload(ValidationErrorEvent.ValidationErrorPayload.builder()
                        .transactionId(transactionId)
                        .interfaceType(interfaceType)
                        .validationErrors(validationErrors)
                        .build())
                .build();
    }
}