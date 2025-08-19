package com.arcone.biopro.exception.collector.api.graphql;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Utility class providing common functionality for GraphQL integration tests.
 */
public class GraphQLTestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a test exception with the specified parameters.
     */
    public static InterfaceException createTestException(
            String transactionId,
            InterfaceType interfaceType,
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

    /**
     * Creates a test retry attempt for the given exception.
     */
    public static RetryAttempt createTestRetryAttempt(InterfaceException exception, int attemptNumber) {
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

    /**
     * Sets up default WireMock stubs for external service calls.
     */
    public static void setupDefaultWireMockStubs(WireMockServer wireMockServer) {
        // Mock payload retrieval endpoints
        wireMockServer.stubFor(get(urlPathMatching("/api/v1/orders/.*/payload"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(createMockPayload("ORDER"))));

        wireMockServer.stubFor(get(urlPathMatching("/api/v1/collections/.*/payload"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(createMockPayload("COLLECTION"))));

        wireMockServer.stubFor(get(urlPathMatching("/api/v1/distributions/.*/payload"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(createMockPayload("DISTRIBUTION"))));

        // Mock retry endpoints
        wireMockServer.stubFor(post(urlPathMatching("/api/v1/orders/.*/retry"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true,\"message\":\"Retry initiated successfully\"}")));

        wireMockServer.stubFor(post(urlPathMatching("/api/v1/collections/.*/retry"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true,\"message\":\"Retry initiated successfully\"}")));

        wireMockServer.stubFor(post(urlPathMatching("/api/v1/distributions/.*/retry"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true,\"message\":\"Retry initiated successfully\"}")));
    }

    /**
     * Creates mock payload data for different interface types.
     */
    private static String createMockPayload(String interfaceType) {
        try {
            Map<String, Object> payload = switch (interfaceType) {
                case "ORDER" -> Map.of(
                        "orderId", "ORDER-001",
                        "customerId", "CUST-001",
                        "items", List.of(Map.of("sku", "ITEM-001", "quantity", 2)));
                case "COLLECTION" -> Map.of(
                        "collectionId", "COL-001",
                        "orderId", "ORDER-001",
                        "status", "PENDING");
                case "DISTRIBUTION" -> Map.of(
                        "distributionId", "DIST-001",
                        "orderId", "ORDER-001",
                        "status", "FAILED");
                default -> Map.of("type", interfaceType, "data", "mock-data");
            };
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return "{\"error\":\"Failed to create mock payload\"}";
        }
    }

    /**
     * Sets up WireMock stubs for failure scenarios.
     */
    public static void setupFailureWireMockStubs(WireMockServer wireMockServer) {
        // Mock external service failures
        wireMockServer.stubFor(post(urlPathMatching("/api/v1/orders/.*/retry"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("{\"error\":\"Internal server error\"}")));

        wireMockServer.stubFor(post(urlPathMatching("/api/v1/collections/.*/retry"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("{\"error\":\"Service unavailable\"}")));

        wireMockServer.stubFor(post(urlPathMatching("/api/v1/distributions/.*/retry"))
                .willReturn(aResponse()
                        .withStatus(408)
                        .withBody("{\"error\":\"Request timeout\"}")));

        // Mock payload retrieval failures
        wireMockServer.stubFor(get(urlPathMatching("/api/v1/orders/.*/payload"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("{\"error\":\"Payload not found\"}")));
    }

    /**
     * Validates GraphQL response structure and timing.
     */
    public static void validateResponseTiming(GraphQlTester.Response response, long maxTimeMs) {
        long startTime = System.currentTimeMillis();
        response.errors().verify();
        long endTime = System.currentTimeMillis();

        if (endTime - startTime > maxTimeMs) {
            throw new AssertionError(
                    String.format("Response took %d ms, expected less than %d ms",
                            endTime - startTime, maxTimeMs));
        }
    }

    /**
     * Creates a batch of test exceptions for performance testing.
     */
    public static List<InterfaceException> createTestExceptionBatch(int count, String prefix) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> createTestException(
                        prefix + "-" + String.format("%06d", i),
                        InterfaceType.values()[i % InterfaceType.values().length],
                        ExceptionStatus.values()[i % ExceptionStatus.values().length]))
                .toList();
    }

    /**
     * Waits for a condition to be met within a timeout period.
     */
    public static void waitForCondition(Runnable condition, long timeoutMs, long intervalMs)
            throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                condition.run();
                return; // Condition met
            } catch (Exception e) {
                Thread.sleep(intervalMs);
            }
        }
        throw new AssertionError("Condition not met within timeout period");
    }

    /**
     * Creates a GraphQL query for exception listing with all parameters.
     */
    public static String createExceptionListQuery() {
        return """
                query($filters: ExceptionFilters, $pagination: PaginationInput, $sorting: SortingInput) {
                    exceptions(filters: $filters, pagination: $pagination, sorting: $sorting) {
                        edges {
                            node {
                                id
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
                """;
    }

    /**
     * Creates a GraphQL query for exception details with nested data.
     */
    public static String createExceptionDetailQuery() {
        return """
                query($transactionId: String!) {
                    exception(transactionId: $transactionId) {
                        id
                        transactionId
                        interfaceType
                        exceptionReason
                        operation
                        status
                        severity
                        category
                        customerId
                        locationCode
                        timestamp
                        retryable
                        retryCount
                        maxRetries
                        retryHistory {
                            attemptNumber
                            status
                            initiatedBy
                            initiatedAt
                            completedAt
                            resultSuccess
                            resultMessage
                            resultResponseCode
                        }
                        originalPayload {
                            content
                            contentType
                            sourceService
                            retrievedAt
                        }
                        statusHistory {
                            previousStatus
                            newStatus
                            changedBy
                            changedAt
                            reason
                            notes
                        }
                    }
                }
                """;
    }

    /**
     * Creates a GraphQL mutation for retrying exceptions.
     */
    public static String createRetryMutation() {
        return """
                mutation($transactionId: String!, $reason: String!, $priority: RetryPriority!) {
                    retryException(transactionId: $transactionId, reason: $reason, priority: $priority) {
                        success
                        message
                        exception {
                            transactionId
                            status
                            retryCount
                            lastRetryAt
                        }
                        retryAttempt {
                            attemptNumber
                            status
                            initiatedBy
                            initiatedAt
                        }
                    }
                }
                """;
    }

    /**
     * Creates a GraphQL mutation for acknowledging exceptions.
     */
    public static String createAcknowledgeMutation() {
        return """
                mutation($transactionId: String!, $reason: String!, $notes: String) {
                    acknowledgeException(transactionId: $transactionId, reason: $reason, notes: $notes) {
                        success
                        message
                        exception {
                            transactionId
                            status
                            acknowledgedBy
                            acknowledgedAt
                        }
                    }
                }
                """;
    }

    /**
     * Creates a GraphQL query for exception summary statistics.
     */
    public static String createSummaryQuery() {
        return """
                query($timeRange: TimeRange!, $filters: ExceptionFilters) {
                    exceptionSummary(timeRange: $timeRange, filters: $filters) {
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
                """;
    }

    /**
     * Measures execution time of a GraphQL operation.
     */
    public static long measureExecutionTime(Runnable operation) {
        long startTime = System.nanoTime();
        operation.run();
        long endTime = System.nanoTime();
        return TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
    }

    /**
     * Creates test variables for GraphQL queries.
     */
    public static Map<String, Object> createTestVariables() {
        return Map.of(
                "filters", Map.of(
                        "interfaceTypes", List.of("ORDER", "COLLECTION"),
                        "statuses", List.of("NEW", "ACKNOWLEDGED"),
                        "severities", List.of("HIGH", "CRITICAL")),
                "pagination", Map.of(
                        "first", 10),
                "sorting", Map.of(
                        "field", "timestamp",
                        "direction", "DESC"));
    }
}