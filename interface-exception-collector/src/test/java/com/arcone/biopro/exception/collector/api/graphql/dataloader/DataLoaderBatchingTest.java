package com.arcone.biopro.exception.collector.api.graphql.dataloader;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.api.graphql.config.DataLoaderConfig;
import com.arcone.biopro.exception.collector.application.service.PayloadRetrievalService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.entity.StatusChange;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.StatusChangeRepository;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
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
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

/**
 * Test class for verifying DataLoader batching behavior and cache
 * effectiveness.
 * Tests the DataLoader pattern implementation to ensure N+1 query prevention
 * and proper caching functionality.
 */
@ExtendWith(MockitoExtension.class)
class DataLoaderBatchingTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private PayloadRetrievalService payloadRetrievalService;

    @Mock
    private RetryAttemptRepository retryAttemptRepository;

    @Mock
    private StatusChangeRepository statusChangeRepository;

    private DataLoaderConfig dataLoaderConfig;
    private DataLoaderRegistry registry;

    private ExceptionDataLoader exceptionDataLoader;
    private PayloadDataLoader payloadDataLoader;
    private RetryHistoryDataLoader retryHistoryDataLoader;
    private StatusChangeDataLoader statusChangeDataLoader;

    @BeforeEach
    void setUp() {
        // Create DataLoader instances
        exceptionDataLoader = new ExceptionDataLoader(exceptionRepository);
        payloadDataLoader = new PayloadDataLoader(payloadRetrievalService, exceptionRepository);
        retryHistoryDataLoader = new RetryHistoryDataLoader(retryAttemptRepository, exceptionRepository);
        statusChangeDataLoader = new StatusChangeDataLoader(statusChangeRepository);

        // Create DataLoaderConfig
        dataLoaderConfig = new DataLoaderConfig(
                exceptionDataLoader,
                payloadDataLoader,
                retryHistoryDataLoader,
                statusChangeDataLoader
        );

        // Create registry
        registry = dataLoaderConfig.dataLoaderRegistry();
    }

