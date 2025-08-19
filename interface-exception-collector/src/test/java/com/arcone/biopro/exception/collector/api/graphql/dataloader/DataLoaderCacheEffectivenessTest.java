package com.arcone.biopro.exception.collector.api.graphql.dataloader;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for DataLoader caching effectiveness and batching behavior.
 * Validates that DataLoaders properly batch requests and cache results.
 */
@ExtendWith(MockitoExtension.class)
class DataLoaderCacheEffectivenessTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private RetryAttemptRepository retryAttemptRepository;

    private ExceptionDataLoader exceptionDataLoader;
    private RetryHistoryDataLoader retryHistoryDataLoader;
    private DataLoader<String, InterfaceException> exceptionLoader;
    private DataLoader<String, List<RetryAttempt>> retryHistoryLoader;

    private InterfaceException testException1;
    private InterfaceException testException2;
    private RetryAttempt retryAttempt1;
    private RetryAttempt retryAttempt2;

    @BeforeEach
    void setUp() {
        exceptionDataLoader = new ExceptionDataLoader(exceptionRepository);
        retryHistoryDataLoader = new RetryHistoryDataLoader(retryAttemptRepository, exceptionRepository);

        // Create DataLoaders with caching enabled
        DataLoaderOptions options = DataLoaderOptions.newOptions()
                .setCachingEnabled(true)
                .setBatchingEnabled(true)
                .build();

        exceptionLoader = DataLoader.newDataLoader(exceptionDataLoader, options);
        retryHistoryLoader = DataLoader.newDataLoader(retryHistoryDataLoader, options);

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
    void testExceptionDataLoaderBatching() throws Exception {
        // Given
        List<InterfaceException> mockExceptions = List.of(testException1, testException2);
        when(exceptionRepository.findByTransactionIdIn(any()))
                .thenReturn(mockExceptions);

        // When - Load multiple exceptions in separate calls
        CompletableFuture<InterfaceException> future1 = exceptionLoader.load("TEST-001");
        CompletableFuture<InterfaceException> future2 = exceptionLoader.load("TEST-002");
        CompletableFuture<InterfaceException> future3 = exceptionLoader.load("TEST-003");

        // Dispatch the batch
        exceptionLoader.dispatch();

        // Then - All futures should complete
        assertThat(future1.get().getTransactionId()).isEqualTo("TEST-001");
        assertThat(future2.get().getTransactionId()).isEqualTo("TEST-002");
        assertThat(future3.get()).isNull(); // Non-existent

        // Verify repository was called only once (batched)
        verify(exceptionRepository, times(1)).findByTransactionIdIn(any());
    }

    @Test
    void testExceptionDataLoaderCaching() throws Exception {
        // Given
        List<InterfaceException> mockExceptions = List.of(testException1);
        when(exceptionRepository.findByTransactionIdIn(any()))
                .thenReturn(mockExceptions);

        // When - Load same exception multiple times
        CompletableFuture<InterfaceException> future1 = exceptionLoader.load("TEST-001");
        exceptionLoader.dispatch();
        
        CompletableFuture<InterfaceException> future2 = exceptionLoader.load("TEST-001");
        CompletableFuture<InterfaceException> future3 = exceptionLoader.load("TEST-001");
        exceptionLoader.dispatch();

        // Then - All should return same result
        assertThat(future1.get().getTransactionId()).isEqualTo("TEST-001");
        assertThat(future2.get().getTransactionId()).isEqualTo("TEST-001");
        assertThat(future3.get().getTransactionId()).isEqualTo("TEST-001");

        // Verify repository was called only once (cached)
        verify(exceptionRepository, times(1)).findByTransactionIdIn(any());
    }

    @Test
    void testRetryHistoryDataLoaderBatching() throws Exception {
        // Given
        List<InterfaceException> mockExceptions = List.of(testException1, testException2);
        List<RetryAttempt> mockRetryAttempts = List.of(retryAttempt1, retryAttempt2);

        when(exceptionRepository.findByTransactionIdIn(any()))
                .thenReturn(mockExceptions);
        when(retryAttemptRepository.findByInterfaceExceptionIn(mockExceptions))
                .thenReturn(mockRetryAttempts);

        // When - Load retry history for multiple exceptions
        CompletableFuture<List<RetryAttempt>> future1 = retryHistoryLoader.load("TEST-001");
        CompletableFuture<List<RetryAttempt>> future2 = retryHistoryLoader.load("TEST-002");
        CompletableFuture<List<RetryAttempt>> future3 = retryHistoryLoader.load("TEST-003");

        // Dispatch the batch
        retryHistoryLoader.dispatch();

        // Then - All futures should complete
        assertThat(future1.get()).hasSize(2); // testException1 has 2 retry attempts
        assertThat(future2.get()).isEmpty(); // testException2 has no retry attempts
        assertThat(future3.get()).isEmpty(); // Non-existent exception

        // Verify repositories were called only once each (batched)
        verify(exceptionRepository, times(1)).findByTransactionIdIn(any());
        verify(retryAttemptRepository, times(1)).findByInterfaceExceptionIn(any());
    }

    @Test
    void testRetryHistoryDataLoaderCaching() throws Exception {
        // Given
        List<InterfaceException> mockExceptions = List.of(testException1);
        List<RetryAttempt> mockRetryAttempts = List.of(retryAttempt1, retryAttempt2);

        when(exceptionRepository.findByTransactionIdIn(any()))
                .thenReturn(mockExceptions);
        when(retryAttemptRepository.findByInterfaceExceptionIn(mockExceptions))
                .thenReturn(mockRetryAttempts);

        // When - Load same retry history multiple times
        CompletableFuture<List<RetryAttempt>> future1 = retryHistoryLoader.load("TEST-001");
        retryHistoryLoader.dispatch();
        
        CompletableFuture<List<RetryAttempt>> future2 = retryHistoryLoader.load("TEST-001");
        CompletableFuture<List<RetryAttempt>> future3 = retryHistoryLoader.load("TEST-001");
        retryHistoryLoader.dispatch();

        // Then - All should return same result
        assertThat(future1.get()).hasSize(2);
        assertThat(future2.get()).hasSize(2);
        assertThat(future3.get()).hasSize(2);

        // Verify repositories were called only once each (cached)
        verify(exceptionRepository, times(1)).findByTransactionIdIn(any());
        verify(retryAttemptRepository, times(1)).findByInterfaceExceptionIn(any());
    }

    @Test
    void testDataLoaderCacheStatistics() throws Exception {
        // Given
        List<InterfaceException> mockExceptions = List.of(testException1);
        when(exceptionRepository.findByTransactionIdIn(any()))
                .thenReturn(mockExceptions);

        // When - Load same exception multiple times to test cache hit rate
        for (int i = 0; i < 10; i++) {
            exceptionLoader.load("TEST-001");
        }
        exceptionLoader.dispatch();

        // Load different exceptions to test cache miss
        for (int i = 0; i < 5; i++) {
            exceptionLoader.load("TEST-00" + (i + 2));
        }
        exceptionLoader.dispatch();

        // Then - Verify cache statistics
        var statistics = exceptionLoader.getStatistics();
        assertThat(statistics.getBatchLoadCount()).isGreaterThan(0);
        assertThat(statistics.getCacheHitCount()).isGreaterThan(0);
        assertThat(statistics.getLoadCount()).isEqualTo(15); // 10 + 5 loads
    }

    @Test
    void testDataLoaderWithLargeDataset() throws Exception {
        // Given - Large dataset simulation
        List<InterfaceException> largeDataset = generateLargeExceptionDataset(1000);
        when(exceptionRepository.findByTransactionIdIn(any()))
                .thenReturn(largeDataset);

        // When - Load many exceptions in batches
        List<CompletableFuture<InterfaceException>> futures = new java.util.ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            futures.add(exceptionLoader.load("TEST-" + String.format("%03d", i)));
        }
        exceptionLoader.dispatch();

        // Then - All futures should complete successfully
        for (CompletableFuture<InterfaceException> future : futures) {
            assertThat(future).succeedsWithin(Duration.ofSeconds(5));
        }

        // Verify batching efficiency
        var statistics = exceptionLoader.getStatistics();
        assertThat(statistics.getBatchLoadCount()).isLessThan(100); // Should batch efficiently
    }

    @Test
    void testDataLoaderErrorHandling() throws Exception {
        // Given
        when(exceptionRepository.findByTransactionIdIn(any()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When
        CompletableFuture<InterfaceException> future = exceptionLoader.load("TEST-001");
        exceptionLoader.dispatch();

        // Then - Future should complete exceptionally
        assertThat(future).failsWithin(Duration.ofSeconds(1))
                .withThrowableOfType(java.util.concurrent.ExecutionException.class);
    }

    @Test
    void testDataLoaderClearCache() throws Exception {
        // Given
        List<InterfaceException> mockExceptions = List.of(testException1);
        when(exceptionRepository.findByTransactionIdIn(any()))
                .thenReturn(mockExceptions);

        // When - Load, clear cache, then load again
        CompletableFuture<InterfaceException> future1 = exceptionLoader.load("TEST-001");
        exceptionLoader.dispatch();
        
        exceptionLoader.clear("TEST-001"); // Clear specific cache entry
        
        CompletableFuture<InterfaceException> future2 = exceptionLoader.load("TEST-001");
        exceptionLoader.dispatch();

        // Then - Both should succeed but repository should be called twice
        assertThat(future1.get().getTransactionId()).isEqualTo("TEST-001");
        assertThat(future2.get().getTransactionId()).isEqualTo("TEST-001");
        
        verify(exceptionRepository, times(2)).findByTransactionIdIn(any());
    }

    private List<InterfaceException> generateLargeExceptionDataset(int size) {
        List<InterfaceException> exceptions = new java.util.ArrayList<>();
        for (int i = 0; i < size; i++) {
            exceptions.add(InterfaceException.builder()
                    .transactionId("TEST-" + String.format("%03d", i))
                    .interfaceType(InterfaceType.ORDER)
                    .exceptionReason("Test exception " + i)
                    .operation("CREATE_ORDER")
                    .status(ExceptionStatus.NEW)
                    .severity(ExceptionSeverity.MEDIUM)
                    .timestamp(OffsetDateTime.now())
                    .processedAt(OffsetDateTime.now())
                    .build());
        }
        return exceptions;
    }
}