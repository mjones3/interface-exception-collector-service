package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.api.graphql.config.DataLoaderConfig;
import com.arcone.biopro.exception.collector.api.graphql.util.DataLoaderUtil;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.entity.StatusChange;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test for DataLoader usage in GraphQL resolvers.
 * Verifies that resolvers properly use DataLoaders to prevent N+1 queries.
 */
@ExtendWith(MockitoExtension.class)
class DataLoaderResolverIntegrationTest {

    @Mock
    private DataFetchingEnvironment environment;

    @Mock
    private DataLoader<String, InterfaceException> exceptionLoader;

    @Mock
    private DataLoader<String, PayloadResponse> payloadLoader;

    @Mock
    private DataLoader<String, List<RetryAttempt>> retryHistoryLoader;

    @Mock
    private DataLoader<String, List<StatusChange>> statusChangeLoader;

    private DataLoaderRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DataLoaderRegistry();
        registry.register(DataLoaderConfig.EXCEPTION_LOADER, exceptionLoader);
        registry.register(DataLoaderConfig.PAYLOAD_LOADER, payloadLoader);
        registry.register(DataLoaderConfig.RETRY_HISTORY_LOADER, retryHistoryLoader);
        registry.register(DataLoaderConfig.STATUS_CHANGE_LOADER, statusChangeLoader);

