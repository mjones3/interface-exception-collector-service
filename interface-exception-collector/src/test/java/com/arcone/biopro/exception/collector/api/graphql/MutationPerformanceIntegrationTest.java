package com.arcone.biopro.exception.collector.api.graphql;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.*;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance-focused integration tests for mutation operations.
 * Validates response time requirements and concurrent operation handling.
 * 
 * Requirements covered:
 * - 7.1: Response within 2 seconds for individual operations (95th percentile)
 * - 7.4: Concurrent operations without data corruption
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Mutation Performance Integration Tests")
class MutationPerformanceIntegrationTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private RetryAttemptRepository retryAttemptRepository;

    private static final String RETRY_MUTATION = """
            mutation RetryException($input: RetryExceptionInput!) {
                retryException(input: $input) {
                    success
                    operationId
                    timestamp
                    exception {
                        transactionId
                        status
                    }
                }
            }
            """;

    private static final String ACKNOWLEDGE_MUTATION = """
            mutation AcknowledgeException($input: AcknowledgeExceptionInput!) {
                acknowledgeException(input: $input) {
                    success
                    operationId
                    timestamp
                    exception {
                        transactionId
                        status
                    }
                }
            }
            """;

    private static final String RESOLVE_MUTATION = """
            mutation ResolveException($input: ResolveExceptionInput!) {
                resolveException(input: $input) {
                    success
                    operationId
                    timestamp
                    exception {
                        transactionId
                        status
                    }
                }
            }
            """;

    private static final String CANCEL_RETRY_MUTATION = """
            mutation CancelRetry($transactionId: String!, $reason: String!) {
                cancelRetry(transactionId: $transactionId, reason: $reason) {
                    success
                    operationId
                    timestamp
                    exception {
                        transactionId
                        status
                    }
                }
            }
            """;

    @BeforeEach
    void setUp() {
        // Clean up test data
        retryAttemptRepository.deleteAll();
        exceptionRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Single retry mutation should complete within 2 seconds")
    void singleRetryMutation_ShouldCompleteWithin2Seconds() {
        // Given
        InterfaceException testException = createTestException("PERF-RETRY-SINGLE");
        
        // When
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> result = graphQlTester.document(RETRY_MUTATION)
                .variable("input", Map.of(
                        "transactionId", testException.getTransactionId(),
                        "reason", "Performance test retry",
                        "priority", "NORMAL"
                ))
                .execute()
                .path("retryException")
                .entity(Map.class)
                .get();
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Then
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(executionTime).isLessThan(2000); // Must complete within 2 seconds
        
        System.out.println("Retry mutation execution time: " + executionTime + "ms");
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Single acknowledge mutation should complete within 2 seconds")
    void singleAcknowledgeMutation_ShouldCompleteWithin2Seconds() {
        // Given
        InterfaceException testException = createTestException("PERF-ACK-SINGLE");
        
        // When
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> result = graphQlTester.document(ACKNOWLEDGE_MUTATION)
                .variable("input", Map.of(
                        "transactionId", testException.getTransactionId(),
                        "reason", "Performance test acknowledge",
                        "notes", "Performance testing notes"
                ))
                .execute()
                .path("acknowledgeException")
                .entity(Map.class)
                .get();
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Then
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(executionTime).isLessThan(2000);
        
        System.out.println("Acknowledge mutation execution time: " + executionTime + "ms");
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Single resolve mutation should complete within 2 seconds")
    void singleResolveMutation_ShouldCompleteWithin2Seconds() {
        // Given
        InterfaceException testException = createTestException("PERF-RESOLVE-SINGLE");
        testException.setStatus(ExceptionStatus.ACKNOWLEDGED);
        exceptionRepository.save(testException);
        
        // When
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> result = graphQlTester.document(RESOLVE_MUTATION)
                .variable("input", Map.of(
                        "transactionId", testException.getTransactionId(),
                        "resolutionMethod", "MANUAL_RESOLUTION",
                        "resolutionNotes", "Performance test resolution"
                ))
                .execute()
                .path("resolveException")
                .entity(Map.class)
                .get();
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Then
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(executionTime).isLessThan(2000);
        
        System.out.println("Resolve mutation execution time: " + executionTime + "ms");
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Batch of 10 retry mutations should maintain performance")
    void batchRetryMutations_ShouldMaintainPerformance() {
        // Given
        List<InterfaceException> testExceptions = IntStream.range(0, 10)
                .mapToObj(i -> createTestException("PERF-BATCH-" + i))
                .toList();
        
        List<Long> executionTimes = new ArrayList<>();
        
        // When
        for (InterfaceException exception : testExceptions) {
            long startTime = System.currentTimeMillis();
            
            Map<String, Object> result = graphQlTester.document(RETRY_MUTATION)
                    .variable("input", Map.of(
                            "transactionId", exception.getTransactionId(),
                            "reason", "Batch performance test",
                            "priority", "NORMAL"
                    ))
                    .execute()
                    .path("retryException")
                    .entity(Map.class)
                    .get();
            
            long executionTime = System.currentTimeMillis() - startTime;
            executionTimes.add(executionTime);
            
            assertThat(result.get("success")).isEqualTo(true);
        }
        
        // Then
        // Calculate 95th percentile
        executionTimes.sort(Long::compareTo);
        int p95Index = (int) Math.ceil(0.95 * executionTimes.size()) - 1;
        long p95Time = executionTimes.get(p95Index);
        
        assertThat(p95Time).isLessThan(2000); // 95th percentile must be under 2 seconds
        
        double avgTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        System.out.println("Batch retry mutations - Average: " + avgTime + "ms, 95th percentile: " + p95Time + "ms");
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Concurrent mutations should maintain performance and data integrity")
    void concurrentMutations_ShouldMaintainPerformanceAndIntegrity() throws Exception {
        // Given
        int concurrentOperations = 20;
        List<InterfaceException> testExceptions = IntStream.range(0, concurrentOperations)
                .mapToObj(i -> createTestException("PERF-CONCURRENT-" + i))
                .toList();
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        // When
        long overallStartTime = System.currentTimeMillis();
        
        for (InterfaceException exception : testExceptions) {
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                
                try {
                    Map<String, Object> result = graphQlTester.document(RETRY_MUTATION)
                            .variable("input", Map.of(
                                    "transactionId", exception.getTransactionId(),
                                    "reason", "Concurrent performance test",
                                    "priority", "NORMAL"
                            ))
                            .execute()
                            .path("retryException")
                            .entity(Map.class)
                            .get();
                    
                    assertThat(result.get("success")).isEqualTo(true);
                    return System.currentTimeMillis() - startTime;
                } catch (Exception e) {
                    throw new RuntimeException("Concurrent operation failed", e);
                }
            }, executor);
            
            futures.add(future);
        }
        
        // Wait for all operations to complete
        List<Long> executionTimes = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        
        long overallExecutionTime = System.currentTimeMillis() - overallStartTime;
        
        // Then
        // Verify all operations completed successfully
        assertThat(executionTimes).hasSize(concurrentOperations);
        
        // Verify performance requirements
        executionTimes.sort(Long::compareTo);
        int p95Index = (int) Math.ceil(0.95 * executionTimes.size()) - 1;
        long p95Time = executionTimes.get(p95Index);
        
        assertThat(p95Time).isLessThan(2000); // Individual operations under 2 seconds
        
        // Verify data integrity - each exception should have exactly one retry
        for (InterfaceException exception : testExceptions) {
            InterfaceException updated = exceptionRepository
                    .findByTransactionId(exception.getTransactionId())
                    .orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(ExceptionStatus.RETRYING);
            assertThat(updated.getRetryCount()).isEqualTo(1);
        }
        
        double avgTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        System.out.println("Concurrent mutations - Operations: " + concurrentOperations + 
                          ", Overall time: " + overallExecutionTime + "ms" +
                          ", Average individual: " + avgTime + "ms" +
                          ", 95th percentile: " + p95Time + "ms");
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Mixed mutation types should maintain performance under load")
    void mixedMutationTypes_ShouldMaintainPerformanceUnderLoad() throws Exception {
        // Given
        int operationsPerType = 5;
        List<InterfaceException> retryExceptions = IntStream.range(0, operationsPerType)
                .mapToObj(i -> createTestException("PERF-MIXED-RETRY-" + i))
                .toList();
        
        List<InterfaceException> acknowledgeExceptions = IntStream.range(0, operationsPerType)
                .mapToObj(i -> createTestException("PERF-MIXED-ACK-" + i))
                .toList();
        
        List<InterfaceException> resolveExceptions = IntStream.range(0, operationsPerType)
                .mapToObj(i -> {
                    InterfaceException ex = createTestException("PERF-MIXED-RESOLVE-" + i);
                    ex.setStatus(ExceptionStatus.ACKNOWLEDGED);
                    return exceptionRepository.save(ex);
                })
                .toList();
        
        ExecutorService executor = Executors.newFixedThreadPool(15);
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        // When - Execute mixed mutation types concurrently
        long overallStartTime = System.currentTimeMillis();
        
        // Add retry operations
        for (InterfaceException exception : retryExceptions) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                graphQlTester.document(RETRY_MUTATION)
                        .variable("input", Map.of(
                                "transactionId", exception.getTransactionId(),
                                "reason", "Mixed load test retry",
                                "priority", "NORMAL"
                        ))
                        .execute()
                        .path("retryException.success")
                        .entity(Boolean.class)
                        .isEqualTo(true);
                return System.currentTimeMillis() - startTime;
            }, executor));
        }
        
        // Add acknowledge operations
        for (InterfaceException exception : acknowledgeExceptions) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                graphQlTester.document(ACKNOWLEDGE_MUTATION)
                        .variable("input", Map.of(
                                "transactionId", exception.getTransactionId(),
                                "reason", "Mixed load test acknowledge",
                                "notes", "Load test notes"
                        ))
                        .execute()
                        .path("acknowledgeException.success")
                        .entity(Boolean.class)
                        .isEqualTo(true);
                return System.currentTimeMillis() - startTime;
            }, executor));
        }
        
        // Add resolve operations
        for (InterfaceException exception : resolveExceptions) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                graphQlTester.document(RESOLVE_MUTATION)
                        .variable("input", Map.of(
                                "transactionId", exception.getTransactionId(),
                                "resolutionMethod", "MANUAL_RESOLUTION",
                                "resolutionNotes", "Mixed load test resolution"
                        ))
                        .execute()
                        .path("resolveException.success")
                        .entity(Boolean.class)
                        .isEqualTo(true);
                return System.currentTimeMillis() - startTime;
            }, executor));
        }
        
        // Wait for all operations
        List<Long> executionTimes = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        
        long overallExecutionTime = System.currentTimeMillis() - overallStartTime;
        
        // Then
        assertThat(executionTimes).hasSize(operationsPerType * 3);
        
        // Verify performance requirements
        executionTimes.sort(Long::compareTo);
        int p95Index = (int) Math.ceil(0.95 * executionTimes.size()) - 1;
        long p95Time = executionTimes.get(p95Index);
        
        assertThat(p95Time).isLessThan(2000);
        
        double avgTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        System.out.println("Mixed mutation types - Total operations: " + executionTimes.size() + 
                          ", Overall time: " + overallExecutionTime + "ms" +
                          ", Average individual: " + avgTime + "ms" +
                          ", 95th percentile: " + p95Time + "ms");
        
        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Error scenarios should not impact performance significantly")
    void errorScenarios_ShouldNotImpactPerformanceSignificantly() {
        // Given
        List<Long> successTimes = new ArrayList<>();
        List<Long> errorTimes = new ArrayList<>();
        
        // Test successful operations
        for (int i = 0; i < 5; i++) {
            InterfaceException validException = createTestException("PERF-ERROR-VALID-" + i);
            
            long startTime = System.currentTimeMillis();
            graphQlTester.document(RETRY_MUTATION)
                    .variable("input", Map.of(
                            "transactionId", validException.getTransactionId(),
                            "reason", "Performance error test - valid",
                            "priority", "NORMAL"
                    ))
                    .execute()
                    .path("retryException.success")
                    .entity(Boolean.class)
                    .isEqualTo(true);
            successTimes.add(System.currentTimeMillis() - startTime);
        }
        
        // Test error operations (non-existent transactions)
        for (int i = 0; i < 5; i++) {
            long startTime = System.currentTimeMillis();
            graphQlTester.document(RETRY_MUTATION)
                    .variable("input", Map.of(
                            "transactionId", "NON-EXISTENT-" + i,
                            "reason", "Performance error test - invalid",
                            "priority", "NORMAL"
                    ))
                    .execute()
                    .path("retryException.success")
                    .entity(Boolean.class)
                    .isEqualTo(false);
            errorTimes.add(System.currentTimeMillis() - startTime);
        }
        
        // Then
        double avgSuccessTime = successTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgErrorTime = errorTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        
        // Error handling should not be significantly slower than success cases
        assertThat(avgErrorTime).isLessThan(avgSuccessTime * 2); // Allow up to 2x slower for errors
        assertThat(avgErrorTime).isLessThan(2000); // Still under 2 seconds
        
        System.out.println("Error performance - Success avg: " + avgSuccessTime + "ms, Error avg: " + avgErrorTime + "ms");
    }

    private InterfaceException createTestException(String transactionId) {
        InterfaceException exception = new InterfaceException();
        exception.setTransactionId(transactionId);
        exception.setExternalId("EXT-" + transactionId);
        exception.setInterfaceType(InterfaceType.ORDER_PROCESSING);
        exception.setOperation("CREATE_ORDER");
        exception.setStatus(ExceptionStatus.NEW);
        exception.setExceptionReason("Performance test exception");
        exception.setSeverity(ExceptionSeverity.MEDIUM);
        exception.setCategory("TECHNICAL");
        exception.setCustomerId("CUST-PERF");
        exception.setLocationCode("LOC-PERF");
        exception.setTimestamp(OffsetDateTime.now());
        exception.setRetryable(true);
        exception.setRetryCount(0);
        exception.setMaxRetries(3);
        exception.setPayload("{\"test\": \"performance\"}");
        
        return exceptionRepository.save(exception);
    }
}