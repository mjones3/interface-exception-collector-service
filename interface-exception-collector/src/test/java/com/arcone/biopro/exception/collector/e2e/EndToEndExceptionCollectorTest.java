package com.arcone.biopro.exception.collector.e2e;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end tests for the complete Exception Collector Service.
 * Tests the entire system from Kafka event consumption through database
 * storage,
 * API access, external service integration, and event publishing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {
        "OrderRejected", "CollectionRejected", "DistributionFailed", "ValidationError",
        "ExceptionCaptured", "ExceptionResolved", "CriticalExceptionAlert", "ExceptionRetryCompleted"
})
@TestPropertySource(properties = {
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=e2e-test-group",
        "app.security.jwt.secret=e2eTestSecret1234567890123456789012345678901234567890",
        "app.security.rate-limit.enabled=false",
        "logging.level.com.arcone.biopro=INFO"
})
@DirtiesContext
class EndToEndExceptionCollectorTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("e2e_test_db")
            .withUsername("e2e_user")
            .withPassword("e2e_pass");

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
    private WireMockServer wireMockServer;

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
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("e2e-consumer", "false", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        kafkaConsumer = new DefaultKafkaConsumerFactory<String, Object>(consumerProps).createConsumer();

        // Setup WireMock for external service simulation
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);

        // Configure external service URLs to point to WireMock
        System.setProperty("app.source-services.order.base-url", "http://localhost:8089");
        System.setProperty("app.source-services.collection.base-url", "http://localhost:8089");
        System.setProperty("app.source-services.distribution.base-url", "http://localhost:8089");
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    @DisplayName("Complete Exception Processing Workflow - From Event to Resolution")
    void shouldProcessCompleteExceptionWorkflowFromEventToResolution() throws Exception {
        // Given - Setup external service mocks
        setupExternalServiceMocks();

        String transactionId = "e2e-complete-workflow-001";

        // Subscribe to all outbound events
        kafkaConsumer.subscribe(List.of("ExceptionCaptured", "ExceptionResolved", "ExceptionRetryCompleted"));

        // Step 1: Publish inbound exception event
        OrderRejectedEvent orderEvent = createOrderRejectedEvent(transactionId, "Temporary service unavailable",
                "CREATE_ORDER");
        kafkaProducer.send(new ProducerRecord<>("OrderRejected", transactionId, orderEvent));
        kafkaProducer.flush();

        // Step 2: Verify exception was captured and stored
        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
                    assertThat(exception).isPresent();
                    assertThat(exception.get().getInterfaceType()).isEqualTo(InterfaceType.ORDER);
                    assertThat(exception.get().getStatus()).isEqualTo(ExceptionStatus.NEW);
                    assertThat(exception.get().getRetryable()).isTrue();
                });

        // Step 3: Verify ExceptionCaptured event was published
        ConsumerRecords<String, Object> capturedRecords = kafkaConsumer.poll(Duration.ofSeconds(5));
        assertThat(capturedRecords).isNotEmpty();
        boolean foundCapturedEvent = false;
        for (ConsumerRecord<String, Object> record : capturedRecords) {
            if ("ExceptionCaptured".equals(record.topic())) {
                foundCapturedEvent = true;
                break;
            }
        }
        assertThat(foundCapturedEvent).isTrue();

        // Step 4: Acknowledge exception via API
        String ackUrl = "http://localhost:" + port + "/api/v1/exceptions/" + transactionId + "/acknowledge";
        Map<String, String> ackRequest = Map.of(
                "acknowledgedBy", "e2e-test-user",
                "notes", "Acknowledged during E2E test");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> ackEntity = new HttpEntity<>(ackRequest, headers);

        ResponseEntity<String> ackResponse = restTemplate.exchange(ackUrl, HttpMethod.PUT, ackEntity, String.class);
        assertThat(ackResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Step 5: Verify acknowledgment was processed
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
                    assertThat(exception).isPresent();
                    assertThat(exception.get().getStatus()).isEqualTo(ExceptionStatus.ACKNOWLEDGED);
                    assertThat(exception.get().getAcknowledgedBy()).isEqualTo("e2e-test-user");
                });

        // Step 6: Initiate retry via API
        String retryUrl = "http://localhost:" + port + "/api/v1/exceptions/" + transactionId + "/retry";
        Map<String, Object> retryRequest = Map.of(
                "reason", "E2E test retry",
                "priority", "HIGH",
                "notifyOnCompletion", true);

        HttpEntity<Map<String, Object>> retryEntity = new HttpEntity<>(retryRequest, headers);
        ResponseEntity<String> retryResponse = restTemplate.postForEntity(retryUrl, retryEntity, String.class);
        assertThat(retryResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // Step 7: Verify retry attempt was created and processed
        InterfaceException exception = exceptionRepository.findByTransactionId(transactionId).get();
        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    List<RetryAttempt> attempts = retryAttemptRepository
                            .findByExceptionIdOrderByAttemptNumber(exception.getId());
                    assertThat(attempts).hasSize(1);
                    assertThat(attempts.get(0).getStatus()).isIn(RetryStatus.SUCCESS, RetryStatus.FAILED);
                });

        // Step 8: Verify retry completion event was published
        ConsumerRecords<String, Object> retryRecords = kafkaConsumer.poll(Duration.ofSeconds(5));
        boolean foundRetryEvent = false;
        for (ConsumerRecord<String, Object> record : retryRecords) {
            if ("ExceptionRetryCompleted".equals(record.topic())) {
                foundRetryEvent = true;
                break;
            }
        }
        assertThat(foundRetryEvent).isTrue();

        // Step 9: Verify final exception state
        Optional<InterfaceException> finalException = exceptionRepository.findByTransactionId(transactionId);
        assertThat(finalException).isPresent();
        assertThat(finalException.get().getRetryCount()).isGreaterThan(0);
        assertThat(finalException.get().getLastRetryAt()).isNotNull();
    }

    @Test
    @DisplayName("Critical Exception Alert Workflow")
    void shouldProcessCriticalExceptionAlertWorkflow() throws Exception {
        // Given
        String transactionId = "e2e-critical-alert-001";
        kafkaConsumer.subscribe(Collections.singletonList("CriticalExceptionAlert"));

        // When - Publish critical exception event
        OrderRejectedEvent criticalEvent = createOrderRejectedEvent(transactionId, "System error occurred",
                "CREATE_ORDER");
        kafkaProducer.send(new ProducerRecord<>("OrderRejected", transactionId, criticalEvent));
        kafkaProducer.flush();

        // Then - Verify exception was processed as critical
        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
                    assertThat(exception).isPresent();
                    assertThat(exception.get().getSeverity().name()).isEqualTo("CRITICAL");
                });

        // Verify critical alert was published
        ConsumerRecords<String, Object> alertRecords = kafkaConsumer.poll(Duration.ofSeconds(5));
        assertThat(alertRecords).isNotEmpty();

        boolean foundCriticalAlert = false;
        for (ConsumerRecord<String, Object> record : alertRecords) {
            if ("CriticalExceptionAlert".equals(record.topic())) {
                foundCriticalAlert = true;
                // Verify alert content
                JsonNode alertPayload = objectMapper.readTree(record.value().toString());
                assertThat(alertPayload.get("payload").get("alertLevel").asText()).isEqualTo("CRITICAL");
                break;
            }
        }
        assertThat(foundCriticalAlert).isTrue();
    }

    @Test
    @DisplayName("API Comprehensive Functionality Test")
    void shouldProvideComprehensiveApiFunctionality() throws Exception {
        // Given - Create test data
        createMultipleTestExceptions();

        // Wait for all exceptions to be processed
        await().atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    long count = exceptionRepository.count();
                    assertThat(count).isEqualTo(5);
                });

        // Test 1: List exceptions with pagination
        String listUrl = "http://localhost:" + port + "/api/v1/exceptions?page=0&size=3";
        ResponseEntity<String> listResponse = restTemplate.getForEntity(listUrl, String.class);
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode listJson = objectMapper.readTree(listResponse.getBody());
        assertThat(listJson.get("content")).isArray();
        assertThat(listJson.get("content").size()).isEqualTo(3);
        assertThat(listJson.get("totalElements").asInt()).isEqualTo(5);

        // Test 2: Filter by interface type
        String filterUrl = "http://localhost:" + port + "/api/v1/exceptions?interfaceType=ORDER&page=0&size=10";
        ResponseEntity<String> filterResponse = restTemplate.getForEntity(filterUrl, String.class);
        assertThat(filterResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Test 3: Search functionality
        String searchUrl = "http://localhost:" + port
                + "/api/v1/exceptions/search?query=validation&fields=exceptionReason";
        ResponseEntity<String> searchResponse = restTemplate.getForEntity(searchUrl, String.class);
        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Test 4: Get exception details
        List<InterfaceException> exceptions = exceptionRepository.findAll();
        String detailUrl = "http://localhost:" + port + "/api/v1/exceptions/" + exceptions.get(0).getTransactionId();
        ResponseEntity<String> detailResponse = restTemplate.getForEntity(detailUrl, String.class);
        assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode detailJson = objectMapper.readTree(detailResponse.getBody());
        assertThat(detailJson.get("transactionId").asText()).isEqualTo(exceptions.get(0).getTransactionId());

        // Test 5: Summary statistics
        String summaryUrl = "http://localhost:" + port + "/api/v1/exceptions/summary?timeRange=today";
        ResponseEntity<String> summaryResponse = restTemplate.getForEntity(summaryUrl, String.class);
        assertThat(summaryResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode summaryJson = objectMapper.readTree(summaryResponse.getBody());
        assertThat(summaryJson.get("totalExceptions").asInt()).isEqualTo(5);
        assertThat(summaryJson.has("byInterfaceType")).isTrue();
        assertThat(summaryJson.has("bySeverity")).isTrue();
        assertThat(summaryJson.has("byStatus")).isTrue();
    }

    @Test
    @DisplayName("External Service Integration with Fallback")
    void shouldHandleExternalServiceIntegrationWithFallback() throws Exception {
        // Given - Setup external service to fail initially, then succeed
        String transactionId = "e2e-external-service-001";

        // First, make external service unavailable
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/orders/" + transactionId + "/payload"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Service unavailable\"}")));

        // Create exception
        InterfaceException exception = InterfaceException.builder()
                .transactionId(transactionId)
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("External service test")
                .operation("CREATE_ORDER")
                .externalId("ORDER-EXT-001")
                .status(ExceptionStatus.ACKNOWLEDGED)
                .severity(com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity.HIGH)
                .category(com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory.NETWORK_ERROR)
                .retryable(true)
                .customerId("CUST-EXT-001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryCount(0)
                .build();

        exceptionRepository.save(exception);

        // When - Try to get exception details with payload (should fail gracefully)
        String detailUrl = "http://localhost:" + port + "/api/v1/exceptions/" + transactionId + "?includePayload=true";
        ResponseEntity<String> detailResponse = restTemplate.getForEntity(detailUrl, String.class);

        // Then - Should return exception details without payload
        assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode detailJson = objectMapper.readTree(detailResponse.getBody());
        assertThat(detailJson.get("transactionId").asText()).isEqualTo(transactionId);
        assertThat(detailJson.get("originalPayload")).isNull();

        // Now make external service available
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/orders/" + transactionId + "/payload"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"orderId\": \"ORDER-EXT-001\", \"customerId\": \"CUST-EXT-001\", \"items\": []}")));

        // Retry the request - should now include payload
        ResponseEntity<String> retryDetailResponse = restTemplate.getForEntity(detailUrl, String.class);
        assertThat(retryDetailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode retryDetailJson = objectMapper.readTree(retryDetailResponse.getBody());
        assertThat(retryDetailJson.get("originalPayload")).isNotNull();
        assertThat(retryDetailJson.get("originalPayload").get("orderId").asText()).isEqualTo("ORDER-EXT-001");
    }

    @Test
    @DisplayName("System Health and Monitoring")
    void shouldProvideSystemHealthAndMonitoring() throws Exception {
        // Test health endpoint
        String healthUrl = "http://localhost:" + port + "/actuator/health";
        ResponseEntity<String> healthResponse = restTemplate.getForEntity(healthUrl, String.class);
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode healthJson = objectMapper.readTree(healthResponse.getBody());
        assertThat(healthJson.get("status").asText()).isEqualTo("UP");

        // Test metrics endpoint
        String metricsUrl = "http://localhost:" + port + "/actuator/metrics";
        ResponseEntity<String> metricsResponse = restTemplate.getForEntity(metricsUrl, String.class);
        assertThat(metricsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Test info endpoint
        String infoUrl = "http://localhost:" + port + "/actuator/info";
        ResponseEntity<String> infoResponse = restTemplate.getForEntity(infoUrl, String.class);
        assertThat(infoResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // Helper methods

    private void setupExternalServiceMocks() {
        // Mock successful payload retrieval
        wireMockServer.stubFor(get(WireMock.urlMatching("/api/v1/orders/.*/payload"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"orderId\": \"ORDER-123\", \"customerId\": \"CUST-123\", \"items\": []}")));

        // Mock successful retry operation
        wireMockServer.stubFor(post(WireMock.urlMatching("/api/v1/orders"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\": true, \"message\": \"Order processed successfully\"}")));
    }

    private void createMultipleTestExceptions() throws Exception {
        String[] transactionIds = {
                "e2e-multi-001", "e2e-multi-002", "e2e-multi-003", "e2e-multi-004", "e2e-multi-005"
        };

        String[] reasons = {
                "Order validation failed",
                "Collection processing error",
                "Distribution network timeout",
                "System error occurred",
                "Customer data invalid"
        };

        for (int i = 0; i < transactionIds.length; i++) {
            OrderRejectedEvent event = createOrderRejectedEvent(transactionIds[i], reasons[i], "CREATE_ORDER");
            kafkaProducer.send(new ProducerRecord<>("OrderRejected", transactionIds[i], event));
        }
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
                        .customerId("CUST-E2E-001")
                        .locationCode("LOC-E2E-001")
                        .build())
                .build();
    }
}