        // Mock the environment to return our DataLoaders
        when(environment.getDataLoader(DataLoaderConfig.EXCEPTION_LOADER)).thenReturn(exceptionLoader);
        when(environment.getDataLoader(DataLoaderConfig.PAYLOAD_LOADER)).thenReturn(payloadLoader);
        when(environment.getDataLoader(DataLoaderConfig.RETRY_HISTORY_LOADER)).thenReturn(retryHistoryLoader);
        when(environment.getDataLoader(DataLoaderConfig.STATUS_CHANGE_LOADER)).thenReturn(statusChangeLoader);
    }

    @Test
    void shouldLoadExceptionUsingDataLoader() throws ExecutionException, InterruptedException {
        // Given
        String transactionId = "test-tx-123";
        InterfaceException mockException = createMockException(transactionId);
        
        when(exceptionLoader.load(transactionId))
                .thenReturn(CompletableFuture.completedFuture(mockException));

        // When
        CompletableFuture<InterfaceException> result = DataLoaderUtil.loadException(environment, transactionId);

        // Then
        InterfaceException exception = result.get();
        assertThat(exception).isNotNull();
        assertThat(exception.getTransactionId()).isEqualTo(transactionId);
    }

    @Test
    void shouldLoadPayloadUsingDataLoader() throws ExecutionException, InterruptedException {
        // Given
        String transactionId = "test-tx-456";
        PayloadResponse mockPayload = PayloadResponse.builder()
                .transactionId(transactionId)
                .retrieved(true)
                .originalPayload("{\"test\": \"data\"}")
                .build();
        
        when(payloadLoader.load(transactionId))
                .thenReturn(CompletableFuture.completedFuture(mockPayload));

        // When
        CompletableFuture<PayloadResponse> result = DataLoaderUtil.loadPayload(environment, transactionId);

        // Then
        PayloadResponse payload = result.get();
        assertThat(payload).isNotNull();
        assertThat(payload.getTransactionId()).isEqualTo(transactionId);
        assertThat(payload.isRetrieved()).isTrue();
        assertThat(payload.getOriginalPayload()).isEqualTo("{\"test\": \"data\"}");
    }

    @Test
    void shouldLoadRetryHistoryUsingDataLoader() throws ExecutionException, InterruptedException {
        // Given
        String transactionId = "test-tx-789";
        List<RetryAttempt> mockRetryHistory = List.of(
                createMockRetryAttempt(transactionId, 1),
                createMockRetryAttempt(transactionId, 2)
        );
        
        when(retryHistoryLoader.load(transactionId))
                .thenReturn(CompletableFuture.completedFuture(mockRetryHistory));

        // When
        CompletableFuture<List<RetryAttempt>> result = DataLoaderUtil.loadRetryHistory(environment, transactionId);

        // Then
        List<RetryAttempt> retryHistory = result.get();
        assertThat(retryHistory).hasSize(2);
        assertThat(retryHistory.get(0).getAttemptNumber()).isEqualTo(1);
        assertThat(retryHistory.get(1).getAttemptNumber()).isEqualTo(2);
    }

    @Test
    void shouldLoadStatusHistoryUsingDataLoader() throws ExecutionException, InterruptedException {
        // Given
        String transactionId = "test-tx-101";
        List<StatusChange> mockStatusHistory = List.of(
                createMockStatusChange(transactionId, "PENDING", "PROCESSING"),
                createMockStatusChange(transactionId, "PROCESSING", "COMPLETED")
        );
        
        when(statusChangeLoader.load(transactionId))
                .thenReturn(CompletableFuture.completedFuture(mockStatusHistory));

        // When
        CompletableFuture<List<StatusChange>> result = DataLoaderUtil.loadStatusHistory(environment, transactionId);

        // Then
        List<StatusChange> statusHistory = result.get();
        assertThat(statusHistory).hasSize(2);
        assertThat(statusHistory.get(0).getOldStatus()).isEqualTo("PENDING");
        assertThat(statusHistory.get(0).getNewStatus()).isEqualTo("PROCESSING");
        assertThat(statusHistory.get(1).getOldStatus()).isEqualTo("PROCESSING");
        assertThat(statusHistory.get(1).getNewStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void shouldHandleNullTransactionIdGracefully() throws ExecutionException, InterruptedException {
        // When/Then - These should not throw exceptions
        CompletableFuture<InterfaceException> exceptionResult = DataLoaderUtil.loadException(environment, null);
        CompletableFuture<PayloadResponse> payloadResult = DataLoaderUtil.loadPayload(environment, null);
        CompletableFuture<List<RetryAttempt>> retryResult = DataLoaderUtil.loadRetryHistory(environment, null);
        CompletableFuture<List<StatusChange>> statusResult = DataLoaderUtil.loadStatusHistory(environment, null);

        // Verify that appropriate default values are returned
        assertThat(exceptionResult.get()).isNull();
        
        PayloadResponse payload = payloadResult.get();
        assertThat(payload.isRetrieved()).isFalse();
        assertThat(payload.getErrorMessage()).contains("Invalid transaction ID");
        
        assertThat(retryResult.get()).isEmpty();
        assertThat(statusResult.get()).isEmpty();
    }

    @Test
    void shouldHandleEmptyTransactionIdGracefully() throws ExecutionException, InterruptedException {
        // When/Then - These should not throw exceptions
        CompletableFuture<InterfaceException> exceptionResult = DataLoaderUtil.loadException(environment, "");
        CompletableFuture<PayloadResponse> payloadResult = DataLoaderUtil.loadPayload(environment, "");
        CompletableFuture<List<RetryAttempt>> retryResult = DataLoaderUtil.loadRetryHistory(environment, "");
        CompletableFuture<List<StatusChange>> statusResult = DataLoaderUtil.loadStatusHistory(environment, "");

        // Verify that appropriate default values are returned
        assertThat(exceptionResult.get()).isNull();
        
        PayloadResponse payload = payloadResult.get();
        assertThat(payload.isRetrieved()).isFalse();
        assertThat(payload.getErrorMessage()).contains("Invalid transaction ID");
        
        assertThat(retryResult.get()).isEmpty();
        assertThat(statusResult.get()).isEmpty();
    }

    @Test
    void shouldDetectDataLoaderAvailability() {
        // When
        boolean available = DataLoaderUtil.areDataLoadersAvailable(environment);

        // Then
        assertThat(available).isTrue();
    }

    @Test
    void shouldDetectMissingDataLoaders() {
        // Given - Mock environment with missing DataLoaders
        DataFetchingEnvironment emptyEnvironment = mock(DataFetchingEnvironment.class);
        when(emptyEnvironment.getDataLoader(anyString())).thenReturn(null);

        // When
        boolean available = DataLoaderUtil.areDataLoadersAvailable(emptyEnvironment);

        // Then
        assertThat(available).isFalse();
    }

    @Test
    void shouldHandleDataLoaderFailures() {
        // Given
        String transactionId = "failing-tx";
        CompletableFuture<InterfaceException> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("DataLoader failure"));
        
        when(exceptionLoader.load(transactionId)).thenReturn(failedFuture);

        // When
        CompletableFuture<InterfaceException> result = DataLoaderUtil.loadException(environment, transactionId);

        // Then
        assertThat(result).isCompletedExceptionally();
    }

    @Test
    void shouldLogDataLoaderStats() {
        // This test verifies that the logging method doesn't throw exceptions
        // When
        DataLoaderUtil.logDataLoaderStats(environment);

        // Then - No exception should be thrown
        // The actual logging is tested through log output verification in integration tests
    }

    private InterfaceException createMockException(String transactionId) {
        InterfaceException exception = new InterfaceException();
        exception.setTransactionId(transactionId);
        exception.setInterfaceType("TEST");
        exception.setErrorMessage("Test error message");
        exception.setCreatedAt(LocalDateTime.now());
        return exception;
    }

    private RetryAttempt createMockRetryAttempt(String transactionId, int attemptNumber) {
        RetryAttempt retry = new RetryAttempt();
        InterfaceException exception = createMockException(transactionId);
        retry.setInterfaceException(exception);
        retry.setAttemptNumber(attemptNumber);
        retry.setAttemptedAt(LocalDateTime.now());
        retry.setResultSuccess(attemptNumber > 1); // First attempt fails, subsequent succeed
        return retry;
    }

    private StatusChange createMockStatusChange(String transactionId, String oldStatus, String newStatus) {
        StatusChange statusChange = new StatusChange();
        InterfaceException exception = createMockException(transactionId);
        statusChange.setInterfaceException(exception);
        statusChange.setOldStatus(oldStatus);
        statusChange.setNewStatus(newStatus);
        statusChange.setChangedAt(LocalDateTime.now());
        return statusChange;
    }
}