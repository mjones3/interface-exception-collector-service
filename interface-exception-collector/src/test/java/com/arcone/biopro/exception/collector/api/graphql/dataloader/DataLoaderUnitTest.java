package com.arcone.biopro.exception.collector.api.graphql.dataloader;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DataLoader implementations.
 * Tests the core batching logic without Spring context.
 */
@ExtendWith(MockitoExtension.class)
class DataLoaderUnitTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private RetryAttemptRepository retryAttemptRepository;

    private ExceptionDataLoader exceptionDataLoader;
    private RetryHistoryDataLoader retryHistoryDataLoader;

    private InterfaceException testException1;
    private InterfaceException testException2;
    private RetryAttempt retryAttempt1;
    private RetryAttempt retryAttempt2;

    @BeforeEach
    void setUp() {
        exceptionDataLoader = new ExceptionDataLoader(exceptionRepository);
        retryHistoryDataLoader = new RetryHistoryDataLoader(retryAttemptRepository, exceptionRepository);

        // Create test data
        testException1 = InterfaceException.builder()
                .transactionId("TEST-001")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception 1")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.MEDIUM)
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .build();

        testException2 = InterfaceException.builder()
                .transactionId("TEST-002")
                .interfaceType(InterfaceType.COLLECTION)
                .exceptionReason("Test exception 2")
                .operation("COLLECT_SAMPLE")
                .status(ExceptionStatus.ACKNOWLEDGED)
                .severity(ExceptionSeverity.HIGH)
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .build();

        retryAttempt1 = RetryAttempt.builder()
                .interfaceException(testException1)
                .attemptNumber(1)
                .status(RetryStatus.SUCCESS)
                .initiatedBy("test-user")
                .initiatedAt(OffsetDateTime.now())
                .resultSuccess(true)
                .build();

        retryAttempt2 = RetryAttempt.builder()
                .interfaceException(testException1)
                .attemptNumber(2)
                .status(RetryStatus.FAILED)
                .initiatedBy("test-user")
                .initiatedAt(OffsetDateTime.now())
                .resultSuccess(false)
                .build();
    }

    @Test
    void testExceptionDataLoaderBatchLoad() throws Exception {
        // Given
        Set<String> transactionIds = Set.of("TEST-001", "TEST-002", "NON-EXISTENT");
        List<InterfaceException> mockExceptions = List.of(testException1, testException2);

        when(exceptionRepository.findByTransactionIdIn(transactionIds))
                .thenReturn(mockExceptions);

        // When
        CompletableFuture<Map<String, InterfaceException>> future = exceptionDataLoader.load(transactionIds)
                .toCompletableFuture();
        Map<String, InterfaceException> result = future.get();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsKey("TEST-001");
        assertThat(result).containsKey("TEST-002");
        assertThat(result).doesNotContainKey("NON-EXISTENT");

        assertThat(result.get("TEST-001").getInterfaceType()).isEqualTo(InterfaceType.ORDER);
        assertThat(result.get("TEST-002").getInterfaceType()).isEqualTo(InterfaceType.COLLECTION);
    }

    @Test
    void testExceptionDataLoaderEmptyResult() throws Exception {
        // Given
        Set<String> transactionIds = Set.of("NON-EXISTENT-1", "NON-EXISTENT-2");

        when(exceptionRepository.findByTransactionIdIn(transactionIds))
                .thenReturn(List.of());

        // When
        CompletableFuture<Map<String, InterfaceException>> future = exceptionDataLoader.load(transactionIds)
                .toCompletableFuture();
        Map<String, InterfaceException> result = future.get();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testRetryHistoryDataLoaderBatchLoad() throws Exception {
        // Given
        Set<String> transactionIds = Set.of("TEST-001", "TEST-002", "NON-EXISTENT");
        List<InterfaceException> mockExceptions = List.of(testException1, testException2);
        List<RetryAttempt> mockRetryAttempts = List.of(retryAttempt1, retryAttempt2);

        when(exceptionRepository.findByTransactionIdIn(transactionIds))
                .thenReturn(mockExceptions);
        when(retryAttemptRepository.findByInterfaceExceptionIn(mockExceptions))
                .thenReturn(mockRetryAttempts);

        // When
        CompletableFuture<Map<String, List<RetryAttempt>>> future = retryHistoryDataLoader.load(transactionIds)
                .toCompletableFuture();
        Map<String, List<RetryAttempt>> result = future.get();

        // Then
        assertThat(result).hasSize(3); // All requested IDs should have entries
        assertThat(result.get("TEST-001")).hasSize(2); // testException1 has 2 retry attempts
        assertThat(result.get("TEST-002")).isEmpty(); // testException2 has no retry attempts
        assertThat(result.get("NON-EXISTENT")).isEmpty(); // Non-existent exception

        // Verify sorting by attempt number
        List<RetryAttempt> retries = result.get("TEST-001");
        assertThat(retries.get(0).getAttemptNumber()).isEqualTo(1);
        assertThat(retries.get(1).getAttemptNumber()).isEqualTo(2);
    }

    @Test
    void testRetryHistoryDataLoaderEmptyExceptions() throws Exception {
        // Given
        Set<String> transactionIds = Set.of("NON-EXISTENT-1", "NON-EXISTENT-2");

        when(exceptionRepository.findByTransactionIdIn(transactionIds))
                .thenReturn(List.of());

        // When
        CompletableFuture<Map<String, List<RetryAttempt>>> future = retryHistoryDataLoader.load(transactionIds)
                .toCompletableFuture();
        Map<String, List<RetryAttempt>> result = future.get();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get("NON-EXISTENT-1")).isEmpty();
        assertThat(result.get("NON-EXISTENT-2")).isEmpty();
    }

    @Test
    void testExceptionDataLoaderErrorHandling() {
        // Given
        Set<String> transactionIds = Set.of("TEST-001");

        when(exceptionRepository.findByTransactionIdIn(any()))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        CompletableFuture<Map<String, InterfaceException>> future = exceptionDataLoader.load(transactionIds)
                .toCompletableFuture();

        assertThat(future).failsWithin(java.time.Duration.ofSeconds(1))
                .withThrowableOfType(java.util.concurrent.ExecutionException.class)
                .withMessageContaining("Failed to batch load exceptions");
    }

    @Test
    void testRetryHistoryDataLoaderErrorHandling() {
        // Given
        Set<String> transactionIds = Set.of("TEST-001");

        when(exceptionRepository.findByTransactionIdIn(any()))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        CompletableFuture<Map<String, List<RetryAttempt>>> future = retryHistoryDataLoader.load(transactionIds)
                .toCompletableFuture();

        assertThat(future).failsWithin(java.time.Duration.ofSeconds(1))
                .withThrowableOfType(java.util.concurrent.ExecutionException.class)
                .withMessageContaining("Failed to batch load retry history");
    }
}