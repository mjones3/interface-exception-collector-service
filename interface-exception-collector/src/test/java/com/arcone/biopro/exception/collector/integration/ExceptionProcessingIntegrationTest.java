package com.arcone.biopro.exception.collector.integration;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionCapturedEvent;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for complete exception processing workflows.
 * Tests end-to-end flows from Kafka event consumption to database storage,
 * API access, and retry operations.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {
        "OrderRejected",
        "CollectionRejected",
        "DistributionFailed",
        "ValidationError",
        "ExceptionCaptured",
        "ExceptionResolved",
        "CriticalExceptionAlert"
})
@TestPropertySource(properties = {
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=integration-test-group",
        "app.security.jwt.secret=integrationTestSecret1234567890123456789012345678901234567890",
        "app.security.rate-limit.enabled=false"
})
@DirtiesContext
class ExceptionProcessingIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("integration_test_db")
            .withUsername("test_user")
            .withPassword("test_pass");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private RetryAttemptRepository retryAttemptRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private Producer<String, Object> kafkaProducer;
    private Consumer<String, Object> kafkaConsumer;

    @BeforeEach
    void setUp() {
        // Clear database
        retryAttemptRepository.deleteAll();
        exceptionRepository.deleteAll();

        // Setup Kafka producer
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producerProps.put("key.serializer", StringSerializer.class);
        producerProps.put("value.serializer", JsonSerializer.class);
        kafkaProducer = new DefaultKafkaProducerFactory<String, Object>(producerProps).createProducer();

        // Setup Kafka consumer for outbound events
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-consumer", "false", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        kafkaConsumer = new DefaultKafkaConsumerFactory<String, Object>(consumerProps).createConsumer();
    }

    @Test
    @DisplayName("Complete Order Exception Processing Workflow")
    void shouldProcessCompleteOrderExceptionWorkflow() throws Exception {
        // Given - Create OrderRejected event
        String transactionId = "integration-test-order-001";
        OrderRejectedEvent orderEvent = createOrderRejectedEvent(transactionId, "Order validation failed",
                "CREATE_ORDER");

        // Subscribe to outbound events
        kafkaConsumer.subscribe(Collections.singletonList("ExceptionCaptured"));

        // When - Publish inbound event
        kafkaProducer.send(new ProducerRecord<>("OrderRejected", transactionId, orderEvent));
        kafkaProducer.flush();

        // Then - Wait for exception to be processed and stored
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
                    assertThat(exception).isPresent();
                    assertThat(exception.get().getInterfaceType()).isEqualTo(InterfaceType.ORDER);
                    assertThat(exception.get().getStatus()).isEqualTo(ExceptionStatus.NEW);
                    assertThat(exception.get().getExceptionReason()).isEqualTo("Order validation failed");
                });

        // Verify outbound event was published
        ConsumerRecords<String, Object> records = kafkaConsumer.poll(Duration.ofSeconds(5));
        assertThat(records).isNotEmpty();

        ConsumerRecord<String, Object> capturedEventRecord = records.iterator().next();
        assertThat(capturedEventRecord.topic()).isEqualTo("ExceptionCaptured");

        // Test API access
        String apiUrl = "http://localhost:" + port + "/api/v1/exceptions/" + transactionId;
        ResponseEntity<String> apiResponse = restTemplate.getForEntity(apiUrl, String.class);
        assertThat(apiResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Test acknowledgment
        String ackUrl = "http://localhost:" + port + "/api/v1/exceptions/" + transactionId + "/acknowledge";
        Map<String, String> ackRequest = Map.of(
                "acknowledgedBy", "integration-test-user",
                "notes", "Acknowledged during integration test");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> ackEntity = new HttpEntity<>(ackRequest, headers);

        ResponseEntity<String> ackResponse = restTemplate.exchange(ackUrl, HttpMethod.PUT, ackEntity, String.class);
        assertThat(ackResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify status update
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
                    assertThat(exception).isPresent();
                    assertThat(exception.get().getStatus()).isEqualTo(ExceptionStatus.ACKNOWLEDGED);
                    assertThat(exception.get().getAcknowledgedBy()).isEqualTo("integration-test-user");
                });
    }

    @Test
    @DisplayName("Exception Retry Workflow with External Service Integration")
    void shouldProcessExceptionRetryWorkflow() throws Exception {
        // Given - Create exception in database
        String transactionId = "integration-test-retry-001";
        InterfaceException exception = InterfaceException.builder()
                .transactionId(transactionId)
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Temporary service unavailable")
                .operation("CREATE_ORDER")
                .externalId("ORDER-RETRY-001")
                .status(ExceptionStatus.ACKNOWLEDGED)
                .severity(ExceptionSeverity.HIGH)
                .category(ExceptionCategory.NETWORK_ERROR)
                .retryable(true)
                .customerId("CUST-RETRY-001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryCount(0)
                .build();

        exceptionRepository.save(exception);

        // When - Initiate retry via API
        String retryUrl = "http://localhost:" + port + "/api/v1/exceptions/" + transactionId + "/retry";
        Map<String, Object> retryRequest = Map.of(
                "reason", "Integration test retry",
                "priority", "HIGH",
                "notifyOnCompletion", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> retryEntity = new HttpEntity<>(retryRequest, headers);

        ResponseEntity<String> retryResponse = restTemplate.postForEntity(retryUrl, retryEntity, String.class);
        assertThat(retryResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // Then - Verify retry attempt was created
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    List<RetryAttempt> attempts = retryAttemptRepository
                            .findByExceptionIdOrderByAttemptNumber(exception.getId());
                    assertThat(attempts).hasSize(1);
                    assertThat(attempts.get(0).getStatus()).isIn(RetryStatus.PENDING, RetryStatus.SUCCESS,
                            RetryStatus.FAILED);
                    assertThat(attempts.get(0).getInitiatedBy()).isEqualTo("system"); // Default for API calls
                });

        // Verify exception retry count was updated
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Optional<InterfaceException> updatedException = exceptionRepository
                            .findByTransactionId(transactionId);
                    assertThat(updatedException).isPresent();
                    assertThat(updatedException.get().getRetryCount()).isGreaterThan(0);
                });
    }

    @Test
    @DisplayName("Critical Exception Alerting Workflow")
    void shouldProcessCriticalExceptionAlertingWorkflow() throws Exception {
        // Given - Create critical exception event
        String transactionId = "integration-test-critical-001";
        OrderRejectedEvent criticalEvent = createOrderRejectedEvent(transactionId, "System error occurred",
                "CREATE_ORDER");

        // Subscribe to critical alert events
        kafkaConsumer.subscribe(Collections.singletonList("CriticalExceptionAlert"));

        // When - Publish critical event
        kafkaProducer.send(new ProducerRecord<>("OrderRejected", transactionId, criticalEvent));
        kafkaProducer.flush();

        // Then - Wait for exception to be processed
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
                    assertThat(exception).isPresent();
                    assertThat(exception.get().getSeverity()).isEqualTo(ExceptionSeverity.CRITICAL);
                });

        // Verify critical alert was published
        ConsumerRecords<String, Object> records = kafkaConsumer.poll(Duration.ofSeconds(5));
        assertThat(records).isNotEmpty();

        ConsumerRecord<String, Object> alertRecord = records.iterator().next();
        assertThat(alertRecord.topic()).isEqualTo("CriticalExceptionAlert");
    }

    @Test
    @DisplayName("Exception Search and Filtering Workflow")
    void shouldProcessExceptionSearchAndFilteringWorkflow() throws Exception {
        // Given - Create multiple exceptions
        createTestExceptions();

        // Wait for all exceptions to be processed
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    long count = exceptionRepository.count();
                    assertThat(count).isEqualTo(3);
                });

        // When & Then - Test various API endpoints

        // Test list with filters
        String listUrl = "http://localhost:" + port
                + "/api/v1/exceptions?interfaceType=ORDER&status=NEW&page=0&size=10";
        ResponseEntity<String> listResponse = restTemplate.getForEntity(listUrl, String.class);
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Test search functionality
        String searchUrl = "http://localhost:" + port
                + "/api/v1/exceptions/search?query=validation&fields=exceptionReason";
        ResponseEntity<String> searchResponse = restTemplate.getForEntity(searchUrl, String.class);
        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Test summary endpoint
        String summaryUrl = "http://localhost:" + port + "/api/v1/exceptions/summary?timeRange=today";
        ResponseEntity<String> summaryResponse = restTemplate.getForEntity(summaryUrl, String.class);
        assertThat(summaryResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Exception Lifecycle State Management Workflow")
    void shouldProcessExceptionLifecycleStateManagementWorkflow() throws Exception {
        // Given - Create exception
        String transactionId = "integration-test-lifecycle-001";
        InterfaceException exception = InterfaceException.builder()
                .transactionId(transactionId)
                .interfaceType(InterfaceType.COLLECTION)
                .exceptionReason("Collection validation failed")
                .operation("CREATE_COLLECTION")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.MEDIUM)
                .category(ExceptionCategory.VALIDATION)
                .retryable(true)
                .customerId("CUST-LIFECYCLE-001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryCount(0)
                .build();

        exceptionRepository.save(exception);

        // When & Then - Test state transitions

        // NEW -> ACKNOWLEDGED
        String ackUrl = "http://localhost:" + port + "/api/v1/exceptions/" + transactionId + "/acknowledge";
        Map<String, String> ackRequest = Map.of("acknowledgedBy", "lifecycle-test-user");
        HttpEntity<Map<String, String>> ackEntity = new HttpEntity<>(ackRequest, createHeaders());

        ResponseEntity<String> ackResponse = restTemplate.exchange(ackUrl, HttpMethod.PUT, ackEntity, String.class);
        assertThat(ackResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify ACKNOWLEDGED status
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Optional<InterfaceException> updated = exceptionRepository.findByTransactionId(transactionId);
                    assertThat(updated).isPresent();
                    assertThat(updated.get().getStatus()).isEqualTo(ExceptionStatus.ACKNOWLEDGED);
                });

        // ACKNOWLEDGED -> RESOLVED (via successful retry simulation)
        // This would typically happen through the retry mechanism
        InterfaceException acknowledgedEx = exceptionRepository.findByTransactionId(transactionId).get();
        acknowledgedEx.setStatus(ExceptionStatus.RESOLVED);
        acknowledgedEx.setResolvedAt(OffsetDateTime.now());
        acknowledgedEx.setResolvedBy("system");
        exceptionRepository.save(acknowledgedEx);

        // Verify RESOLVED status
        Optional<InterfaceException> resolvedEx = exceptionRepository.findByTransactionId(transactionId);
        assertThat(resolvedEx).isPresent();
        assertThat(resolvedEx.get().getStatus()).isEqualTo(ExceptionStatus.RESOLVED);
        assertThat(resolvedEx.get().getResolvedAt()).isNotNull();
    }

    private void createTestExceptions() throws Exception {
        // Order exception
        OrderRejectedEvent orderEvent = createOrderRejectedEvent(
                "test-search-order-001", "Order validation failed", "CREATE_ORDER");
        kafkaProducer.send(new ProducerRecord<>("OrderRejected", "test-search-order-001", orderEvent));

        // Collection exception
        OrderRejectedEvent collectionEvent = createOrderRejectedEvent(
                "test-search-collection-001", "Collection processing error", "CREATE_ORDER");
        kafkaProducer.send(new ProducerRecord<>("OrderRejected", "test-search-collection-001", collectionEvent));

        // Distribution exception
        OrderRejectedEvent distributionEvent = createOrderRejectedEvent(
                "test-search-distribution-001", "Distribution network timeout", "CREATE_ORDER");
        kafkaProducer.send(new ProducerRecord<>("OrderRejected", "test-search-distribution-001", distributionEvent));

        kafkaProducer.flush();
    }

    private OrderRejectedEvent createOrderRejectedEvent(String transactionId, String rejectedReason, String operation) {
        return OrderRejectedEvent.builder()
                .eventId("event-" + transactionId)
                .eventType("OrderRejected")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("order-service")
                .correlationId("corr-" + transactionId)
                .payload(OrderRejectedEvent.OrderRejectedPayload.builder()
                        .transactionId(transactionId)
                        .externalId("EXT-" + transactionId)
                        .operation(operation)
                        .rejectedReason(rejectedReason)
                        .customerId("CUST-INTEGRATION-001")
                        .locationCode("LOC-INTEGRATION-001")
                        .build())
                .build();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}