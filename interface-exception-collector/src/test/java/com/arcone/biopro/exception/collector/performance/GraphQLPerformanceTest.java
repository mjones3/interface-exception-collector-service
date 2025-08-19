package com.arcone.biopro.exception.collector.performance;

import com.arcone.biopro.exception.collector.api.graphql.service.PerformanceMonitoringService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Performance tests for GraphQL API to validate optimization requirements.
 * Tests response times, throughput, and resource utilization under load.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Slf4j
class GraphQLPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PerformanceMonitoringService performanceMonitoringService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String GRAPHQL_ENDPOINT = "/graphql";

    // Performance requirement thresholds
    private static final long LIST_QUERY_MAX_TIME_MS = 500;
    private static final long DETAIL_QUERY_MAX_TIME_MS = 1000;
    private static final long MUTATION_MAX_TIME_MS = 3000;
    private static final double MIN_CACHE_HIT_RATE = 80.0;
    private static final int CONCURRENT_USERS = 50;
    private static final int QUERIES_PER_USER = 20;

    @BeforeEach
    void setUp() {
        log.info("Setting up GraphQL performance test environment");
    }

    /**
     * Tests exception list query performance under load.
     * Validates requirement 1.5: List queries respond within 500ms (95th
     * percentile).
     */
    @Test
    void testExceptionListQueryPerformance() throws Exception {
        log.info("Testing exception list query performance");

        String query = """
                {
                    exceptions(
                        filters: {
                            interfaceTypes: [ORDER, COLLECTION]
                            statuses: [PENDING, FAILED]
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
                                transactionId
                                interfaceType
                                status
                                severity
                                timestamp
                                exceptionReason
                            }
                        }
                        pageInfo {
                            hasNextPage
                            endCursor
                        }
                    }
                }
                """;

        List<Long> responseTimes = new ArrayList<>();

        // Execute multiple queries to get statistical data
        for (int i = 0; i < 100; i++) {
            long startTime = System.currentTimeMillis();

            MvcResult result = mockMvc.perform(post(GRAPHQL_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createGraphQLRequest(query)))
                    .andExpect(status().isOk())
                    .andReturn();

            long responseTime = System.currentTimeMillis() - startTime;
            responseTimes.add(responseTime);

            // Verify response structure
            String responseContent = result.getResponse().getContentAsString();
            assertFalse(responseContent.contains("errors"), "Query should not contain errors");
            assertTrue(responseContent.contains("exceptions"), "Response should contain exceptions data");
        }

        // Calculate performance statistics
        responseTimes.sort(Long::compareTo);
        long p95ResponseTime = responseTimes.get((int) (responseTimes.size() * 0.95));
        double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        log.info("Exception list query performance - Avg: {}ms, P95: {}ms", avgResponseTime, p95ResponseTime);

        // Validate performance requirements
        assertTrue(p95ResponseTime <= LIST_QUERY_MAX_TIME_MS,
                String.format("P95 response time (%dms) exceeds requirement (%dms)",
                        p95ResponseTime, LIST_QUERY_MAX_TIME_MS));
    }

    /**
     * Tests exception detail query performance.
     * Validates requirement 1.5: Detail queries respond within 1s (95th
     * percentile).
     */
    @Test
    void testExceptionDetailQueryPerformance() throws Exception {
        log.info("Testing exception detail query performance");

        String query = """
                {
                    exception(transactionId: "test-transaction-001") {
                        transactionId
                        interfaceType
                        status
                        severity
                        exceptionReason
                        originalPayload {
                            content
                            contentType
                            retrievedAt
                        }
                        retryHistory {
                            attemptNumber
                            status
                            initiatedAt
                            resultSuccess
                        }
                        statusHistory {
                            previousStatus
                            newStatus
                            changedAt
                            changedBy
                        }
                    }
                }
                """;

        List<Long> responseTimes = new ArrayList<>();

        // Execute multiple detail queries
        for (int i = 0; i < 50; i++) {
            long startTime = System.currentTimeMillis();

            mockMvc.perform(post(GRAPHQL_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createGraphQLRequest(query)))
                    .andExpect(status().isOk());

            long responseTime = System.currentTimeMillis() - startTime;
            responseTimes.add(responseTime);
        }

        // Calculate performance statistics
        responseTimes.sort(Long::compareTo);
        long p95ResponseTime = responseTimes.get((int) (responseTimes.size() * 0.95));
        double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        log.info("Exception detail query performance - Avg: {}ms, P95: {}ms", avgResponseTime, p95ResponseTime);

        // Validate performance requirements
        assertTrue(p95ResponseTime <= DETAIL_QUERY_MAX_TIME_MS,
                String.format("P95 response time (%dms) exceeds requirement (%dms)",
                        p95ResponseTime, DETAIL_QUERY_MAX_TIME_MS));
    }

    /**
     * Tests summary statistics query performance.
     * Validates requirement 4.4: Dashboard queries respond within 200ms (95th
     * percentile).
     */
    @Test
    void testSummaryQueryPerformance() throws Exception {
        log.info("Testing summary statistics query performance");

        String query = """
                {
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
                        trends {
                            timestamp
                            count
                        }
                        keyMetrics {
                            retrySuccessRate
                            avgResolutionTime
                            customerImpactScore
                        }
                    }
                }
                """;

        List<Long> responseTimes = new ArrayList<>();

        // Execute multiple summary queries
        for (int i = 0; i < 50; i++) {
            long startTime = System.currentTimeMillis();

            mockMvc.perform(post(GRAPHQL_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createGraphQLRequest(query)))
                    .andExpect(status().isOk());

            long responseTime = System.currentTimeMillis() - startTime;
            responseTimes.add(responseTime);
        }

        // Calculate performance statistics
        responseTimes.sort(Long::compareTo);
        long p95ResponseTime = responseTimes.get((int) (responseTimes.size() * 0.95));
        double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        log.info("Summary query performance - Avg: {}ms, P95: {}ms", avgResponseTime, p95ResponseTime);

        // Validate dashboard performance requirement (200ms)
        assertTrue(p95ResponseTime <= 200,
                String.format("P95 response time (%dms) exceeds dashboard requirement (200ms)",
                        p95ResponseTime));
    }

    /**
     * Tests concurrent load performance.
     * Validates system behavior under concurrent user load.
     */
    @Test
    void testConcurrentLoadPerformance() throws Exception {
        log.info("Testing concurrent load performance with {} users, {} queries each",
                CONCURRENT_USERS, QUERIES_PER_USER);

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        String listQuery = """
                {
                    exceptions(pagination: { first: 10 }) {
                        edges {
                            node {
                                transactionId
                                status
                                timestamp
                            }
                        }
                    }
                }
                """;

        long testStartTime = System.currentTimeMillis();

        // Create concurrent users
        for (int user = 0; user < CONCURRENT_USERS; user++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    for (int query = 0; query < QUERIES_PER_USER; query++) {
                        mockMvc.perform(post(GRAPHQL_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createGraphQLRequest(listQuery)))
                                .andExpect(status().isOk());
                    }
                } catch (Exception e) {
                    log.error("Error in concurrent test", e);
                    throw new RuntimeException(e);
                }
            }, executor);

            futures.add(future);
        }

        // Wait for all requests to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(60, TimeUnit.SECONDS);

        long totalTestTime = System.currentTimeMillis() - testStartTime;
        int totalQueries = CONCURRENT_USERS * QUERIES_PER_USER;
        double throughput = (totalQueries * 1000.0) / totalTestTime; // queries per second

        log.info("Concurrent load test completed - {} queries in {}ms, throughput: {:.2f} qps",
                totalQueries, totalTestTime, throughput);

        executor.shutdown();

        // Validate minimum throughput requirement
        assertTrue(throughput >= 50,
                String.format("Throughput (%.2f qps) below minimum requirement (50 qps)", throughput));
    }

    /**
     * Tests cache performance and hit rate.
     * Validates requirement 8.4: Cache hit rate > 80% for dashboard summary
     * queries.
     */
    @Test
    void testCachePerformance() throws Exception {
        log.info("Testing cache performance and hit rate");

        String summaryQuery = """
                {
                    exceptionSummary(timeRange: LAST_24_HOURS) {
                        totalExceptions
                        byInterfaceType {
                            interfaceType
                            count
                        }
                    }
                }
                """;

        // Execute the same query multiple times to test caching
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(post(GRAPHQL_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createGraphQLRequest(summaryQuery)))
                    .andExpect(status().isOk());
        }

        // Get performance summary to check cache hit rate
        PerformanceMonitoringService.PerformanceSummary summary = performanceMonitoringService.getPerformanceSummary();

        log.info("Cache performance - Hit rate: {:.2f}%", summary.getCacheHitRate());

        // Validate cache hit rate requirement
        assertTrue(summary.getCacheHitRate() >= MIN_CACHE_HIT_RATE,
                String.format("Cache hit rate (%.2f%%) below requirement (%.2f%%)",
                        summary.getCacheHitRate(), MIN_CACHE_HIT_RATE));
    }

    /**
     * Tests DataLoader batching effectiveness.
     * Validates that N+1 query problems are prevented.
     */
    @Test
    void testDataLoaderBatchingPerformance() throws Exception {
        log.info("Testing DataLoader batching performance");

        String queryWithNestedData = """
                {
                    exceptions(pagination: { first: 50 }) {
                        edges {
                            node {
                                transactionId
                                originalPayload {
                                    content
                                    contentType
                                }
                                retryHistory {
                                    attemptNumber
                                    status
                                }
                                statusHistory {
                                    newStatus
                                    changedAt
                                }
                            }
                        }
                    }
                }
                """;

        long startTime = System.currentTimeMillis();

        MvcResult result = mockMvc.perform(post(GRAPHQL_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createGraphQLRequest(queryWithNestedData)))
                .andExpect(status().isOk())
                .andReturn();

        long responseTime = System.currentTimeMillis() - startTime;

        log.info("DataLoader batching test completed in {}ms", responseTime);

        // Verify response contains nested data
        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("originalPayload"), "Response should contain payload data");
        assertTrue(responseContent.contains("retryHistory"), "Response should contain retry history");
        assertTrue(responseContent.contains("statusHistory"), "Response should contain status history");

        // DataLoader batching should keep response time reasonable even with nested
        // data
        assertTrue(responseTime <= 2000,
                String.format("DataLoader query time (%dms) indicates poor batching performance", responseTime));
    }

    /**
     * Creates a GraphQL request JSON payload.
     */
    private String createGraphQLRequest(String query) throws Exception {
        Map<String, Object> request = Map.of(
                "query", query,
                "variables", Map.of());
        return objectMapper.writeValueAsString(request);
    }
}