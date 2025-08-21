package com.arcone.biopro.exception.collector.api;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.*;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Performance comparison tests between REST and GraphQL APIs.
 * Validates that both APIs perform within acceptable limits and compares their
 * performance characteristics.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class DualApiPerformanceComparisonTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private HttpGraphQlTester graphQlTester;

    private static final int PERFORMANCE_TEST_ITERATIONS = 10;
    private static final int CONCURRENT_REQUESTS = 5;
    private static final long ACCEPTABLE_RESPONSE_TIME_MS = 1000;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        graphQlTester = HttpGraphQlTester.create(mockMvc);

        // Create test data for performance testing
        createPerformanceTestData();
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void exceptionListPerformance_RestVsGraphQL_ShouldBeComparable() throws Exception {
        // Warm up
        performWarmupRequests();

        // Test REST API performance
        StopWatch restStopWatch = new StopWatch("REST API Performance");
        restStopWatch.start();

        for (int i = 0; i < PERFORMANCE_TEST_ITERATIONS; i++) {
            mockMvc.perform(get("/api/v1/exceptions")
                    .param("page", "0")
                    .param("size", "20")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        restStopWatch.stop();
        long restAverageTime = restStopWatch.getTotalTimeMillis() / PERFORMANCE_TEST_ITERATIONS;

        // Test GraphQL API performance
        String graphqlQuery = """
                query {
                    exceptions(pagination: { first: 20 }) {
                        edges {
                            node {
                                transactionId
                                interfaceType
                                exceptionReason
                                status
                                severity
                                timestamp
                            }
                        }
                        totalCount
                    }
                }
                """;

        StopWatch graphqlStopWatch = new StopWatch("GraphQL API Performance");
        graphqlStopWatch.start();

        for (int i = 0; i < PERFORMANCE_TEST_ITERATIONS; i++) {
            graphQlTester.document(graphqlQuery)
                    .execute()
                    .path("exceptions.totalCount")
                    .entity(Long.class)
                    .isGreaterThanOrEqualTo(0L);
        }

        graphqlStopWatch.stop();
        long graphqlAverageTime = graphqlStopWatch.getTotalTimeMillis() / PERFORMANCE_TEST_ITERATIONS;

        // Assertions
        assertThat(restAverageTime).isLessThan(ACCEPTABLE_RESPONSE_TIME_MS);
        assertThat(graphqlAverageTime).isLessThan(ACCEPTABLE_RESPONSE_TIME_MS);

        // GraphQL should not be significantly slower than REST (within 50% difference)
        double performanceDifference = Math.abs(graphqlAverageTime - restAverageTime) / (double) restAverageTime;
        assertThat(performanceDifference).isLessThan(0.5);

        System.out.printf("REST API average time: %d ms%n", restAverageTime);
        System.out.printf("GraphQL API average time: %d ms%n", graphqlAverageTime);
        System.out.printf("Performance difference: %.2f%%%n", performanceDifference * 100);
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void exceptionDetailPerformance_RestVsGraphQL_ShouldBeComparable() throws Exception {
        // Test REST API performance for detail retrieval
        StopWatch restStopWatch = new StopWatch("REST Detail Performance");
        restStopWatch.start();

        for (int i = 0; i < PERFORMANCE_TEST_ITERATIONS; i++) {
            mockMvc.perform(get("/api/v1/exceptions/TXN-{i}", i % 100)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        restStopWatch.stop();
        long restAverageTime = restStopWatch.getTotalTimeMillis() / PERFORMANCE_TEST_ITERATIONS;

        // Test GraphQL API performance for detail retrieval
        String graphqlQuery = """
                query($transactionId: String!) {
                    exception(transactionId: $transactionId) {
                        transactionId
                        interfaceType
                        exceptionReason
                        status
                        severity
                        category
                        customerId
                        locationCode
                        timestamp
                        retryable
                        retryCount
                    }
                }
                """;

        StopWatch graphqlStopWatch = new StopWatch("GraphQL Detail Performance");
        graphqlStopWatch.start();

        for (int i = 0; i < PERFORMANCE_TEST_ITERATIONS; i++) {
            Map<String, Object> variables = Map.of("transactionId", "TXN-" + (i % 100));

            graphQlTester.document(graphqlQuery)
                    .variables(variables)
                    .execute()
                    .path("exception")
                    .hasValue();
        }

        graphqlStopWatch.stop();
        long graphqlAverageTime = graphqlStopWatch.getTotalTimeMillis() / PERFORMANCE_TEST_ITERATIONS;

        // Assertions
        assertThat(restAverageTime).isLessThan(ACCEPTABLE_RESPONSE_TIME_MS);
        assertThat(graphqlAverageTime).isLessThan(ACCEPTABLE_RESPONSE_TIME_MS);

        System.out.printf("REST Detail average time: %d ms%n", restAverageTime);
        System.out.printf("GraphQL Detail average time: %d ms%n", graphqlAverageTime);
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void concurrentRequestPerformance_BothAPIs_ShouldHandleLoad() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);

        // Test REST API concurrent performance
        List<CompletableFuture<Long>> restFutures = new ArrayList<>();
        StopWatch restConcurrentStopWatch = new StopWatch("REST Concurrent Performance");
        restConcurrentStopWatch.start();

        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    mockMvc.perform(get("/api/v1/exceptions")
                            .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk());
                    return System.currentTimeMillis() - startTime;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
            restFutures.add(future);
        }

        CompletableFuture.allOf(restFutures.toArray(new CompletableFuture[0])).join();
        restConcurrentStopWatch.stop();

        long restMaxTime = restFutures.stream()
                .mapToLong(CompletableFuture::join)
                .max()
                .orElse(0);

        // Test GraphQL API concurrent performance
        String graphqlQuery = """
                query {
                    exceptions(pagination: { first: 20 }) {
                        edges {
                            node {
                                transactionId
                                status
                            }
                        }
                        totalCount
                    }
                }
                """;

        List<CompletableFuture<Long>> graphqlFutures = new ArrayList<>();
        StopWatch graphqlConcurrentStopWatch = new StopWatch("GraphQL Concurrent Performance");
        graphqlConcurrentStopWatch.start();

        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    graphQlTester.document(graphqlQuery)
                            .execute()
                            .path("exceptions.totalCount")
                            .entity(Long.class)
                            .isGreaterThanOrEqualTo(0L);
                    return System.currentTimeMillis() - startTime;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
            graphqlFutures.add(future);
        }

        CompletableFuture.allOf(graphqlFutures.toArray(new CompletableFuture[0])).join();
        graphqlConcurrentStopWatch.stop();

        long graphqlMaxTime = graphqlFutures.stream()
                .mapToLong(CompletableFuture::join)
                .max()
                .orElse(0);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Assertions
        assertThat(restMaxTime).isLessThan(ACCEPTABLE_RESPONSE_TIME_MS * 2); // Allow more time for concurrent requests
        assertThat(graphqlMaxTime).isLessThan(ACCEPTABLE_RESPONSE_TIME_MS * 2);

        System.out.printf("REST concurrent max time: %d ms%n", restMaxTime);
        System.out.printf("GraphQL concurrent max time: %d ms%n", graphqlMaxTime);
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void complexQueryPerformance_GraphQL_ShouldOptimizeDataFetching() throws Exception {
        // Test GraphQL's ability to fetch related data efficiently
        String complexGraphqlQuery = """
                query {
                    exceptions(pagination: { first: 10 }) {
                        edges {
                            node {
                                transactionId
                                interfaceType
                                exceptionReason
                                status
                                severity
                                retryHistory {
                                    attemptNumber
                                    status
                                    initiatedBy
                                }
                                originalPayload {
                                    contentType
                                    sourceService
                                }
                                statusHistory {
                                    previousStatus
                                    newStatus
                                    changedBy
                                }
                            }
                        }
                        totalCount
                    }
                }
                """;

        // Test equivalent REST API calls (multiple requests needed)
        StopWatch restComplexStopWatch = new StopWatch("REST Complex Data Fetching");
        restComplexStopWatch.start();

        for (int i = 0; i < 5; i++) {
            // Main exception list
            mockMvc.perform(get("/api/v1/exceptions")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Additional requests for related data would be needed
            mockMvc.perform(get("/api/v1/exceptions/TXN-{i}/retry-history", i)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/exceptions/TXN-{i}/payload", i)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        restComplexStopWatch.stop();
        long restComplexTime = restComplexStopWatch.getTotalTimeMillis() / 5;

        // Test GraphQL complex query performance
        StopWatch graphqlComplexStopWatch = new StopWatch("GraphQL Complex Data Fetching");
        graphqlComplexStopWatch.start();

        for (int i = 0; i < 5; i++) {
            graphQlTester.document(complexGraphqlQuery)
                    .execute()
                    .path("exceptions.totalCount")
                    .entity(Long.class)
                    .isGreaterThanOrEqualTo(0L);
        }

        graphqlComplexStopWatch.stop();
        long graphqlComplexTime = graphqlComplexStopWatch.getTotalTimeMillis() / 5;

        // GraphQL should be more efficient for complex data fetching
        assertThat(graphqlComplexTime).isLessThan(restComplexTime);

        System.out.printf("REST complex data fetching average time: %d ms%n", restComplexTime);
        System.out.printf("GraphQL complex data fetching average time: %d ms%n", graphqlComplexTime);
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void paginationPerformance_BothAPIs_ShouldScaleWell() throws Exception {
        int[] pageSizes = { 10, 50, 100 };

        for (int pageSize : pageSizes) {
            // Test REST API pagination performance
            StopWatch restPaginationStopWatch = new StopWatch("REST Pagination " + pageSize);
            restPaginationStopWatch.start();

            mockMvc.perform(get("/api/v1/exceptions")
                    .param("page", "0")
                    .param("size", String.valueOf(pageSize))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            restPaginationStopWatch.stop();

            // Test GraphQL API pagination performance
            String paginationQuery = String.format("""
                    query {
                        exceptions(pagination: { first: %d }) {
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
                    """, pageSize);

            StopWatch graphqlPaginationStopWatch = new StopWatch("GraphQL Pagination " + pageSize);
            graphqlPaginationStopWatch.start();

            graphQlTester.document(paginationQuery)
                    .execute()
                    .path("exceptions.totalCount")
                    .entity(Long.class)
                    .isGreaterThanOrEqualTo(0L);

            graphqlPaginationStopWatch.stop();

            // Both should complete within acceptable time regardless of page size
            assertThat(restPaginationStopWatch.getTotalTimeMillis()).isLessThan(ACCEPTABLE_RESPONSE_TIME_MS);
            assertThat(graphqlPaginationStopWatch.getTotalTimeMillis()).isLessThan(ACCEPTABLE_RESPONSE_TIME_MS);

            System.out.printf("Page size %d - REST: %d ms, GraphQL: %d ms%n",
                    pageSize,
                    restPaginationStopWatch.getTotalTimeMillis(),
                    graphqlPaginationStopWatch.getTotalTimeMillis());
        }
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void memoryUsageComparison_BothAPIs_ShouldBeEfficient() throws Exception {
        // This test would typically use profiling tools to measure memory usage
        // For now, we'll test that both APIs can handle large result sets without
        // issues

        Runtime runtime = Runtime.getRuntime();

        // Measure memory before REST API calls
        System.gc();
        long memoryBeforeRest = runtime.totalMemory() - runtime.freeMemory();

        // Make multiple REST API calls
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(get("/api/v1/exceptions")
                    .param("page", String.valueOf(i))
                    .param("size", "50")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        System.gc();
        long memoryAfterRest = runtime.totalMemory() - runtime.freeMemory();
        long restMemoryUsage = memoryAfterRest - memoryBeforeRest;

        // Measure memory before GraphQL API calls
        System.gc();
        long memoryBeforeGraphql = runtime.totalMemory() - runtime.freeMemory();

        String graphqlQuery = """
                query($first: Int!, $after: String) {
                    exceptions(pagination: { first: $first, after: $after }) {
                        edges {
                            node {
                                transactionId
                                interfaceType
                                status
                            }
                            cursor
                        }
                        pageInfo {
                            hasNextPage
                            endCursor
                        }
                    }
                }
                """;

        // Make multiple GraphQL API calls
        String cursor = null;
        for (int i = 0; i < 20; i++) {
            Map<String, Object> variables = Map.of(
                    "first", 50,
                    "after", cursor != null ? cursor : "");

            var response = graphQlTester.document(graphqlQuery)
                    .variables(variables)
                    .execute();

            // Get cursor for next page (in real implementation)
            cursor = "cursor-" + i; // Simplified for test
        }

        System.gc();
        long memoryAfterGraphql = runtime.totalMemory() - runtime.freeMemory();
        long graphqlMemoryUsage = memoryAfterGraphql - memoryBeforeGraphql;

        // Both APIs should have reasonable memory usage
        assertThat(restMemoryUsage).isLessThan(100 * 1024 * 1024); // Less than 100MB
        assertThat(graphqlMemoryUsage).isLessThan(100 * 1024 * 1024); // Less than 100MB

        System.out.printf("REST memory usage: %d bytes%n", restMemoryUsage);
        System.out.printf("GraphQL memory usage: %d bytes%n", graphqlMemoryUsage);
    }

    private void performWarmupRequests() throws Exception {
        // Warm up both APIs to ensure fair performance comparison
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/v1/exceptions")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            graphQlTester.document("query { exceptions { totalCount } }")
                    .execute()
                    .path("exceptions.totalCount")
                    .entity(Long.class)
                    .isGreaterThanOrEqualTo(0L);
        }
    }

    private void createPerformanceTestData() {
        List<InterfaceException> exceptions = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            InterfaceException exception = InterfaceException.builder()
                    .transactionId("TXN-" + i)
                    .interfaceType(InterfaceType.values()[i % InterfaceType.values().length])
                    .exceptionReason("Performance test exception " + i)
                    .operation("CREATE_" + (i % 3 == 0 ? "ORDER" : i % 3 == 1 ? "COLLECTION" : "DISTRIBUTION"))
                    .externalId("EXT-" + i)
                    .status(ExceptionStatus.values()[i % ExceptionStatus.values().length])
                    .severity(ExceptionSeverity.values()[i % ExceptionSeverity.values().length])
                    .category(ExceptionCategory.values()[i % ExceptionCategory.values().length])
                    .retryable(i % 2 == 0)
                    .customerId("CUST-" + (i % 10))
                    .locationCode("LOC-" + (i % 5))
                    .timestamp(OffsetDateTime.now().minusHours(i))
                    .processedAt(OffsetDateTime.now().minusHours(i))
                    .retryCount(i % 3)
                    .maxRetries(3)
                    .build();

            exceptions.add(exception);
        }

        exceptionRepository.saveAll(exceptions);
    }
}