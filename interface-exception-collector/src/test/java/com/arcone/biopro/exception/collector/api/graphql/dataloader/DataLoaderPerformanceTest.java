package com.arcone.biopro.exception.collector.api.graphql.dataloader;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.entity.StatusChange;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.StatusChangeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Performance test for DataLoader implementations to verify N+1 query
 * prevention
 * and batching effectiveness.
 */
@ExtendWith(MockitoExtension.class)
class DataLoaderPerformanceTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private RetryAttemptRepository retryAttemptRepository;

    @Mock
    private StatusChangeRepository statusChangeRepository;

    @Test
    void shouldBatchExceptionQueries() throws ExecutionException, InterruptedException {
        // Given
        ExceptionDataLoader dataLoader = new ExceptionDataLoader(exceptionRepository);

        Set<String> transactionIds = Set.of("tx1", "tx2", "tx3", "tx4", "tx5");

        List<InterfaceException> mockExceptions = transactionIds.stream()
                .map(this::createMockException)
                .toList();

        when(exceptionRepository.findByTransactionIdIn(transactionIds))
                .thenReturn(mockExceptions);

        // When
        CompletableFuture<java.util.Map<String, InterfaceException>> result = dataLoader.load(transactionIds);

        // Then
        java.util.Map<String, InterfaceException> exceptions = result.get();

        assertThat(exceptions).hasSize(5);
        assertThat(exceptions.keySet()).containsExactlyInAnyOrderElementsOf(transactionIds);

        // Verify that repository was called only once (batching)
        verify(exceptionRepository, times(1)).findByTransactionIdIn(transactionIds);
    }

    @Test
    void shouldBatchRetryHistoryQueries() throws ExecutionException, InterruptedException {
        // Given
        RetryHistoryDataLoader dataLoader = new RetryHistoryDataLoader(
                retryAttemptRepository, exceptionRepository);

        Set<String> transactionIds = Set.of("tx1", "tx2", "tx3");

        List<InterfaceException> mockExceptions = transactionIds.stream()
                .map(this::createMockException)
                .toList();

        List<RetryAttempt> mockRetryAttempts = List.of(
                createMockRetryAttempt(mockExceptions.get(0), 1),
                createMockRetryAttempt(mockExceptions.get(0), 2),
                createMockRetryAttempt(mockExceptions.get(1), 1));

        when(exceptionRepository.findByTransactionIdIn(transactionIds))
                .thenReturn(mockExceptions);
        when(retryAttemptRepository.findByInterfaceExceptionIn(mockExceptions))
                .thenReturn(mockRetryAttempts);

        // When
        CompletableFuture<java.util.Map<String, List<RetryAttempt>>> result = dataLoader.load(transactionIds);

        // Then
        java.util.Map<String, List<RetryAttempt>> retryHistory = result.get();

        assertThat(retryHistory).hasSize(3);
        assertThat(retryHistory.get("tx1")).hasSize(2);
        assertThat(retryHistory.get("tx2")).hasSize(1);
        assertThat(retryHistory.get("tx3")).isEmpty();

        // Verify batching - only one call to each repository
        verify(exceptionRepository, times(1)).findByTransactionIdIn(transactionIds);
        verify(retryAttemptRepository, times(1)).findByInterfaceExceptionIn(mockExceptions);
    }

    @Test
    void shouldBatchStatusChangeQueries() throws ExecutionException, InterruptedException {
        // Given
        StatusChangeDataLoader dataLoader = new StatusChangeDataLoader(statusChangeRepository);

        Set<String> transactionIds = Set.of("tx1", "tx2");

        List<StatusChange> mockStatusChanges = List.of(
                createMockStatusChange("tx1", "PENDING"),
                createMockStatusChange("tx1", "PROCESSING"),
                createMockStatusChange("tx2", "PENDING"));

        when(statusChangeRepository.findByInterfaceExceptionTransactionIdInOrderByChangedAtDesc(transactionIds))
                .thenReturn(mockStatusChanges);

        // When
        CompletableFuture<java.util.Map<String, List<StatusChange>>> result = dataLoader.load(transactionIds);

        // Then
        java.util.Map<String, List<StatusChange>> statusHistory = result.get();

        assertThat(statusHistory).hasSize(2);
        assertThat(statusHistory.get("tx1")).hasSize(2);
        assertThat(statusHistory.get("tx2")).hasSize(1);

        // Verify batching - only one repository call
        verify(statusChangeRepository, times(1))
                .findByInterfaceExceptionTransactionIdInOrderByChangedAtDesc(transactionIds);
    }

    @Test
    void shouldHandleEmptyResultsGracefully() throws ExecutionException, InterruptedException {
        // Given
        ExceptionDataLoader dataLoader = new ExceptionDataLoader(exceptionRepository);
        Set<String> transactionIds = Set.of("nonexistent1", "nonexistent2");

        when(exceptionRepository.findByTransactionIdIn(transactionIds))
                .thenReturn(List.of());

        // When
        CompletableFuture<java.util.Map<String, InterfaceException>> result = dataLoader.load(transactionIds);

        // Then
        java.util.Map<String, InterfaceException> exceptions = result.get();

        assertThat(exceptions).isEmpty();
        verify(exceptionRepository, times(1)).findByTransactionIdIn(transactionIds);
    }

    @Test
    void shouldHandleLargeBatchSizes() throws ExecutionException, InterruptedException {
        // Given
        ExceptionDataLoader dataLoader = new ExceptionDataLoader(exceptionRepository);

        // Create a large set of transaction IDs to test batch size handling
        Set<String> transactionIds = java.util.stream.IntStream.range(1, 501)
                .mapToObj(i -> "tx" + i)
                .collect(java.util.stream.Collectors.toSet());

        List<InterfaceException> mockExceptions = transactionIds.stream()
                .map(this::createMockException)
                .toList();

        when(exceptionRepository.findByTransactionIdIn(transactionIds))
                .thenReturn(mockExceptions);

        // When
        long startTime = System.currentTimeMillis();
        CompletableFuture<java.util.Map<String, InterfaceException>> result = dataLoader.load(transactionIds);
        java.util.Map<String, InterfaceException> exceptions = result.get();
        long endTime = System.currentTimeMillis();

        // Then
        assertThat(exceptions).hasSize(500);
        assertThat(endTime - startTime).isLessThan(1000); // Should complete within 1 second

        // Verify that repository was called only once despite large batch size
        verify(exceptionRepository, times(1)).findByTransactionIdIn(transactionIds);
    }

    @Test
    void shouldMaintainDataIntegrityInBatching() throws ExecutionException, InterruptedException {
        // Given
        RetryHistoryDataLoader dataLoader = new RetryHistoryDataLoader(
                retryAttemptRepository, exceptionRepository);

        Set<String> transactionIds = Set.of("tx1", "tx2");

        InterfaceException exception1 = createMockException("tx1");
        InterfaceException exception2 = createMockException("tx2");
        List<InterfaceException> mockExceptions = List.of(exception1, exception2);

        // Create retry attempts with specific ordering
        List<RetryAttempt> mockRetryAttempts = List.of(
                createMockRetryAttempt(exception1, 3), // Third attempt
                createMockRetryAttempt(exception1, 1), // First attempt
                createMockRetryAttempt(exception1, 2), // Second attempt
                createMockRetryAttempt(exception2, 1) // First attempt for tx2
        );

        when(exceptionRepository.findByTransactionIdIn(transactionIds))
                .thenReturn(mockExceptions);
        when(retryAttemptRepository.findByInterfaceExceptionIn(mockExceptions))
                .thenReturn(mockRetryAttempts);

        // When
        CompletableFuture<java.util.Map<String, List<RetryAttempt>>> result = dataLoader.load(transactionIds);

        // Then
        java.util.Map<String, List<RetryAttempt>> retryHistory = result.get();

        // Verify that retry attempts are properly sorted by attempt number
        List<RetryAttempt> tx1Retries = retryHistory.get("tx1");
        assertThat(tx1Retries).hasSize(3);
        assertThat(tx1Retries.get(0).getAttemptNumber()).isEqualTo(1);
        assertThat(tx1Retries.get(1).getAttemptNumber()).isEqualTo(2);
        assertThat(tx1Retries.get(2).getAttemptNumber()).isEqualTo(3);

        List<RetryAttempt> tx2Retries = retryHistory.get("tx2");
        assertThat(tx2Retries).hasSize(1);
        assertThat(tx2Retries.get(0).getAttemptNumber()).isEqualTo(1);
    }

    private InterfaceException createMockException(String transactionId) {
        InterfaceException exception = new InterfaceException();
        exception.setTransactionId(transactionId);
        exception.setInterfaceType(InterfaceType.ORDER);
        exception.setErrorMessage("Test error");
        exception.setCreatedAt(OffsetDateTime.now());
        exception.setTimestamp(OffsetDateTime.now());
        return exception;
    }

    private RetryAttempt createMockRetryAttempt(InterfaceException exception, int attemptNumber) {
        RetryAttempt retry = new RetryAttempt();
        retry.setInterfaceException(exception);
        retry.setAttemptNumber(attemptNumber);
        retry.setInitiatedAt(OffsetDateTime.now());
        retry.setResultSuccess(attemptNumber % 2 == 0); // Even attempts succeed
        return retry;
    }

    private StatusChange createMockStatusChange(String transactionId, String status) {
        StatusChange statusChange = new StatusChange();
        InterfaceException exception = createMockException(transactionId);
        statusChange.setInterfaceException(exception);
        statusChange.setOldStatus("PENDING");
        statusChange.setNewStatus(status);
        statusChange.setChangedAt(OffsetDateTime.now());
        statusChange.setChangedBy("test-user");
        return statusChange;
    }
}