@Test
    void testExceptionDataLoaderBatching() throws ExecutionException, InterruptedException {
        // Arrange
        Set<String> transactionIds = Set.of("tx1", "tx2", "tx3");
        
        InterfaceException exception1 = createTestException("tx1");
        InterfaceException exception2 = createTestException("tx2");
        InterfaceException exception3 = createTestException("tx3");
        
        List<InterfaceException> exceptions = List.of(exception1, exception2, exception3);
        
        when(exceptionRepository.findByTransactionIdIn(transactionIds))
                .thenReturn(exceptions);

        DataLoader<String, InterfaceException> loader = registry.getDataLoader(DataLoaderConfig.EXCEPTION_LOADER);

        // Act - Load multiple exceptions
        CompletableFuture<InterfaceException> future1 = loader.load("tx1");
        CompletableFuture<InterfaceException> future2 = loader.load("tx2");
        CompletableFuture<InterfaceException> future3 = loader.load("tx3");

        // Dispatch the batch
        loader.dispatch();

        // Wait for results
        InterfaceException result1 = future1.get();
        InterfaceException result2 = future2.get();
        InterfaceException result3 = future3.get();

        // Assert
        assertThat(result1).isNotNull();
        assertThat(result1.getTransactionId()).isEqualTo("tx1");
        assertThat(result2).isNotNull();
        assertThat(result2.getTransactionId()).isEqualTo("tx2");
        assertThat(result3).isNotNull();
        assertThat(result3.getTransactionId()).isEqualTo("tx3");
       // Verify batching - repository should be called only once
        verify(exceptionRepository, times(1)).findByTransactionIdIn(anySet());
    }

    @Test
    void testExceptionDataLoaderCaching() throws ExecutionException, InterruptedException {
        // Arrange
        Set<String> transactionIds = Set.of("tx1");
        InterfaceException exception = createTestException("tx1");
        
        when(exceptionRepository.findByTransactionIdIn(transactionIds))
                .thenReturn(List.of(exception));

        DataLoader<String, InterfaceException> loader = registry.getDataLoader(DataLoaderConfig.EXCEPTION_LOADER);

        // Act - Load the same exception multiple times
        CompletableFuture<InterfaceException> future1 = loader.load("tx1");
        loader.dispatch();
        InterfaceException result1 = future1.get();

        CompletableFuture<InterfaceException> future2 = loader.load("tx1");
        loader.dispatch();
        InterfaceException result2 = future2.get();

        // Assert
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1).isSameAs(result2); // Should be cached

        // Verify caching - repository should be called only once
        verify(exceptionRepository, times(1)).findByTransactionIdIn(anySet());
    }

    @Test
    void testPayloadDataLoaderBatching() throws ExecutionException, InterruptedException {
        // Arrange
        Set<String> transactionIds = Set.of("tx1", "tx2");
        
        InterfaceException exception1 = createTestException("tx1");
        InterfaceException exception2 = createTestException("tx2");
        List<InterfaceException> exceptions = List.of(exception1, exception2);
        
        PayloadResponse payload1 = PayloadResponse.builder()
                .transactionId("tx1")
                .retrieved(true)
                .content("payload1")
                .build();
        
        PayloadResponse payload2 = PayloadResponse.builder()
                .transactionId("tx2")
                .retrieved(true)
                .content("payload2")
                .build();

        when(exceptionRepository.findByTransactionIdIn(transactionIds))
                .thenReturn(exceptions);
        when(payloadRetrievalService.getOriginalPayload(exception1))
                .thenReturn(CompletableFuture.completedFuture(payload1));
        when(payloadRetrievalService.getOriginalPayload(exception2))
                .thenReturn(CompletableFuture.completedFuture(payload2));

        DataLoader<String, PayloadResponse> loader = registry.getDataLoader(DataLoaderConfig.PAYLOAD_LOADER);

        // Act
        CompletableFuture<PayloadResponse> future1 = loader.load("tx1");
        CompletableFuture<PayloadResponse> future2 = loader.load("tx2");

        loader.dispatch();

        PayloadResponse result1 = future1.get();
        PayloadResponse result2 = future2.get();

        // Assert
        assertThat(result1).isNotNull();
        assertThat(result1.getTransactionId()).isEqualTo("tx1");
        assertThat(result1.isRetrieved()).isTrue();
        
        assertThat(result2).isNotNull();
        assertThat(result2.getTransactionId()).isEqualTo("tx2");
        assertThat(result2.isRetrieved()).isTrue();

        // Verify batching - repository should be called only once
        verify(exceptionRepository, times(1)).findByTransactionIdIn(anySet());
    }

    @Test
    void testRetryHistoryDataLoaderBatching() throws ExecutionException, InterruptedException {
        // Arrange
        Set<String> transactionIds = Set.of("tx1", "tx2");
        
        InterfaceException exception1 = createTestException("tx1");
        InterfaceException exception2 = createTestException("tx2");
        List<InterfaceException> exceptions = List.of(exception1, exception2);
        
        RetryAttempt retry1 = createTestRetryAttempt(exception1, 1);
        RetryAttempt retry2 = createTestRetryAttempt(exception2, 1);
        List<RetryAttempt> retryAttempts = List.of(retry1, retry2);

        when(exceptionRepository.findByTransactionIdIn(transactionIds))
                .thenReturn(exceptions);
        when(retryAttemptRepository.findByInterfaceExceptionIn(exceptions))
                .thenReturn(retryAttempts);

        DataLoader<String, List<RetryAttempt>> loader = registry.getDataLoader(DataLoaderConfig.RETRY_HISTORY_LOADER);

        // Act
        CompletableFuture<List<RetryAttempt>> future1 = loader.load("tx1");
        CompletableFuture<List<RetryAttempt>> future2 = loader.load("tx2");

        loader.dispatch();

        List<RetryAttempt> result1 = future1.get();
        List<RetryAttempt> result2 = future2.get();

        // Assert
        assertThat(result1).hasSize(1);
        assertThat(result1.get(0).getInterfaceException().getTransactionId()).isEqualTo("tx1");
        
        assertThat(result2).hasSize(1);
        assertThat(result2.get(0).getInterfaceException().getTransactionId()).isEqualTo("tx2");

        // Verify batching
        verify(exceptionRepository, times(1)).findByTransactionIdIn(anySet());
        verify(retryAttemptRepository, times(1)).findByInterfaceExceptionIn(anyList());
    }

    @Test
    void testStatusChangeDataLoaderBatching() throws ExecutionException, InterruptedException {
        // Arrange
        Set<String> transactionIds = Set.of("tx1", "tx2");
        
        InterfaceException exception1 = createTestException("tx1");
        InterfaceException exception2 = createTestException("tx2");
        
        StatusChange statusChange1 = createTestStatusChange(exception1);
        StatusChange statusChange2 = createTestStatusChange(exception2);
        List<StatusChange> statusChanges = List.of(statusChange1, statusChange2);

        when(statusChangeRepository.findByInterfaceExceptionTransactionIdInOrderByChangedAtDesc(transactionIds))
                .thenReturn(statusChanges);

        DataLoader<String, List<StatusChange>> loader = registry.getDataLoader(DataLoaderConfig.STATUS_CHANGE_LOADER);

        // Act
        CompletableFuture<List<StatusChange>> future1 = loader.load("tx1");
        CompletableFuture<List<StatusChange>> future2 = loader.load("tx2");

        loader.dispatch();

        List<StatusChange> result1 = future1.get();
        List<StatusChange> result2 = future2.get();

        // Assert
        assertThat(result1).hasSize(1);
        assertThat(result1.get(0).getInterfaceException().getTransactionId()).isEqualTo("tx1");
        
        assertThat(result2).hasSize(1);
        assertThat(result2.get(0).getInterfaceException().getTransactionId()).isEqualTo("tx2");

        // Verify batching
        verify(statusChangeRepository, times(1))
                .findByInterfaceExceptionTransactionIdInOrderByChangedAtDesc(anySet());
    }

    @Test
    void testDataLoaderErrorHandling() throws ExecutionException, InterruptedException {
        // Arrange
        Set<String> transactionIds = Set.of("tx1");
        
        when(exceptionRepository.findByTransactionIdIn(transactionIds))
                .thenThrow(new RuntimeException("Database error"));

        DataLoader<String, InterfaceException> loader = registry.getDataLoader(DataLoaderConfig.EXCEPTION_LOADER);

        // Act
        CompletableFuture<InterfaceException> future = loader.load("tx1");
        loader.dispatch();

        InterfaceException result = future.get();

        // Assert - Should return null for missing data instead of throwing
        assertThat(result).isNull();

        // Verify error was handled gracefully
        verify(exceptionRepository, times(1)).findByTransactionIdIn(anySet());
    }

    @Test
    void testDataLoaderRegistryConfiguration() {
        // Assert that all expected DataLoaders are registered
        assertThat(registry.getDataLoader(DataLoaderConfig.EXCEPTION_LOADER)).isNotNull();
        assertThat(registry.getDataLoader(DataLoaderConfig.PAYLOAD_LOADER)).isNotNull();
        assertThat(registry.getDataLoader(DataLoaderConfig.RETRY_HISTORY_LOADER)).isNotNull();
        assertThat(registry.getDataLoader(DataLoaderConfig.STATUS_CHANGE_LOADER)).isNotNull();

        // Verify registry contains all expected keys
        assertThat(registry.getKeys()).containsExactlyInAnyOrder(
                DataLoaderConfig.EXCEPTION_LOADER,
                DataLoaderConfig.PAYLOAD_LOADER,
                DataLoaderConfig.RETRY_HISTORY_LOADER,
                DataLoaderConfig.STATUS_CHANGE_LOADER
        );
    }

    @Test
    void testRequestScopedDataLoaderRegistry() {
        // Act - Create multiple registry instances
        DataLoaderRegistry registry1 = dataLoaderConfig.requestScopedDataLoaderRegistry();
        DataLoaderRegistry registry2 = dataLoaderConfig.requestScopedDataLoaderRegistry();

        // Assert - Each request should get a fresh registry
        assertThat(registry1).isNotSameAs(registry2);
        
        // But they should have the same structure
        assertThat(registry1.getKeys()).isEqualTo(registry2.getKeys());
    }

    // Helper methods for creating test data

    private InterfaceException createTestException(String transactionId) {
        InterfaceException exception = new InterfaceException();
        exception.setTransactionId(transactionId);
        exception.setInterfaceType(InterfaceType.ORDER);
        exception.setStatus(ExceptionStatus.CAPTURED);
        exception.setExceptionReason("Test exception");
        exception.setOperation("TEST_OPERATION");
        exception.setTimestamp(OffsetDateTime.now());
        exception.setProcessedAt(OffsetDateTime.now());
        return exception;
    }

    private RetryAttempt createTestRetryAttempt(InterfaceException exception, int attemptNumber) {
        RetryAttempt retry = new RetryAttempt();
        retry.setInterfaceException(exception);
        retry.setAttemptNumber(attemptNumber);
        retry.setInitiatedBy("test-user");
        retry.setInitiatedAt(OffsetDateTime.now());
        return retry;
    }

    private StatusChange createTestStatusChange(InterfaceException exception) {
        StatusChange statusChange = new StatusChange();
        statusChange.setInterfaceException(exception);
        statusChange.setFromStatus(ExceptionStatus.CAPTURED);
        statusChange.setToStatus(ExceptionStatus.ACKNOWLEDGED);
        statusChange.setChangedBy("test-user");
        statusChange.setChangedAt(OffsetDateTime.now());
        return statusChange;
    }
}