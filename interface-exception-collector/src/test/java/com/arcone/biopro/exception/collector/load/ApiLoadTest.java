package com.arcone.biopro.exception.collector.load;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Load tests for API endpoints.
 * Tests system behavior under high concurrent load for various API operations.
 * 
 * Run with: -Dload.tests.enabled=true
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestPropertySource(properties = {
        "app.security.rate-limit.enabled=false",
        "logging.level.com.arcone.biopro=WARN",
        "spring.jpa.show-sql=false"
})
@EnabledIfSystemProperty(named = "load.tests.enabled", matches = "true")
class ApiLoadTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("load_test_db")
            .withUsername("load_user")
            .withPassword("load_pass")
            .withCommand("postgres", "-c", "max_connections=300", "-c", "shared_buffers=512MB");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withCommand("redis-server", "--maxmemory", "512mb", "--maxmemory-policy", "allkeys-lru");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "50");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        exceptionRepository.deleteAll();
    }

    @Test
    @DisplayName("List Exceptions API Load Test - 1000 Concurrent Requests")
    void shouldHandleHighConcurrentListRequests() throws Exception {
        // Given - Create test data
        int testDataSize = 1000;
        createTestData(testDataSize);

        int concurrentRequests = 1000;
        int threadPoolSize = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        // When - Execute concurrent requests
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<LoadTestResult>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentRequests; i++) {
            final int requestId = i;
            CompletableFuture<LoadTestResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    long requestStart = System.currentTimeMillis();
                    String url = "http://localhost:" + port + "/api/v1/exceptions?page=" + (requestId % 10)
                            + "&size=20";
                    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                    long requestEnd = System.currentTimeMillis();

                    return LoadTestResult.builder()
                            .requestId(requestId)
                            .statusCode(response.getStatusCode().value())
                            .responseTime(requestEnd - requestStart)
                            .success(response.getStatusCode() == HttpStatus.OK)
                            .build();
                } catch (Exception e) {
                    return LoadTestResult.builder()
                            .requestId(requestId)
                            .statusCode(500)
                            .responseTime(-1)
                            .success(false)
                            .error(e.getMessage())
                            .build();
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all requests to complete
        List<LoadTestResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long totalTime = System.currentTimeMillis() - startTime;

        // Then - Analyze results
        analyzeLoadTestResults("List Exceptions API", results, totalTime, concurrentRequests);

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Exception Details API Load Test - 500 Concurrent Requests")
    void shouldHandleHighConcurrentDetailRequests() throws Exception {
        // Given - Create test data
        int testDataSize = 100;
        List<InterfaceException> testExceptions = createTestData(testDataSize);

        int concurrentRequests = 500;
        int threadPoolSize = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        // When - Execute concurrent requests
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<LoadTestResult>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentRequests; i++) {
            final int requestId = i;
            CompletableFuture<LoadTestResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    long requestStart = System.currentTimeMillis();
                    InterfaceException randomException = testExceptions.get(requestId % testExceptions.size());
                    String url = "http://localhost:" + port + "/api/v1/exceptions/"
                            + randomException.getTransactionId();
                    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                    long requestEnd = System.currentTimeMillis();

                    return LoadTestResult.builder()
                            .requestId(requestId)
                            .statusCode(response.getStatusCode().value())
                            .responseTime(requestEnd - requestStart)
                            .success(response.getStatusCode() == HttpStatus.OK)
                            .build();
                } catch (Exception e) {
                    return LoadTestResult.builder()
                            .requestId(requestId)
                            .statusCode(500)
                            .responseTime(-1)
                            .success(false)
                            .error(e.getMessage())
                            .build();
                }
            }, executor);
            futures.add(future);
        }

        List<LoadTestResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long totalTime = System.currentTimeMillis() - startTime;

        // Then - Analyze results
        analyzeLoadTestResults("Exception Details API", results, totalTime, concurrentRequests);

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Search API Load Test - 300 Concurrent Requests")
    void shouldHandleHighConcurrentSearchRequests() throws Exception {
        // Given - Create test data with searchable content
        int testDataSize = 500;
        createSearchableTestData(testDataSize);

        int concurrentRequests = 300;
        int threadPoolSize = 30;
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        String[] searchTerms = { "validation", "error", "timeout", "failed", "system" };

        // When - Execute concurrent search requests
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<LoadTestResult>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentRequests; i++) {
            final int requestId = i;
            CompletableFuture<LoadTestResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    long requestStart = System.currentTimeMillis();
                    String searchTerm = searchTerms[requestId % searchTerms.length];
                    String url = "http://localhost:" + port + "/api/v1/exceptions/search?query=" + searchTerm
                            + "&fields=exceptionReason";
                    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                    long requestEnd = System.currentTimeMillis();

                    return LoadTestResult.builder()
                            .requestId(requestId)
                            .statusCode(response.getStatusCode().value())
                            .responseTime(requestEnd - requestStart)
                            .success(response.getStatusCode() == HttpStatus.OK)
                            .build();
                } catch (Exception e) {
                    return LoadTestResult.builder()
                            .requestId(requestId)
                            .statusCode(500)
                            .responseTime(-1)
                            .success(false)
                            .error(e.getMessage())
                            .build();
                }
            }, executor);
            futures.add(future);
        }

        List<LoadTestResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long totalTime = System.currentTimeMillis() - startTime;

        // Then - Analyze results
        analyzeLoadTestResults("Search API", results, totalTime, concurrentRequests);

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Mixed API Operations Load Test - 800 Concurrent Requests")
    void shouldHandleMixedApiOperationsLoad() throws Exception {
        // Given - Create test data
        int testDataSize = 200;
        List<InterfaceException> testExceptions = createTestData(testDataSize);

        int concurrentRequests = 800;
        int threadPoolSize = 80;
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        // When - Execute mixed concurrent requests
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<LoadTestResult>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentRequests; i++) {
            final int requestId = i;
            CompletableFuture<LoadTestResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    long requestStart = System.currentTimeMillis();
                    ResponseEntity<String> response;
                    String operation;

                    // Distribute requests across different operations
                    int operationType = requestId % 4;
                    switch (operationType) {
                        case 0: // List exceptions
                            operation = "LIST";
                            String listUrl = "http://localhost:" + port + "/api/v1/exceptions?page=" + (requestId % 5)
                                    + "&size=10";
                            response = restTemplate.getForEntity(listUrl, String.class);
                            break;
                        case 1: // Get exception details
                            operation = "DETAIL";
                            InterfaceException randomException = testExceptions.get(requestId % testExceptions.size());
                            String detailUrl = "http://localhost:" + port + "/api/v1/exceptions/"
                                    + randomException.getTransactionId();
                            response = restTemplate.getForEntity(detailUrl, String.class);
                            break;
                        case 2: // Search exceptions
                            operation = "SEARCH";
                            String searchUrl = "http://localhost:" + port
                                    + "/api/v1/exceptions/search?query=test&fields=exceptionReason";
                            response = restTemplate.getForEntity(searchUrl, String.class);
                            break;
                        case 3: // Get summary
                            operation = "SUMMARY";
                            String summaryUrl = "http://localhost:" + port
                                    + "/api/v1/exceptions/summary?timeRange=today";
                            response = restTemplate.getForEntity(summaryUrl, String.class);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected operation type: " + operationType);
                    }

                    long requestEnd = System.currentTimeMillis();

                    return LoadTestResult.builder()
                            .requestId(requestId)
                            .statusCode(response.getStatusCode().value())
                            .responseTime(requestEnd - requestStart)
                            .success(response.getStatusCode() == HttpStatus.OK)
                            .operation(operation)
                            .build();
                } catch (Exception e) {
                    return LoadTestResult.builder()
                            .requestId(requestId)
                            .statusCode(500)
                            .responseTime(-1)
                            .success(false)
                            .error(e.getMessage())
                            .build();
                }
            }, executor);
            futures.add(future);
        }

        List<LoadTestResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long totalTime = System.currentTimeMillis() - startTime;

        // Then - Analyze results by operation type
        analyzeMixedOperationResults(results, totalTime, concurrentRequests);

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Acknowledgment API Load Test - 200 Concurrent Requests")
    void shouldHandleConcurrentAcknowledgmentRequests() throws Exception {
        // Given - Create test data
        int testDataSize = 200;
        List<InterfaceException> testExceptions = createTestData(testDataSize);

        int concurrentRequests = 200;
        int threadPoolSize = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        // When - Execute concurrent acknowledgment requests
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<LoadTestResult>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentRequests; i++) {
            final int requestId = i;
            CompletableFuture<LoadTestResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    long requestStart = System.currentTimeMillis();
                    InterfaceException exception = testExceptions.get(requestId % testExceptions.size());

                    String url = "http://localhost:" + port + "/api/v1/exceptions/" + exception.getTransactionId()
                            + "/acknowledge";
                    Map<String, String> ackRequest = Map.of(
                            "acknowledgedBy", "load-test-user-" + requestId,
                            "notes", "Load test acknowledgment " + requestId);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Map<String, String>> entity = new HttpEntity<>(ackRequest, headers);

                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
                    long requestEnd = System.currentTimeMillis();

                    return LoadTestResult.builder()
                            .requestId(requestId)
                            .statusCode(response.getStatusCode().value())
                            .responseTime(requestEnd - requestStart)
                            .success(response.getStatusCode() == HttpStatus.OK)
                            .build();
                } catch (Exception e) {
                    return LoadTestResult.builder()
                            .requestId(requestId)
                            .statusCode(500)
                            .responseTime(-1)
                            .success(false)
                            .error(e.getMessage())
                            .build();
                }
            }, executor);
            futures.add(future);
        }

        List<LoadTestResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long totalTime = System.currentTimeMillis() - startTime;

        // Then - Analyze results
        analyzeLoadTestResults("Acknowledgment API", results, totalTime, concurrentRequests);

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
    }

    // Helper methods

    private List<InterfaceException> createTestData(int count) {
        List<InterfaceException> exceptions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            InterfaceException exception = InterfaceException.builder()
                    .transactionId("load-test-" + i)
                    .interfaceType(InterfaceType.values()[i % InterfaceType.values().length])
                    .exceptionReason("Load test exception " + i)
                    .operation("CREATE_ORDER")
                    .externalId("EXT-LOAD-" + i)
                    .status(ExceptionStatus.NEW)
                    .severity(ExceptionSeverity.values()[i % ExceptionSeverity.values().length])
                    .category(ExceptionCategory.values()[i % ExceptionCategory.values().length])
                    .retryable(i % 2 == 0)
                    .customerId("CUST-LOAD-" + (i % 100))
                    .locationCode("LOC-LOAD-" + (i % 10))
                    .timestamp(OffsetDateTime.now().minusHours(i % 24))
                    .processedAt(OffsetDateTime.now().minusHours(i % 24))
                    .retryCount(i % 3)
                    .build();
            exceptions.add(exception);
        }

        return exceptionRepository.saveAll(exceptions);
    }

    private void createSearchableTestData(int count) {
        String[] searchableReasons = {
                "Order validation failed due to invalid customer data",
                "System error occurred during processing",
                "Network timeout while connecting to external service",
                "Authentication failed for user request",
                "Database connection error encountered"
        };

        List<InterfaceException> exceptions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            InterfaceException exception = InterfaceException.builder()
                    .transactionId("search-test-" + i)
                    .interfaceType(InterfaceType.values()[i % InterfaceType.values().length])
                    .exceptionReason(searchableReasons[i % searchableReasons.length] + " " + i)
                    .operation("CREATE_ORDER")
                    .externalId("EXT-SEARCH-" + i)
                    .status(ExceptionStatus.NEW)
                    .severity(ExceptionSeverity.values()[i % ExceptionSeverity.values().length])
                    .category(ExceptionCategory.values()[i % ExceptionCategory.values().length])
                    .retryable(true)
                    .customerId("CUST-SEARCH-" + (i % 50))
                    .locationCode("LOC-SEARCH-" + (i % 5))
                    .timestamp(OffsetDateTime.now().minusHours(i % 48))
                    .processedAt(OffsetDateTime.now().minusHours(i % 48))
                    .retryCount(0)
                    .build();
            exceptions.add(exception);
        }

        exceptionRepository.saveAll(exceptions);
    }

    private void analyzeLoadTestResults(String testName, List<LoadTestResult> results, long totalTime,
            int concurrentRequests) {
        long successCount = results.stream().mapToLong(r -> r.success ? 1 : 0).sum();
        double successRate = (double) successCount / concurrentRequests * 100;

        List<Long> responseTimes = results.stream()
                .filter(r -> r.responseTime > 0)
                .map(r -> r.responseTime)
                .sorted()
                .toList();

        double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long p50 = responseTimes.isEmpty() ? 0 : responseTimes.get((int) (responseTimes.size() * 0.5));
        long p95 = responseTimes.isEmpty() ? 0 : responseTimes.get((int) (responseTimes.size() * 0.95));
        long p99 = responseTimes.isEmpty() ? 0 : responseTimes.get((int) (responseTimes.size() * 0.99));

        double throughput = (double) concurrentRequests / (totalTime / 1000.0);

        System.out.println("\n" + "=".repeat(60));
        System.out.println(testName + " Load Test Results");
        System.out.println("=".repeat(60));
        System.out.println("Total Requests: " + concurrentRequests);
        System.out.println("Successful Requests: " + successCount);
        System.out.println("Success Rate: " + String.format("%.2f", successRate) + "%");
        System.out.println("Total Time: " + totalTime + "ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/second");
        System.out.println("Average Response Time: " + String.format("%.2f", avgResponseTime) + "ms");
        System.out.println("50th Percentile: " + p50 + "ms");
        System.out.println("95th Percentile: " + p95 + "ms");
        System.out.println("99th Percentile: " + p99 + "ms");

        // Performance assertions
        assertThat(successRate).isGreaterThan(95.0); // At least 95% success rate
        assertThat(avgResponseTime).isLessThan(2000); // Average response time under 2 seconds
        assertThat(p95).isLessThan(5000); // 95th percentile under 5 seconds
        assertThat(throughput).isGreaterThan(10); // At least 10 requests per second
    }

    private void analyzeMixedOperationResults(List<LoadTestResult> results, long totalTime, int concurrentRequests) {
        Map<String, List<LoadTestResult>> resultsByOperation = results.stream()
                .collect(java.util.stream.Collectors.groupingBy(r -> r.operation != null ? r.operation : "UNKNOWN"));

        System.out.println("\n" + "=".repeat(60));
        System.out.println("Mixed API Operations Load Test Results");
        System.out.println("=".repeat(60));
        System.out.println("Total Requests: " + concurrentRequests);
        System.out.println("Total Time: " + totalTime + "ms");
        System.out.println("Overall Throughput: "
                + String.format("%.2f", (double) concurrentRequests / (totalTime / 1000.0)) + " requests/second");

        for (Map.Entry<String, List<LoadTestResult>> entry : resultsByOperation.entrySet()) {
            String operation = entry.getKey();
            List<LoadTestResult> operationResults = entry.getValue();

            long successCount = operationResults.stream().mapToLong(r -> r.success ? 1 : 0).sum();
            double successRate = (double) successCount / operationResults.size() * 100;

            List<Long> responseTimes = operationResults.stream()
                    .filter(r -> r.responseTime > 0)
                    .map(r -> r.responseTime)
                    .sorted()
                    .toList();

            double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);

            System.out.println("\n" + operation + " Operation:");
            System.out.println("  Requests: " + operationResults.size());
            System.out.println("  Success Rate: " + String.format("%.2f", successRate) + "%");
            System.out.println("  Avg Response Time: " + String.format("%.2f", avgResponseTime) + "ms");
        }

        // Overall performance assertions
        long totalSuccessCount = results.stream().mapToLong(r -> r.success ? 1 : 0).sum();
        double overallSuccessRate = (double) totalSuccessCount / concurrentRequests * 100;
        assertThat(overallSuccessRate).isGreaterThan(90.0); // At least 90% overall success rate
    }

    // Inner class for load test results
    private static class LoadTestResult {
        private final int requestId;
        private final int statusCode;
        private final long responseTime;
        private final boolean success;
        private final String error;
        private final String operation;

        private LoadTestResult(Builder builder) {
            this.requestId = builder.requestId;
            this.statusCode = builder.statusCode;
            this.responseTime = builder.responseTime;
            this.success = builder.success;
            this.error = builder.error;
            this.operation = builder.operation;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private int requestId;
            private int statusCode;
            private long responseTime;
            private boolean success;
            private String error;
            private String operation;

            public Builder requestId(int requestId) {
                this.requestId = requestId;
                return this;
            }

            public Builder statusCode(int statusCode) {
                this.statusCode = statusCode;
                return this;
            }

            public Builder responseTime(long responseTime) {
                this.responseTime = responseTime;
                return this;
            }

            public Builder success(boolean success) {
                this.success = success;
                return this;
            }

            public Builder error(String error) {
                this.error = error;
                return this;
            }

            public Builder operation(String operation) {
                this.operation = operation;
                return this;
            }

            public LoadTestResult build() {
                return new LoadTestResult(this);
            }
        }
    }
}