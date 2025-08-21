package com.arcone.biopro.exception.collector.api.graphql;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.*;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive integration test suite for GraphQL API.
 * Tests end-to-end query execution with real database and external service
 * mocking.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GraphQLIntegrationTestSuite {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("exception_collector_test")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withInitScript("test-schema.sql");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", "test_pass");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    private static WireMockServer wireMockServer;

    @LocalServerPort
    private int port;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private RetryAttemptRepository retryAttemptRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private WebGraphQlTester graphQlTester;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Database configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Redis configuration
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "test_pass");

        // Kafka configuration
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        // External service configuration
        registry.add("app.source-services.order.base-url", () -> "http://localhost:" + wireMockServer.port());
        registry.add("app.source-services.collection.base-url", () -> "http://localhost:" + wireMockServer.port());
        registry.add("app.source-services.distribution.base-url", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeAll
    static void setupWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void tearDownWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        WebTestClient webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(30))
                .build();

        graphQlTester = WebGraphQlTester.create(webTestClient);

        // Reset WireMock
        wireMockServer.resetAll();
        setupDefaultWireMockStubs();
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        retryAttemptRepository.deleteAll();
        exceptionRepository.deleteAll();
    }

    private void setupDefaultWireMockStubs() {
        // Mock payload retrieval endpoints
        stubFor(get(urlPathMatching("/api/v1/orders/.*/payload"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"orderId\":\"ORDER-001\",\"customerId\":\"CUST-001\",\"items\":[{\"sku\":\"ITEM-001\",\"quantity\":2}]}")));

        stubFor(get(urlPathMatching("/api/v1/collections/.*/payload"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"collectionId\":\"COL-001\",\"orderId\":\"ORDER-001\",\"status\":\"PENDING\"}")));

        stubFor(get(urlPathMatching("/api/v1/distributions/.*/payload"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"distributionId\":\"DIST-001\",\"orderId\":\"ORDER-001\",\"status\":\"FAILED\"}")));

        // Mock retry endpoints
        stubFor(post(urlPathMatching("/api/v1/orders/.*/retry"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true,\"message\":\"Retry initiated successfully\"}")));

        stubFor(post(urlPathMatching("/api/v1/collections/.*/retry"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true,\"message\":\"Retry initiated successfully\"}")));

        stubFor(post(urlPathMatching("/api/v1/distributions/.*/retry"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true,\"message\":\"Retry initiated successfully\"}")));
    }

    @Test
    @Order(1)
    @DisplayName("Should query exceptions with basic filters and pagination")
    void testBasicExceptionQuery() {
        // Given
        InterfaceException exception = createTestException("TXN-001", InterfaceType.ORDER, ExceptionStatus.NEW);
        exceptionRepository.save(exception);

        // When & Then
        graphQlTester.document("""
                query {
                    exceptions(
                        filters: {
                            interfaceTypes: [ORDER]
                            statuses: [NEW]
                        }
                        pagination: {
                            first: 10
                        }
                    ) {
                        edges {
                            node {
                                id
                                transactionId
                                interfaceType
                                status
                                severity
                                exceptionReason
                                customerId
                                locationCode
                                retryable
                                retryCount
                            }
                            cursor
                        }
                        pageInfo {
                            hasNextPage
                            hasPreviousPage
                            startCursor
                            endCursor
                        }
                        totalCount
                    }
                }
                """)
                .execute()
                .path("exceptions.edges")
                .entityList(Object.class)
                .hasSize(1)
                .path("exceptions.edges[0].node.transactionId")
                .entity(String.class)
                .isEqualTo("TXN-001")
                .path("exceptions.edges[0].node.interfaceType")
                .entity(String.class)
                .isEqualTo("ORDER")
                .path("exceptions.totalCount")
                .entity(Long.class)
                .isEqualTo(1L);
    }

    @Test
    @Order(2)
    @DisplayName("Should query single exception by transaction ID with nested data")
    void testSingleExceptionQueryWithNestedData() {
        // Given
        InterfaceException exception = createTestException("TXN-002", InterfaceType.ORDER, ExceptionStatus.NEW);
        exception = exceptionRepository.save(exception);

        RetryAttempt retryAttempt = createTestRetryAttempt(exception, 1);
        retryAttemptRepository.save(retryAttempt);

        // When & Then
        graphQlTester.document("""
                query {
                    exception(transactionId: "TXN-002") {
                        id
                        transactionId
                        interfaceType
                        exceptionReason
                        status
                        severity
                        retryHistory {
                            attemptNumber
                            status
                            initiatedBy
                            resultSuccess
                            resultMessage
                        }
                        originalPayload {
                            content
                            contentType
                            sourceService
                        }
                    }
                }
                """)
                .execute()
                .path("exception.transactionId")
                .entity(String.class)
                .isEqualTo("TXN-002")
                .path("exception.retryHistory")
                .entityList(Object.class)
                .hasSize(1)
                .path("exception.retryHistory[0].attemptNumber")
                .entity(Integer.class)
                .isEqualTo(1)
                .path("exception.originalPayload.sourceService")
                .entity(String.class)
                .isEqualTo("ORDER");
    }

    @Test
    @Order(3)
    @DisplayName("Should execute retry mutation successfully")
    @Transactional
    void testRetryMutation() {
        // Given
        InterfaceException exception = createTestException("TXN-003", InterfaceType.ORDER, ExceptionStatus.NEW);
        exception.setRetryable(true);
        exceptionRepository.save(exception);

        // When & Then
        graphQlTester.document("""
                mutation {
                    retryException(
                        transactionId: "TXN-003"
                        reason: "Manual retry for testing"
                        priority: HIGH
                    ) {
                        success
                        message
                        exception {
                            transactionId
                            status
                            retryCount
                        }
                        retryAttempt {
                            attemptNumber
                            status
                            initiatedBy
                        }
                    }
                }
                """)
                .execute()
                .path("retryException.success")
                .entity(Boolean.class)
                .isEqualTo(true)
                .path("retryException.exception.transactionId")
                .entity(String.class)
                .isEqualTo("TXN-003")
                .path("retryException.retryAttempt.attemptNumber")
                .entity(Integer.class)
                .isEqualTo(1);

        // Verify WireMock was called
        verify(postRequestedFor(urlPathMatching("/api/v1/orders/.*/retry")));
    }

    @Test
    @Order(4)
    @DisplayName("Should execute acknowledge mutation successfully")
    @Transactional
    void testAcknowledgeMutation() {
        // Given
        InterfaceException exception = createTestException("TXN-004", InterfaceType.ORDER, ExceptionStatus.NEW);
        exceptionRepository.save(exception);

        // When & Then
        graphQlTester.document("""
                mutation {
                    acknowledgeException(
                        transactionId: "TXN-004"
                        reason: "Acknowledged for testing"
                        notes: "Test acknowledgment"
                    ) {
                        success
                        message
                        exception {
                            transactionId
                            status
                            acknowledgedBy
                        }
                    }
                }
                """)
                .execute()
                .path("acknowledgeException.success")
                .entity(Boolean.class)
                .isEqualTo(true)
                .path("acknowledgeException.exception.transactionId")
                .entity(String.class)
                .isEqualTo("TXN-004")
                .path("acknowledgeException.exception.status")
                .entity(String.class)
                .isEqualTo("ACKNOWLEDGED");
    }

    @Test
    @Order(5)
    @DisplayName("Should query exception summary statistics")
    void testExceptionSummaryQuery() {
        // Given
        createMultipleTestExceptions();

        // When & Then
        graphQlTester.document("""
                query {
                    exceptionSummary(
                        timeRange: LAST_24_HOURS
                        filters: {
                            interfaceTypes: [ORDER, COLLECTION]
                        }
                    ) {
                        totalExceptions
                        byInterfaceType {
                            interfaceType
                            count
                            percentage
                        }
                        bySeverity {
                            severity
                            count
                            percentage
                        }
                        byStatus {
                            status
                            count
                            percentage
                        }
                        keyMetrics {
                            retrySuccessRate
                            averageResolutionTime
                            criticalExceptions
                            newExceptionsToday
                        }
                        trends {
                            timestamp
                            count
                            interfaceType
                        }
                    }
                }
                """)
                .execute()
                .path("exceptionSummary.totalExceptions")
                .entity(Integer.class)
                .satisfies(count -> assertThat(count).isGreaterThan(0))
                .path("exceptionSummary.byInterfaceType")
                .entityList(Object.class)
                .hasSizeGreaterThan(0)
                .path("exceptionSummary.keyMetrics.retrySuccessRate")
                .entity(Double.class)
                .satisfies(rate -> assertThat(rate).isBetween(0.0, 100.0));
    }

    @Test
    @Order(6)
    @DisplayName("Should handle complex filtering and sorting")
    void testComplexFiltering() {
        // Given
        createMultipleTestExceptions();

        // When & Then
        graphQlTester.document("""
                query {
                    exceptions(
                        filters: {
                            interfaceTypes: [ORDER, COLLECTION]
                            statuses: [NEW, ACKNOWLEDGED]
                            severities: [HIGH, CRITICAL]
                            dateRange: {
                                start: "2024-01-01T00:00:00Z"
                                end: "2024-12-31T23:59:59Z"
                            }
                            customerIds: ["CUST-001", "CUST-002"]
                            searchTerm: "test"
                            excludeResolved: true
                            retryable: true
                        }
                        pagination: {
                            first: 5
                        }
                        sorting: {
                            field: "timestamp"
                            direction: DESC
                        }
                    ) {
                        edges {
                            node {
                                transactionId
                                interfaceType
                                status
                                severity
                                customerId
                                retryable
                            }
                        }
                        pageInfo {
                            hasNextPage
                            hasPreviousPage
                        }
                        totalCount
                    }
                }
                """)
                .execute()
                .path("exceptions.edges")
                .entityList(Object.class)
                .hasSizeGreaterThan(0)
                .path("exceptions.totalCount")
                .entity(Long.class)
                .satisfies(count -> assertThat(count).isGreaterThan(0L));
    }

    @Test
    @Order(7)
    @DisplayName("Should handle validation errors gracefully")
    void testValidationErrors() {
        // Test invalid pagination parameters
        graphQlTester.document("""
                query {
                    exceptions(
                        pagination: {
                            first: -1
                        }
                    ) {
                        edges {
                            node {
                                transactionId
                            }
                        }
                    }
                }
                """)
                .execute()
                .errors()
                .expect(error -> error.getMessage() != null &&
                        error.getMessage().contains("first") &&
                        error.getMessage().contains("positive"));

        // Test invalid transaction ID
        graphQlTester.document("""
                query {
                    exception(transactionId: "") {
                        transactionId
                    }
                }
                """)
                .execute()
                .errors()
                .expect(error -> error.getErrorType().toString().equals("BAD_REQUEST"));
    }

    @Test
    @Order(8)
    @DisplayName("Should handle external service failures gracefully")
    void testExternalServiceFailures() {
        // Given
        InterfaceException exception = createTestException("TXN-FAIL", InterfaceType.ORDER, ExceptionStatus.NEW);
        exception.setRetryable(true);
        exceptionRepository.save(exception);

        // Mock external service failure
        stubFor(post(urlPathMatching("/api/v1/orders/.*/retry"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("{\"error\":\"Internal server error\"}")));

        // When & Then
        graphQlTester.document("""
                mutation {
                    retryException(
                        transactionId: "TXN-FAIL"
                        reason: "Testing failure handling"
                        priority: NORMAL
                    ) {
                        success
                        message
                        exception {
                            transactionId
                        }
                    }
                }
                """)
                .execute()
                .path("retryException.success")
                .entity(Boolean.class)
                .isEqualTo(false)
                .path("retryException.message")
                .entity(String.class)
                .satisfies(message -> assertThat(message).contains("failed"));
    }

    private InterfaceException createTestException(String transactionId, InterfaceType interfaceType,
            ExceptionStatus status) {
        return InterfaceException.builder()
                .transactionId(transactionId)
                .externalId("EXT-" + transactionId)
                .interfaceType(interfaceType)
                .exceptionReason("Test exception for " + transactionId)
                .operation("CREATE_" + interfaceType.name())
                .status(status)
                .severity(ExceptionSeverity.HIGH)
                .category(ExceptionCategory.VALIDATION)
                .customerId("CUST-001")
                .locationCode("LOC-001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryable(true)
                .retryCount(0)
                .maxRetries(3)
                .build();
    }

    private RetryAttempt createTestRetryAttempt(InterfaceException exception, int attemptNumber) {
        return RetryAttempt.builder()
                .exception(exception)
                .attemptNumber(attemptNumber)
                .status(RetryStatus.COMPLETED)
                .initiatedBy("test-user")
                .initiatedAt(OffsetDateTime.now())
                .completedAt(OffsetDateTime.now())
                .resultSuccess(true)
                .resultMessage("Retry completed successfully")
                .resultResponseCode(200)
                .build();
    }

    private void createMultipleTestExceptions() {
        List<InterfaceException> exceptions = List.of(
                createTestException("TXN-MULTI-001", InterfaceType.ORDER, ExceptionStatus.NEW),
                createTestException("TXN-MULTI-002", InterfaceType.COLLECTION, ExceptionStatus.ACKNOWLEDGED),
                createTestException("TXN-MULTI-003", InterfaceType.DISTRIBUTION, ExceptionStatus.RESOLVED),
                createTestException("TXN-MULTI-004", InterfaceType.ORDER, ExceptionStatus.NEW));

        exceptions.get(1).setSeverity(ExceptionSeverity.CRITICAL);
        exceptions.get(2).setSeverity(ExceptionSeverity.MEDIUM);
        exceptions.get(3).setCustomerId("CUST-002");

        exceptionRepository.saveAll(exceptions);
    }
}