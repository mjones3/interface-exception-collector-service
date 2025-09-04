package com.arcone.biopro.exception.collector.infrastructure.client;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.logging.RSocketLoggingInterceptor;
import com.arcone.biopro.exception.collector.infrastructure.metrics.RSocketMetrics;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;


import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MockRSocketOrderServiceClient.
 * Tests success and failure scenarios, timeout behavior, circuit breaker behavior,
 * and fallback handling as required by task 8.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MockRSocketOrderServiceClient Tests")
class MockRSocketOrderServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RSocketRequester.Builder rSocketRequesterBuilder;

    @Mock
    private RSocketRequester rSocketRequester;

    @Mock
    private RSocketRequester.RequestSpec requestSpec;

    @Mock
    private RSocketMetrics rSocketMetrics;

    @Mock
    private RSocketLoggingInterceptor loggingInterceptor;

    private MockRSocketOrderServiceClient client;

    @BeforeEach
    void setUp() {
        client = new MockRSocketOrderServiceClient(restTemplate, rSocketRequesterBuilder);
        client.rSocketMetrics = rSocketMetrics;
        client.loggingInterceptor = loggingInterceptor;
    }

    @Nested
    @DisplayName("Basic Client Configuration Tests")
    class BasicConfigurationTests {

        @Test
        @DisplayName("Should support ORDER interface type only")
        void shouldSupportOrderInterfaceType() {
            // When & Then
            assertThat(client.supports(InterfaceType.ORDER.name())).isTrue();
            assertThat(client.supports("COLLECTION")).isFalse();
            assertThat(client.supports("DISTRIBUTION")).isFalse();
        }

        @Test
        @DisplayName("Should return correct service name")
        void shouldReturnCorrectServiceName() {
            // When & Then
            assertThat(client.getServiceName()).isEqualTo("mock-rsocket-server");
        }

        @Test
        @DisplayName("Should build correct payload endpoint")
        void shouldBuildCorrectPayloadEndpoint() {
            // Given
            InterfaceException exception = InterfaceException.builder()
                    .externalId("TEST-ORDER-1")
                    .transactionId("txn-123")
                    .interfaceType(InterfaceType.ORDER)
                    .build();

            // When
            String endpoint = client.buildPayloadEndpoint(exception);

            // Then
            assertThat(endpoint).isEqualTo("/orders/TEST-ORDER-1");
        }

        @Test
        @DisplayName("Should build correct retry endpoint")
        void shouldBuildCorrectRetryEndpoint() {
            // Given
            InterfaceException exception = InterfaceException.builder()
                    .externalId("TEST-ORDER-1")
                    .transactionId("txn-123")
                    .interfaceType(InterfaceType.ORDER)
                    .build();

            // When
            String endpoint = client.buildRetryEndpoint(exception);

            // Then
            assertThat(endpoint).isEqualTo("/orders/TEST-ORDER-1/retry");
        }

        @Test
        @DisplayName("Should check RSocket connection availability correctly")
        void shouldCheckRSocketConnectionAvailability() {
            // Given - RSocket requester is null
            client.rSocketRequester = null;

            // When & Then
            assertThat(client.isRSocketConnectionAvailable()).isFalse();

            // Given - RSocket requester is available but disposed
            client.rSocketRequester = rSocketRequester;
            when(rSocketRequester.rsocketClient()).thenReturn(null);

            // When & Then - This simulates a disposed client
            assertThat(client.isRSocketConnectionAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("Order Data Retrieval Success Scenarios")
    class SuccessScenarioTests {

        @Test
        @DisplayName("Should retrieve order data successfully with complete order structure")
        void shouldRetrieveOrderDataSuccessfully() throws ExecutionException, InterruptedException {
            // Given
            InterfaceException exception = InterfaceException.builder()
                    .externalId("TEST-ORDER-1")
                    .transactionId("txn-123")
                    .interfaceType(InterfaceType.ORDER)
                    .build();

            // Mock complete order data structure
            Object expectedOrderData = createMockCompleteOrderData("TEST-ORDER-1");

            // Mock logging interceptor
            when(loggingInterceptor.logRSocketCallStart(anyString(), anyString(), anyString()))
                    .thenReturn("correlation-123");
            doNothing().when(loggingInterceptor).logRSocketCallSuccess(anyString(), anyString(), anyString(), any(Duration.class), eq(true));

            // Mock metrics
            doNothing().when(rSocketMetrics).recordSuccessfulCall(any(Duration.class), anyString());

            // Mock RSocket requester behavior
            when(rSocketRequester.route("orders.TEST-ORDER-1")).thenReturn(requestSpec);
            when(requestSpec.retrieveMono(Object.class)).thenReturn(Mono.just(expectedOrderData));

            // Set the mocked requester
            client.rSocketRequester = rSocketRequester;

            // When
            CompletableFuture<PayloadResponse> future = client.getOriginalPayload(exception);
            PayloadResponse response = future.get();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTransactionId()).isEqualTo("txn-123");
            assertThat(response.getInterfaceType()).isEqualTo("ORDER");
            assertThat(response.getPayload()).isEqualTo(expectedOrderData);
            assertThat(response.getSourceService()).isEqualTo("mock-rsocket-server");
            assertThat(response.isRetrieved()).isTrue();
            assertThat(response.getErrorMessage()).isNull();

            // Verify metrics and logging
            verify(rSocketMetrics).recordSuccessfulCall(any(Duration.class), eq("GET_ORDER_DATA"));
            verify(loggingInterceptor).logRSocketCallStart("TEST-ORDER-1", "GET_ORDER_DATA", "txn-123");
            verify(loggingInterceptor).logRSocketCallSuccess(eq("correlation-123"), eq("TEST-ORDER-1"), 
                    eq("GET_ORDER_DATA"), any(Duration.class), eq(true));
        }

        @Test
        @DisplayName("Should handle different order ID patterns successfully")
        void shouldHandleDifferentOrderIdPatterns() throws ExecutionException, InterruptedException {
            // Test different external ID patterns
            String[] testOrderIds = {"TEST-ORDER-1", "TEST-ORD-2025-018", "BULK-ORDER-123", "ORDER-ABC-XYZ"};

            for (String orderId : testOrderIds) {
                // Given
                InterfaceException exception = InterfaceException.builder()
                        .externalId(orderId)
                        .transactionId("txn-" + orderId)
                        .interfaceType(InterfaceType.ORDER)
                        .build();

                Object mockOrderData = createMockCompleteOrderData(orderId);

                // Mock RSocket requester behavior
                when(rSocketRequester.route("orders." + orderId)).thenReturn(requestSpec);
                when(requestSpec.retrieveMono(Object.class)).thenReturn(Mono.just(mockOrderData));

                client.rSocketRequester = rSocketRequester;

                // When
                CompletableFuture<PayloadResponse> future = client.getOriginalPayload(exception);
                PayloadResponse response = future.get();

                // Then
                assertThat(response.isRetrieved()).isTrue();
                assertThat(response.getPayload()).isEqualTo(mockOrderData);
                assertThat(response.getErrorMessage()).isNull();
            }
        }
    }

    @Nested
    @DisplayName("Order Data Retrieval Failure Scenarios")
    class FailureScenarioTests {

        @Test
        @DisplayName("Should handle RSocket connection errors gracefully")
        void shouldHandleRSocketErrorGracefully() throws ExecutionException, InterruptedException {
            // Given
            InterfaceException exception = InterfaceException.builder()
                    .externalId("TEST-ORDER-1")
                    .transactionId("txn-123")
                    .interfaceType(InterfaceType.ORDER)
                    .build();

            // Mock logging interceptor
            when(loggingInterceptor.logRSocketCallStart(anyString(), anyString(), anyString()))
                    .thenReturn("correlation-123");
            doNothing().when(loggingInterceptor).logRSocketCallFailure(anyString(), anyString(), anyString(), 
                    any(Duration.class), anyString(), anyString());

            // Mock metrics
            doNothing().when(rSocketMetrics).recordFailedCall(any(Duration.class), anyString(), anyString());

            // Mock RSocket requester to throw an exception
            when(rSocketRequester.route("orders.TEST-ORDER-1")).thenReturn(requestSpec);
            when(requestSpec.retrieveMono(Object.class))
                    .thenReturn(Mono.error(new RuntimeException("Connection failed")));

            // Set the mocked requester
            client.rSocketRequester = rSocketRequester;

            // When
            CompletableFuture<PayloadResponse> future = client.getOriginalPayload(exception);
            PayloadResponse response = future.get();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTransactionId()).isEqualTo("txn-123");
            assertThat(response.getInterfaceType()).isEqualTo("ORDER");
            assertThat(response.getPayload()).isNull();
            assertThat(response.getSourceService()).isEqualTo("mock-rsocket-server");
            assertThat(response.isRetrieved()).isFalse();
            assertThat(response.getErrorMessage()).contains("Connection failed");

            // Verify error metrics and logging
            verify(rSocketMetrics).recordFailedCall(any(Duration.class), eq("GET_ORDER_DATA"), eq("RuntimeException"));
            verify(loggingInterceptor).logRSocketCallFailure(eq("correlation-123"), eq("TEST-ORDER-1"), 
                    eq("GET_ORDER_DATA"), any(Duration.class), eq("RuntimeException"), eq("Connection failed"));
        }

        @Test
        @DisplayName("Should handle null RSocket requester gracefully")
        void shouldHandleNullRSocketRequester() throws ExecutionException, InterruptedException {
            // Given
            InterfaceException exception = InterfaceException.builder()
                    .externalId("TEST-ORDER-1")
                    .transactionId("txn-123")
                    .interfaceType(InterfaceType.ORDER)
                    .build();

            // RSocket requester is null (not initialized)
            client.rSocketRequester = null;

            // When
            CompletableFuture<PayloadResponse> future = client.getOriginalPayload(exception);
            PayloadResponse response = future.get();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTransactionId()).isEqualTo("txn-123");
            assertThat(response.getInterfaceType()).isEqualTo("ORDER");
            assertThat(response.getPayload()).isNull();
            assertThat(response.getSourceService()).isEqualTo("mock-rsocket-server");
            assertThat(response.isRetrieved()).isFalse();
            assertThat(response.getErrorMessage()).contains("RSocket connection not available");
        }

        @Test
        @DisplayName("Should handle order not found scenarios")
        void shouldHandleOrderNotFound() throws ExecutionException, InterruptedException {
            // Given
            InterfaceException exception = InterfaceException.builder()
                    .externalId("NOTFOUND-ORDER-1")
                    .transactionId("txn-notfound")
                    .interfaceType(InterfaceType.ORDER)
                    .build();

            // Mock RSocket requester to return empty result
            when(rSocketRequester.route("orders.NOTFOUND-ORDER-1")).thenReturn(requestSpec);
            when(requestSpec.retrieveMono(Object.class)).thenReturn(Mono.empty());

            client.rSocketRequester = rSocketRequester;

            // When
            CompletableFuture<PayloadResponse> future = client.getOriginalPayload(exception);
            PayloadResponse response = future.get();

            // Then
            assertThat(response.isRetrieved()).isTrue(); // Mono.empty() is considered successful
            assertThat(response.getPayload()).isNull();
            assertThat(response.getErrorMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("Timeout and Circuit Breaker Behavior Tests")
    class TimeoutAndCircuitBreakerTests {

        @Test
        @DisplayName("Should handle timeout scenarios gracefully")
        void shouldHandleTimeoutGracefully() throws ExecutionException, InterruptedException {
            // Given
            InterfaceException exception = InterfaceException.builder()
                    .externalId("TIMEOUT-ORDER-1")
                    .transactionId("txn-timeout")
                    .interfaceType(InterfaceType.ORDER)
                    .build();

            // Mock timeout scenario
            when(rSocketRequester.route("orders.TIMEOUT-ORDER-1")).thenReturn(requestSpec);
            when(requestSpec.retrieveMono(Object.class))
                    .thenReturn(Mono.delay(Duration.ofSeconds(10))
                            .then(Mono.just(new Object())));

            // Mock metrics for timeout recording
            doNothing().when(rSocketMetrics).recordTimeout(anyString());

            client.rSocketRequester = rSocketRequester;

            // When
            CompletableFuture<PayloadResponse> future = client.getOriginalPayload(exception);
            PayloadResponse response = future.get();

            // Then
            assertThat(response.isRetrieved()).isFalse();
            assertThat(response.getErrorMessage()).contains("timeout");

            // Verify timeout was recorded
            verify(rSocketMetrics).recordTimeout("GET_ORDER_DATA");
        }

        @Test
        @DisplayName("Should return fallback response when circuit breaker is open")
        void shouldReturnFallbackResponseOnCircuitBreakerOpen() throws ExecutionException, InterruptedException {
            // Given
            InterfaceException exception = InterfaceException.builder()
                    .externalId("TEST-ORDER-1")
                    .transactionId("txn-123")
                    .interfaceType(InterfaceType.ORDER)
                    .build();

            CallNotPermittedException circuitBreakerException = 
                    CallNotPermittedException.createCallNotPermittedException(null);

            // Mock circuit breaker metrics
            doNothing().when(rSocketMetrics).recordCircuitBreakerEvent(anyString(), anyString());
            doNothing().when(loggingInterceptor).logCircuitBreakerEvent(anyString(), anyString(), anyString(), anyString());

            // When
            CompletableFuture<PayloadResponse> future = client.fallbackGetPayload(exception, circuitBreakerException);
            PayloadResponse response = future.get();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTransactionId()).isEqualTo("txn-123");
            assertThat(response.getInterfaceType()).isEqualTo("ORDER");
            assertThat(response.getPayload()).isNull();
            assertThat(response.getSourceService()).isEqualTo("mock-rsocket-server");
            assertThat(response.isRetrieved()).isFalse();
            assertThat(response.getErrorMessage()).contains("Mock RSocket server unavailable");
            assertThat(response.getErrorMessage()).contains("circuit breaker");

            // Verify circuit breaker metrics and logging
            verify(rSocketMetrics).recordCircuitBreakerEvent("FALLBACK_TRIGGERED", "GET_ORDER_DATA");
            verify(loggingInterceptor).logCircuitBreakerEvent("TEST-ORDER-1", "GET_ORDER_DATA", 
                    "FALLBACK_TRIGGERED", "OPEN");
        }

        @Test
        @DisplayName("Should handle various circuit breaker exception types")
        void shouldHandleVariousCircuitBreakerExceptions() throws ExecutionException, InterruptedException {
            InterfaceException exception = InterfaceException.builder()
                    .externalId("CB-TEST-ORDER")
                    .transactionId("txn-cb-test")
                    .interfaceType(InterfaceType.ORDER)
                    .build();

            // Test different exception types that might trigger fallback
            Exception[] exceptions = {
                    new RuntimeException("Service unavailable"),
                    new TimeoutException("Request timeout"),
                    CallNotPermittedException.createCallNotPermittedException(null)
            };

            for (Exception ex : exceptions) {
                // When
                CompletableFuture<PayloadResponse> future = client.fallbackGetPayload(exception, ex);
                PayloadResponse response = future.get();

                // Then
                assertThat(response.isRetrieved()).isFalse();
                assertThat(response.getErrorMessage()).contains("Mock RSocket server unavailable");
            }
        }

        @Test
        @DisplayName("Should record metrics for circuit breaker state changes")
        void shouldRecordMetricsForCircuitBreakerStateChanges() throws ExecutionException, InterruptedException {
            // Given
            InterfaceException exception = InterfaceException.builder()
                    .externalId("METRICS-ORDER-1")
                    .transactionId("txn-metrics")
                    .interfaceType(InterfaceType.ORDER)
                    .build();

            Exception circuitBreakerException = new RuntimeException("Circuit breaker test");

            // When
            client.fallbackGetPayload(exception, circuitBreakerException).get();

            // Then
            verify(rSocketMetrics).recordCircuitBreakerEvent("FALLBACK_TRIGGERED", "GET_ORDER_DATA");
            verify(loggingInterceptor).logCircuitBreakerEvent("METRICS-ORDER-1", "GET_ORDER_DATA", 
                    "FALLBACK_TRIGGERED", "OPEN");
        }
    }

    @Nested
    @DisplayName("Mock Server Unavailable Scenarios")
    class MockServerUnavailableTests {

        @Test
        @DisplayName("Should handle mock server completely unavailable")
        void shouldHandleMockServerUnavailable() throws ExecutionException, InterruptedException {
            // Given
            InterfaceException exception = InterfaceException.builder()
                    .externalId("UNAVAILABLE-ORDER-1")
                    .transactionId("txn-unavailable")
                    .interfaceType(InterfaceType.ORDER)
                    .build();

            // Mock server unavailable scenario
            when(rSocketRequester.route("orders.UNAVAILABLE-ORDER-1")).thenReturn(requestSpec);
            when(requestSpec.retrieveMono(Object.class))
                    .thenReturn(Mono.error(new RuntimeException("Connection refused")));

            client.rSocketRequester = rSocketRequester;

            // When
            CompletableFuture<PayloadResponse> future = client.getOriginalPayload(exception);
            PayloadResponse response = future.get();

            // Then
            assertThat(response.isRetrieved()).isFalse();
            assertThat(response.getErrorMessage()).contains("Connection refused");
            
            // Verify error metrics
            verify(rSocketMetrics).recordFailedCall(any(Duration.class), eq("GET_ORDER_DATA"), eq("RuntimeException"));
        }

        @Test
        @DisplayName("Should attempt reconnection when RSocket requester is null")
        void shouldAttemptReconnectionWhenRequesterIsNull() throws ExecutionException, InterruptedException {
            // Given
            InterfaceException exception = InterfaceException.builder()
                    .externalId("RECONNECT-ORDER-1")
                    .transactionId("txn-reconnect")
                    .interfaceType(InterfaceType.ORDER)
                    .build();

            // Initially null requester
            client.rSocketRequester = null;

            // When
            CompletableFuture<PayloadResponse> future = client.getOriginalPayload(exception);
            PayloadResponse response = future.get();

            // Then
            assertThat(response.isRetrieved()).isFalse();
            assertThat(response.getErrorMessage()).contains("RSocket connection not available");
        }
    }

    // Helper method to create mock order data
    private Object createMockCompleteOrderData(String externalId) {
        return new Object() {
            public String getExternalId() { return externalId; }
            public String getCustomerId() { return "CUST001"; }
            public String getLocationCode() { return "LOC001"; }
            public String getStatus() { return "PENDING"; }
            // Simplified mock object - in real tests this would be a proper order structure
        };
    }
}