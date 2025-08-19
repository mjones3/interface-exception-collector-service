package com.arcone.biopro.exception.collector.api.graphql;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.*;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance integration tests for GraphQL API.
 * Validates response time requirements and load handling capabilities.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GraphQLPerformanceIntegrationTest {

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

    @LocalServerPort
    private int port;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    private WebGraphQlTester graphQlTester;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "test_pass");
    }

    @BeforeEach
    void setUp() {
        WebTestClient webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(30))
                .build();

        graphQlTester = WebGraphQlTester.create(webTestClient);
    }

    @AfterEach
    void tearDown() {
        exceptionRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Should meet list query performance requirements (< 500ms for 95th percentile)")
    void testListQueryPerformance() {
        // Given - Create test data
        createLargeDataset(1000);

        List<Long> responseTimes = new ArrayList<>();
        int iterations = 100;

        // When - Execute multiple list queries
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();

            graphQlTester.document("""
                    query {
                        exceptions(
                            filters: {
                                interfaceTypes: [ORDER, COLLECTION]
                                statuses: [NEW, ACKNOWLEDGED]
                            }
                            pagination: {
                                first: 20
                            }
                            sorting: {
                                field: "timestamp"
                                direction: DESC
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
                    .hasSizeGreaterThan(0);

            long endTime = System.currentTimeMillis();
            responseTimes.add(endTime - startTime);
        }

        // Then - Verify 95th percentile is under 500ms (Requirement 1.5)
        responseTimes.sort(Long::compareTo);
        long percentile95 = responseTimes.get((int) (iterations * 0.95));

        assertThat(percentile95).isLessThan(500L);

        // Also check average response time
        double averageTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        assertThat(averageTime).isLessThan(200L); // Should be much faster on average
    }

    @Test
    @Order(2)
    @DisplayName("Should meet detail query performance requirements (< 1s for 95th percentile)")
    void testDetailQueryPerformance() {
        // Given - Create test data with nested relationships
        List<InterfaceException> exceptions = createLargeDataset(100);
        List<String> transactionIds = exceptions.stream()
                .map(InterfaceException::getTransactionId)
                .toList();

        List<Long> responseTimes = new ArrayList<>();
        int iterations = 50;

        // When - Execute multiple detail queries
        for (int i = 0; i < iterations; i++) {
            String transactionId = transactionIds.get(i % transactionIds.size());
            long startTime = System.currentTimeMillis();

            graphQlTester.document("""
                    query($transactionId: String!) {
                        exception(transactionId: $transactionId) {
                            id
                            transactionId
                            interfaceType
                            exceptionReason
                            status
                            severity
                            category
                            customerId
                            locationCode
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
                            statusHistory {
                                status
                                changedAt
                                changedBy
                                reason
                            }
                        }
                    }
                    """)
                    .variable("transactionId", transactionId)
                    .execute()
                    .path("exception.transactionId")
                    .entity(String.class)
                    .isEqualTo(transactionId);

            long endTime = System.currentTimeMillis();
            responseTimes.add(endTime - startTime);
        }

        // Then - Verify 95th percentile is under 1000ms (Requirement 1.5)
        responseTimes.sort(Long::compareTo);
        long percentile95 = responseTimes.get((int) (iterations * 0.95));

        assertThat(percentile95).isLessThan(1000L);
    }

    @Test
    @Order(3)
    @DisplayName("Should meet dashboard summary performance requirements (< 200ms for 95th percentile)")
    void testSummaryQueryPerformance() {
        // Given - Create diverse test data for aggregations
        createDiverseDataset(500);

        List<Long> responseTimes = new ArrayList<>();
        int iterations = 50;

        // When - Execute multiple summary queries
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();

            graphQlTester.document("""
                    query {
                        exceptionSummary(
                            timeRange: LAST_24_HOURS
                            filters: {
                                interfaceTypes: [ORDER, COLLECTION, DISTRIBUTION]
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
                    .satisfies(count -> assertThat(count).isGreaterThan(0));

            long endTime = System.currentTimeMillis();
            responseTimes.add(endTime - startTime);
        }

        // Then - Verify 95th percentile is under 200ms (Requirement 4.4)
        responseTimes.sort(Long::compareTo);
        long percentile95 = responseTimes.get((int) (iterations * 0.95));

        assertThat(percentile95).isLessThan(200L);
    }

    @Test
    @Order(4)
    @DisplayName("Should handle concurrent requests efficiently")
    void testConcurrentRequestPerformance() throws Exception {
        // Given - Create test data
        createLargeDataset(500);

        int concurrentUsers = 50;
        int requestsPerUser = 10;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);

        List<CompletableFuture<Long>> futures = new ArrayList<>();

        // When - Execute concurrent requests
        for (int user = 0; user < concurrentUsers; user++) {
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                List<Long> userResponseTimes = new ArrayList<>();

                for (int request = 0; request < requestsPerUser; request++) {
                    long startTime = System.currentTimeMillis();

                    try {
                        graphQlTester.document("""
                                query {
                                    exceptions(
                                        pagination: { first: 10 }
                                        sorting: { field: "timestamp", direction: DESC }
                                    ) {
                                        edges {
                                            node {
                                                transactionId
                                                interfaceType
                                                status
                                            }
                                        }
                                        totalCount
                                    }
                                }
                                """)
                                .execute()
                                .path("exceptions.totalCount")
                                .entity(Long.class)
                                .satisfies(count -> assertThat(count).isGreaterThan(0L));
                    } catch (Exception e) {
                        // Log error but continue
                        System.err.println("Request failed: " + e.getMessage());
                    }

                    long endTime = System.currentTimeMillis();
                    userResponseTimes.add(endTime - startTime);
                }

                return userResponseTimes.stream().mapToLong(Long::longValue).max().orElse(0L);
            }, executor);

            futures.add(future);
        }

        // Then - Verify all requests complete within reasonable time
        List<Long> maxResponseTimes = new ArrayList<>();
        for (CompletableFuture<Long> future : futures) {
            Long maxTime = future.get(30, TimeUnit.SECONDS);
            maxResponseTimes.add(maxTime);
        }

        executor.shutdown();

        // Verify no request took longer than 2 seconds under load
        long overallMaxTime = maxResponseTimes.stream().mapToLong(Long::longValue).max().orElse(0L);
        assertThat(overallMaxTime).isLessThan(2000L);

        // Verify average response time is still reasonable
        double averageMaxTime = maxResponseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        assertThat(averageMaxTime).isLessThan(1000L);
    }

    @Test
    @Order(5)
    @DisplayName("Should meet mutation performance requirements (< 3s for 95th percentile)")
    void testMutationPerformance() {
        // Given - Create retryable exceptions
        List<InterfaceException> exceptions = createRetryableExceptions(50);
        List<String> transactionIds = exceptions.stream()
                .map(InterfaceException::getTransactionId)
                .toList();

        List<Long> responseTimes = new ArrayList<>();
        int iterations = 20;

        // When - Execute retry mutations
        for (int i = 0; i < iterations; i++) {
            String transactionId = transactionIds.get(i % transactionIds.size());
            long startTime = System.currentTimeMillis();

            graphQlTester.document("""
                    mutation($transactionId: String!) {
                        retryException(
                            transactionId: $transactionId
                            reason: "Performance test retry"
                            priority: NORMAL
                        ) {
                            success
                            message
                            exception {
                                transactionId
                                status
                                retryCount
                            }
                        }
                    }
                    """)
                    .variable("transactionId", transactionId)
                    .execute()
                    .path("retryException.success")
                    .entity(Boolean.class)
                    .isEqualTo(true);

            long endTime = System.currentTimeMillis();
            responseTimes.add(endTime - startTime);
        }

        // Then - Verify 95th percentile is under 3000ms (Requirement 3.5)
        responseTimes.sort(Long::compareTo);
        long percentile95 = responseTimes.get((int) (iterations * 0.95));

        assertThat(percentile95).isLessThan(3000L);
    }

    @Test
    @Order(6)
    @DisplayName("Should handle large result sets efficiently")
    void testLargeResultSetPerformance() {
        // Given - Create large dataset
        createLargeDataset(5000);

        // When - Query large result set with pagination
        long startTime = System.currentTimeMillis();

        graphQlTester.document("""
                query {
                    exceptions(
                        pagination: { first: 100 }
                        sorting: { field: "timestamp", direction: DESC }
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
                                timestamp
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
                .hasSize(100)
                .path("exceptions.totalCount")
                .entity(Long.class)
                .isEqualTo(5000L);

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Then - Should handle large dataset efficiently
        assertThat(responseTime).isLessThan(1000L); // Should be under 1 second even for large datasets
    }

    private List<InterfaceException> createLargeDataset(int count) {
        List<InterfaceException> exceptions = new ArrayList<>();

        InterfaceType[] types = InterfaceType.values();
        ExceptionStatus[] statuses = ExceptionStatus.values();
        ExceptionSeverity[] severities = ExceptionSeverity.values();

        for (int i = 0; i < count; i++) {
            InterfaceException exception = InterfaceException.builder()
                    .transactionId("TXN-PERF-" + String.format("%06d", i))
                    .externalId("EXT-PERF-" + i)
                    .interfaceType(types[i % types.length])
                    .exceptionReason("Performance test exception " + i)
                    .operation("OPERATION_" + (i % 5))
                    .status(statuses[i % statuses.length])
                    .severity(severities[i % severities.length])
                    .category(ExceptionCategory.VALIDATION)
                    .customerId("CUST-" + String.format("%03d", i % 100))
                    .locationCode("LOC-" + String.format("%03d", i % 50))
                    .timestamp(OffsetDateTime.now().minusHours(i % 24))
                    .processedAt(OffsetDateTime.now().minusHours(i % 24))
                    .retryable(i % 3 == 0)
                    .retryCount(i % 5)
                    .maxRetries(3)
                    .build();

            exceptions.add(exception);
        }

        return exceptionRepository.saveAll(exceptions);
    }

    private void createDiverseDataset(int count) {
        List<InterfaceException> exceptions = IntStream.range(0, count)
                .mapToObj(i -> {
                    InterfaceType type = InterfaceType.values()[i % InterfaceType.values().length];
                    ExceptionStatus status = ExceptionStatus.values()[i % ExceptionStatus.values().length];
                    ExceptionSeverity severity = ExceptionSeverity.values()[i % ExceptionSeverity.values().length];

                    return InterfaceException.builder()
                            .transactionId("TXN-DIV-" + String.format("%06d", i))
                            .externalId("EXT-DIV-" + i)
                            .interfaceType(type)
                            .exceptionReason("Diverse test exception " + i)
                            .operation("OPERATION_" + type.name())
                            .status(status)
                            .severity(severity)
                            .category(ExceptionCategory.values()[i % ExceptionCategory.values().length])
                            .customerId("CUST-" + String.format("%03d", i % 20))
                            .locationCode("LOC-" + String.format("%03d", i % 10))
                            .timestamp(OffsetDateTime.now().minusHours(i % 48))
                            .processedAt(OffsetDateTime.now().minusHours(i % 48))
                            .retryable(i % 2 == 0)
                            .retryCount(i % 4)
                            .maxRetries(3)
                            .build();
                })
                .toList();

        exceptionRepository.saveAll(exceptions);
    }

    private List<InterfaceException> createRetryableExceptions(int count) {
        List<InterfaceException> exceptions = IntStream.range(0, count)
                .mapToObj(i -> InterfaceException.builder()
                        .transactionId("TXN-RETRY-" + String.format("%06d", i))
                        .externalId("EXT-RETRY-" + i)
                        .interfaceType(InterfaceType.ORDER)
                        .exceptionReason("Retryable test exception " + i)
                        .operation("CREATE_ORDER")
                        .status(ExceptionStatus.NEW)
                        .severity(ExceptionSeverity.HIGH)
                        .category(ExceptionCategory.VALIDATION)
                        .customerId("CUST-001")
                        .locationCode("LOC-001")
                        .timestamp(OffsetDateTime.now())
                        .processedAt(OffsetDateTime.now())
                        .retryable(true)
                        .retryCount(0)
                        .maxRetries(3)
                        .build())
                .toList();

        return exceptionRepository.saveAll(exceptions);
    }
}