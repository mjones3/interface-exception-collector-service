package com.arcone.biopro.exception.collector.integration;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent;
import com.arcone.biopro.exception.collector.infrastructure.client.MockRSocketOrderServiceClient;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for Mock RSocket Server integration using TestContainers.
 * Tests complete OrderRejected event processing flow with order data retrieval,
 * circuit breaker behavior, and failure scenarios.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"OrderRejected"})
@TestPropertySource(properties = {
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=mock-rsocket-integration-test-group",
        "app.security.jwt.secret=mockRSocketIntegrationTestSecret1234567890123456789012345678901234567890",
        "app.security.rate-limit.enabled=false",
        "app.rsocket.mock-server.enabled=true",
        "app.rsocket.mock-server.timeout=10s",
        "app.rsocket.mock-server.connection-timeout=15s",
        "logging.level.com.arcone.biopro.exception.collector.infrastructure.client=DEBUG",
        "logging.level.io.github.resilience4j=DEBUG"
})
@DirtiesContext
class MockRSocketServerIntegrationTest {

    private static final Network network = Network.newNetwork();

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("mock_rsocket_integration_test_db")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withNetwork(network)
            .withNetworkAliases("postgres");

    @Container
    static GenericContainer<?> mockRSocketServer = new GenericContainer<>("artifactory.sha.ao.arc-one.com/docker/biopro/utils/rsocket_mock:21.0.1")
            .withNetwork(network)
            .withNetworkAliases("mock-rsocket-server")
            .withExposedPorts(7000)
            .withEnv("SERVER_PORT", "7000")
            .withEnv("MAPPINGS_PATH", "/app/mappings")
            .withEnv("RESPONSES_PATH", "/app/__files")
            .withEnv("LOG_LEVEL", "DEBUG")
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("test-mappings/"),
                "/app/mappings/"
            )
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("test-responses/"),
                "/app/__files/"
            )
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)))
            .withStartupTimeout(Duration.ofSeconds(60));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.rsocket.mock-server.host", mockRSocketServer::getHost);
        registry.add("app.rsocket.mock-server.port", () -> mockRSocketServer.getMappedPort(7000));
    }

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private MockRSocketOrderServiceClient mockRSocketClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    private Producer<String, Object> kafkaProducer;

    @BeforeEach
    void setUp() {
        // Clear database
        exceptionRepository.deleteAll();

        // Reset circuit breaker
        circuitBreakerRegistry.circuitBreaker("mock-rsocket-server").reset();

        // Setup Kafka producer
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producerProps.put("key.serializer", StringSerializer.class);
        producerProps.put("value.serializer", JsonSerializer.class);
        kafkaProducer = new DefaultKafkaProducerFactory<String, Object>(producerProps).createProducer();
    }

    @Test
    @DisplayName("Should process OrderRejected event and retrieve order data from mock RSocket server")
    void shouldProcessOrderRejectedEventWithOrderDataRetrieval() throws Exception {
        // Given - Create OrderRejected event with known externalId
        String transactionId = "mock-rsocket-integration-001";
        String externalId = "TEST-ORDER-1";
        OrderRejectedEvent orderEvent = createOrderRejectedEvent(transactionId, externalId, 
                "Order validation failed", OrderRejectedEvent.OrderOperation.CREATE_ORDER);

        // When - Publish OrderRejected event
        kafkaProducer.send(new ProducerRecord<>("OrderRejected", transactionId, orderEvent));
        kafkaProducer.flush();

        // Then - Wait for exception to be processed with order data
        await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
                    assertThat(exception).isPresent();
                    
                    InterfaceException ex = exception.get();
                    assertThat(ex.getInterfaceType()).isEqualTo(InterfaceType.ORDER);
                    assertThat(ex.getStatus()).isEqualTo(ExceptionStatus.NEW);
                    assertThat(ex.getExceptionReason()).isEqualTo("Order validation failed");
                    assertThat(ex.getExternalId()).isEqualTo(externalId);
                    
                    // Verify order data was retrieved and stored
                    assertThat(ex.getOrderRetrievalAttempted()).isTrue();
                    assertThat(ex.getOrderReceived()).isNotNull();
                    assertThat(ex.getOrderRetrievedAt()).isNotNull();
                    assertThat(ex.getOrderRetrievalError()).isNull();
                    assertThat(ex.getRetryable()).isTrue();
                    
                    // Verify order data structure
                    Map<String, Object> orderData = (Map<String, Object>) ex.getOrderReceived();
                    assertThat(orderData).containsKey("externalId");
                    assertThat(orderData).containsKey("customerId");
                    assertThat(orderData).containsKey("orderItems");
                    assertThat(orderData.get("externalId")).isEqualTo("TEST-ORDER-1");
                    assertThat(orderData.get("customerId")).isEqualTo("CUST001");
                });
    }

    @Test
    @DisplayName("Should handle order not found scenario gracefully")
    void shouldHandleOrderNotFoundScenario() throws Exception {
        // Given - Create OrderRejected event with not-found externalId pattern
        String transactionId = "mock-rsocket-not-found-001";
        String externalId = "NOTFOUND-ORDER-123";
        OrderRejectedEvent orderEvent = createOrderRejectedEvent(transactionId, externalId, 
                "Order processing failed", OrderRejectedEvent.OrderOperation.CREATE_ORDER);

        // When - Publish OrderRejected event
        kafkaProducer.send(new ProducerRecord<>("OrderRejected", transactionId, orderEvent));
        kafkaProducer.flush();

        // Then - Wait for exception to be processed without order data
        await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
                    assertThat(exception).isPresent();
                    
                    InterfaceException ex = exception.get();
                    assertThat(ex.getOrderRetrievalAttempted()).isTrue();
                    assertThat(ex.getOrderReceived()).isNull();
                    assertThat(ex.getOrderRetrievalError()).isNotNull();
                    assertThat(ex.getRetryable()).isFalse(); // Not retryable when order not found
                });
    }

    @Test
    @DisplayName("Should test direct RSocket client order data retrieval")
    void shouldTestDirectRSocketClientOrderDataRetrieval() throws Exception {
        // Given - Create test exception
        InterfaceException testException = InterfaceException.builder()
                .transactionId("direct-rsocket-test-001")
                .interfaceType(InterfaceType.ORDER)
                .externalId("TEST-ORDER-1")
                .exceptionReason("Test direct retrieval")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.MEDIUM)
                .category(ExceptionCategory.VALIDATION)
                .retryable(true)
                .customerId("CUST-DIRECT-001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryCount(0)
                .build();

        // When - Call RSocket client directly
        CompletableFuture<PayloadResponse> future = mockRSocketClient.getOriginalPayload(testException);
        PayloadResponse response = future.get(15, TimeUnit.SECONDS);

        // Then - Verify successful response
        assertThat(response).isNotNull();
        assertThat(response.isRetrieved()).isTrue();
        assertThat(response.getPayload()).isNotNull();
        assertThat(response.getSourceService()).isEqualTo("mock-rsocket-server");
        assertThat(response.getErrorMessage()).isNull();

        // Verify order data structure
        Map<String, Object> orderData = (Map<String, Object>) response.getPayload();
        assertThat(orderData).containsKey("externalId");
        assertThat(orderData).containsKey("orderItems");
        assertThat(orderData.get("externalId")).isEqualTo("TEST-ORDER-1");
    }

    @Test
    @DisplayName("Should test RSocket client with not found order")
    void shouldTestRSocketClientWithNotFoundOrder() throws Exception {
        // Given - Create test exception with not-found pattern
        InterfaceException testException = InterfaceException.builder()
                .transactionId("direct-rsocket-not-found-001")
                .interfaceType(InterfaceType.ORDER)
                .externalId("NOTFOUND-ORDER-456")
                .exceptionReason("Test not found scenario")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.MEDIUM)
                .category(ExceptionCategory.VALIDATION)
                .retryable(true)
                .customerId("CUST-NOT-FOUND-001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryCount(0)
                .build();

        // When - Call RSocket client directly
        CompletableFuture<PayloadResponse> future = mockRSocketClient.getOriginalPayload(testException);
        PayloadResponse response = future.get(15, TimeUnit.SECONDS);

        // Then - Verify error response
        assertThat(response).isNotNull();
        assertThat(response.isRetrieved()).isFalse();
        assertThat(response.getPayload()).isNull();
        assertThat(response.getSourceService()).isEqualTo("mock-rsocket-server");
        assertThat(response.getErrorMessage()).isNotNull();
    }

    @Test
    @DisplayName("Should test circuit breaker behavior under failure conditions")
    void shouldTestCircuitBreakerBehaviorUnderFailureConditions() throws Exception {
        // Given - Stop mock server to simulate failure
        mockRSocketServer.stop();
        
        // Wait for container to be fully stopped
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(mockRSocketServer.isRunning()).isFalse());

        InterfaceException testException = InterfaceException.builder()
                .transactionId("circuit-breaker-test-001")
                .interfaceType(InterfaceType.ORDER)
                .externalId("TEST-CIRCUIT-BREAKER")
                .exceptionReason("Test circuit breaker")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.MEDIUM)
                .category(ExceptionCategory.NETWORK_ERROR)
                .retryable(true)
                .customerId("CUST-CB-001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryCount(0)
                .build();

        // When - Make multiple calls to trigger circuit breaker
        for (int i = 0; i < 6; i++) { // Exceed failure threshold
            try {
                CompletableFuture<PayloadResponse> future = mockRSocketClient.getOriginalPayload(testException);
                PayloadResponse response = future.get(5, TimeUnit.SECONDS);
                
                // All calls should fail but return fallback response
                assertThat(response.isRetrieved()).isFalse();
                assertThat(response.getErrorMessage()).isNotNull();
            } catch (Exception e) {
                // Expected for some calls due to timeout/connection failure
            }
        }

        // Then - Verify circuit breaker is open
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var circuitBreaker = circuitBreakerRegistry.circuitBreaker("mock-rsocket-server");
                    assertThat(circuitBreaker.getState()).isIn(
                        io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN,
                        io.github.resilience4j.circuitbreaker.CircuitBreaker.State.HALF_OPEN
                    );
                });

        // When circuit breaker is open, calls should immediately return fallback
        CompletableFuture<PayloadResponse> fallbackFuture = mockRSocketClient.getOriginalPayload(testException);
        PayloadResponse fallbackResponse = fallbackFuture.get(2, TimeUnit.SECONDS);

        assertThat(fallbackResponse.isRetrieved()).isFalse();
        assertThat(fallbackResponse.getErrorMessage()).contains("circuit breaker");
    }

    @Test
    @DisplayName("Should test circuit breaker recovery when service becomes available")
    void shouldTestCircuitBreakerRecoveryWhenServiceBecomesAvailable() throws Exception {
        // Given - First trigger circuit breaker by stopping server
        mockRSocketServer.stop();
        
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(mockRSocketServer.isRunning()).isFalse());

        InterfaceException testException = InterfaceException.builder()
                .transactionId("circuit-breaker-recovery-001")
                .interfaceType(InterfaceType.ORDER)
                .externalId("TEST-RECOVERY")
                .exceptionReason("Test circuit breaker recovery")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.MEDIUM)
                .category(ExceptionCategory.NETWORK_ERROR)
                .retryable(true)
                .customerId("CUST-RECOVERY-001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryCount(0)
                .build();

        // Trigger circuit breaker
        for (int i = 0; i < 6; i++) {
            try {
                mockRSocketClient.getOriginalPayload(testException).get(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                // Expected failures
            }
        }

        // Verify circuit breaker is open
        var circuitBreaker = circuitBreakerRegistry.circuitBreaker("mock-rsocket-server");
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    assertThat(circuitBreaker.getState()).isIn(
                        io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN,
                        io.github.resilience4j.circuitbreaker.CircuitBreaker.State.HALF_OPEN
                    );
                });

        // When - Restart mock server
        mockRSocketServer.start();
        
        await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> assertThat(mockRSocketServer.isRunning()).isTrue());

        // Wait for circuit breaker to transition to half-open and then closed
        Thread.sleep(5000); // Wait for circuit breaker wait duration

        // Then - Verify service recovery
        testException.setExternalId("TEST-ORDER-1"); // Use valid test data
        CompletableFuture<PayloadResponse> recoveryFuture = mockRSocketClient.getOriginalPayload(testException);
        PayloadResponse recoveryResponse = recoveryFuture.get(15, TimeUnit.SECONDS);

        // Should eventually succeed when circuit breaker closes
        await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    CompletableFuture<PayloadResponse> future = mockRSocketClient.getOriginalPayload(testException);
                    PayloadResponse response = future.get(10, TimeUnit.SECONDS);
                    assertThat(response.isRetrieved()).isTrue();
                });
    }

    @Test
    @DisplayName("Should test end-to-end OrderRejected processing with database storage")
    void shouldTestEndToEndOrderRejectedProcessingWithDatabaseStorage() throws Exception {
        // Given - Multiple OrderRejected events with different scenarios
        String[] testCases = {
            "E2E-SUCCESS-001:TEST-ORDER-1",
            "E2E-SUCCESS-002:TEST-ORDER-2", 
            "E2E-NOTFOUND-001:NOTFOUND-ORDER-789"
        };

        // When - Publish all events
        for (String testCase : testCases) {
            String[] parts = testCase.split(":");
            String transactionId = parts[0];
            String externalId = parts[1];
            
            OrderRejectedEvent event = createOrderRejectedEvent(transactionId, externalId,
                    "End-to-end test scenario", OrderRejectedEvent.OrderOperation.CREATE_ORDER);
            
            kafkaProducer.send(new ProducerRecord<>("OrderRejected", transactionId, event));
        }
        kafkaProducer.flush();

        // Then - Verify all exceptions are processed correctly
        await().atMost(Duration.ofSeconds(45))
                .untilAsserted(() -> {
                    long totalExceptions = exceptionRepository.count();
                    assertThat(totalExceptions).isEqualTo(3);
                    
                    // Verify successful order data retrieval cases
                    Optional<InterfaceException> successCase1 = exceptionRepository.findByTransactionId("E2E-SUCCESS-001");
                    assertThat(successCase1).isPresent();
                    assertThat(successCase1.get().getOrderReceived()).isNotNull();
                    assertThat(successCase1.get().getRetryable()).isTrue();
                    
                    Optional<InterfaceException> successCase2 = exceptionRepository.findByTransactionId("E2E-SUCCESS-002");
                    assertThat(successCase2).isPresent();
                    assertThat(successCase2.get().getOrderReceived()).isNotNull();
                    assertThat(successCase2.get().getRetryable()).isTrue();
                    
                    // Verify not found case
                    Optional<InterfaceException> notFoundCase = exceptionRepository.findByTransactionId("E2E-NOTFOUND-001");
                    assertThat(notFoundCase).isPresent();
                    assertThat(notFoundCase.get().getOrderReceived()).isNull();
                    assertThat(notFoundCase.get().getOrderRetrievalError()).isNotNull();
                    assertThat(notFoundCase.get().getRetryable()).isFalse();
                });
    }

    @Test
    @DisplayName("Should verify mock server container health and connectivity")
    void shouldVerifyMockServerContainerHealthAndConnectivity() {
        // Then - Verify container is running and healthy
        assertThat(mockRSocketServer.isRunning()).isTrue();
        assertThat(mockRSocketServer.getMappedPort(7000)).isGreaterThan(0);
        
        // Verify RSocket client connection
        assertThat(mockRSocketClient.isRSocketConnectionAvailable()).isTrue();
        
        // Verify container logs contain expected startup messages
        String logs = mockRSocketServer.getLogs();
        assertThat(logs).isNotEmpty();
    }

    private OrderRejectedEvent createOrderRejectedEvent(String transactionId, String externalId, 
                                                       String rejectedReason, OrderRejectedEvent.OrderOperation operation) {
        return OrderRejectedEvent.builder()
                .eventId("event-" + transactionId)
                .eventType("OrderRejected")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("order-service")
                .correlationId("corr-" + transactionId)
                .payload(OrderRejectedEvent.OrderRejectedPayload.builder()
                        .transactionId(transactionId)
                        .externalId(externalId)
                        .operation(operation)
                        .rejectedReason(rejectedReason)
                        .customerId("CUST-INTEGRATION-001")
                        .locationCode("LOC-INTEGRATION-001")
                        .build())
                .build();
    }
}