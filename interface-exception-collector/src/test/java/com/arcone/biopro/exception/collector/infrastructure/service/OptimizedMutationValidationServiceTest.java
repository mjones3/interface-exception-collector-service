package com.arcone.biopro.exception.collector.infrastructure.service;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.infrastructure.config.MutationQueryTimeoutConfig;
import com.arcone.biopro.exception.collector.infrastructure.repository.OptimizedExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test class for OptimizedMutationValidationService.
 * Tests the optimized validation logic for mutation operations.
 */
@ExtendWith(MockitoExtension.class)
class OptimizedMutationValidationServiceTest {

    @Mock
    private OptimizedExceptionRepository optimizedRepository;

    @Mock
    private TransactionTemplate mutationValidationTransactionTemplate;

    @Mock
    private MutationQueryTimeoutConfig timeoutConfig;

    private OptimizedMutationValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new OptimizedMutationValidationService(
            optimizedRepository, mutationValidationTransactionTemplate, timeoutConfig);
    }

    @Test
    void validateRetryOperation_ShouldReturnSuccess_WhenValidForRetry() {
        // Given
        String transactionId = "TXN-001";
        Object[] validationInfo = {1L, ExceptionStatus.NEW, true, 1, 3};
        
        when(mutationValidationTransactionTemplate.execute(any())).thenAnswer(invocation -> {
            when(optimizedRepository.getValidationInfo(transactionId)).thenReturn(validationInfo);
            when(optimizedRepository.countPendingRetries(transactionId)).thenReturn(0L);
            return invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null);
        });

        // When
        OptimizedMutationValidationService.MutationValidationResult result = 
            validationService.validateRetryOperation(transactionId);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Retry operation is valid");
        assertThat(result.getErrorCode()).isNull();
    }

    @Test
    void validateRetryOperation_ShouldReturnFailure_WhenExceptionNotFound() {
        // Given
        String transactionId = "TXN-999";
        
        when(mutationValidationTransactionTemplate.execute(any())).thenAnswer(invocation -> {
            when(optimizedRepository.getValidationInfo(transactionId)).thenReturn(null);
            return invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null);
        });

        // When
        OptimizedMutationValidationService.MutationValidationResult result = 
            validationService.validateRetryOperation(transactionId);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Exception not found");
        assertThat(result.getErrorCode()).isEqualTo("EXCEPTION_NOT_FOUND");
    }

    @Test
    void validateRetryOperation_ShouldReturnFailure_WhenNotRetryable() {
        // Given
        String transactionId = "TXN-002";
        Object[] validationInfo = {2L, ExceptionStatus.NEW, false, 0, 3}; // retryable = false
        
        when(mutationValidationTransactionTemplate.execute(any())).thenAnswer(invocation -> {
            when(optimizedRepository.getValidationInfo(transactionId)).thenReturn(validationInfo);
            return invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null);
        });

        // When
        OptimizedMutationValidationService.MutationValidationResult result = 
            validationService.validateRetryOperation(transactionId);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Exception is not retryable");
        assertThat(result.getErrorCode()).isEqualTo("NOT_RETRYABLE");
    }

    @Test
    void validateRetryOperation_ShouldReturnFailure_WhenStatusNotRetryable() {
        // Given
        String transactionId = "TXN-003";
        Object[] validationInfo = {3L, ExceptionStatus.RESOLVED, true, 0, 3}; // status = RESOLVED
        
        when(mutationValidationTransactionTemplate.execute(any())).thenAnswer(invocation -> {
            when(optimizedRepository.getValidationInfo(transactionId)).thenReturn(validationInfo);
            return invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null);
        });

        // When
        OptimizedMutationValidationService.MutationValidationResult result = 
            validationService.validateRetryOperation(transactionId);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("Exception status does not allow retry");
        assertThat(result.getErrorCode()).isEqualTo("INVALID_STATUS_FOR_RETRY");
    }

    @Test
    void validateRetryOperation_ShouldReturnFailure_WhenRetryLimitExceeded() {
        // Given
        String transactionId = "TXN-004";
        Object[] validationInfo = {4L, ExceptionStatus.NEW, true, 3, 3}; // retryCount >= maxRetries
        
        when(mutationValidationTransactionTemplate.execute(any())).thenAnswer(invocation -> {
            when(optimizedRepository.getValidationInfo(transactionId)).thenReturn(validationInfo);
            return invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null);
        });

        // When
        OptimizedMutationValidationService.MutationValidationResult result = 
            validationService.validateRetryOperation(transactionId);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("Maximum retry attempts exceeded");
        assertThat(result.getErrorCode()).isEqualTo("RETRY_LIMIT_EXCEEDED");
    }

    @Test
    void validateRetryOperation_ShouldReturnFailure_WhenPendingRetriesExist() {
        // Given
        String transactionId = "TXN-005";
        Object[] validationInfo = {5L, ExceptionStatus.NEW, true, 1, 3};
        
        when(mutationValidationTransactionTemplate.execute(any())).thenAnswer(invocation -> {
            when(optimizedRepository.getValidationInfo(transactionId)).thenReturn(validationInfo);
            when(optimizedRepository.countPendingRetries(transactionId)).thenReturn(1L);
            return invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null);
        });

        // When
        OptimizedMutationValidationService.MutationValidationResult result = 
            validationService.validateRetryOperation(transactionId);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Exception has pending retry attempts");
        assertThat(result.getErrorCode()).isEqualTo("PENDING_RETRY_EXISTS");
    }

    @Test
    void validateAcknowledgeOperation_ShouldReturnSuccess_WhenValidForAcknowledge() {
        // Given
        String transactionId = "TXN-001";
        
        when(mutationValidationTransactionTemplate.execute(any())).thenAnswer(invocation -> {
            when(optimizedRepository.getStatusByTransactionId(transactionId)).thenReturn(ExceptionStatus.NEW);
            return invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null);
        });

        // When
        OptimizedMutationValidationService.MutationValidationResult result = 
            validationService.validateAcknowledgeOperation(transactionId);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Acknowledge operation is valid");
    }

    @Test
    void validateAcknowledgeOperation_ShouldReturnFailure_WhenExceptionNotFound() {
        // Given
        String transactionId = "TXN-999";
        
        when(mutationValidationTransactionTemplate.execute(any())).thenAnswer(invocation -> {
            when(optimizedRepository.getStatusByTransactionId(transactionId)).thenReturn(null);
            return invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null);
        });

        // When
        OptimizedMutationValidationService.MutationValidationResult result = 
            validationService.validateAcknowledgeOperation(transactionId);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Exception not found");
        assertThat(result.getErrorCode()).isEqualTo("EXCEPTION_NOT_FOUND");
    }

    @Test
    void validateResolveOperation_ShouldReturnSuccess_WhenValidForResolve() {
        // Given
        String transactionId = "TXN-001";
        
        when(mutationValidationTransactionTemplate.execute(any())).thenAnswer(invocation -> {
            when(optimizedRepository.getStatusByTransactionId(transactionId)).thenReturn(ExceptionStatus.ACKNOWLEDGED);
            return invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null);
        });

        // When
        OptimizedMutationValidationService.MutationValidationResult result = 
            validationService.validateResolveOperation(transactionId);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Resolve operation is valid");
    }

    @Test
    void validateCancelRetryOperation_ShouldReturnSuccess_WhenValidForCancel() {
        // Given
        String transactionId = "TXN-001";
        
        when(mutationValidationTransactionTemplate.execute(any())).thenAnswer(invocation -> {
            when(optimizedRepository.existsByTransactionIdOptimized(transactionId)).thenReturn(true);
            when(optimizedRepository.hasCancellableRetries(transactionId)).thenReturn(true);
            return invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null);
        });

        // When
        OptimizedMutationValidationService.MutationValidationResult result = 
            validationService.validateCancelRetryOperation(transactionId);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Cancel retry operation is valid");
    }

    @Test
    void validateCancelRetryOperation_ShouldReturnFailure_WhenNoCancellableRetries() {
        // Given
        String transactionId = "TXN-001";
        
        when(mutationValidationTransactionTemplate.execute(any())).thenAnswer(invocation -> {
            when(optimizedRepository.existsByTransactionIdOptimized(transactionId)).thenReturn(true);
            when(optimizedRepository.hasCancellableRetries(transactionId)).thenReturn(false);
            return invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null);
        });

        // When
        OptimizedMutationValidationService.MutationValidationResult result = 
            validationService.validateCancelRetryOperation(transactionId);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).isEqualTo("No active retry attempts to cancel");
        assertThat(result.getErrorCode()).isEqualTo("NO_CANCELLABLE_RETRIES");
    }

    @Test
    void findRetryableException_ShouldReturnException_WhenFound() {
        // Given
        String transactionId = "TXN-001";
        InterfaceException mockException = InterfaceException.builder()
            .transactionId(transactionId)
            .retryable(true)
            .status(ExceptionStatus.NEW)
            .build();
        
        when(mutationValidationTransactionTemplate.execute(any())).thenAnswer(invocation -> {
            when(optimizedRepository.findRetryableExceptionByTransactionId(eq(transactionId), anyList()))
                .thenReturn(Optional.of(mockException));
            return invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null);
        });

        // When
        Optional<InterfaceException> result = validationService.findRetryableException(transactionId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTransactionId()).isEqualTo(transactionId);
        assertThat(result.get().getRetryable()).isTrue();
    }

    @Test
    void validateBatchOperations_ShouldReturnValidationResults_ForMultipleTransactions() {
        // Given
        List<String> transactionIds = Arrays.asList("TXN-001", "TXN-002");
        List<Object[]> batchInfo = Arrays.asList(
            new Object[]{"TXN-001", ExceptionStatus.NEW, true, 1, 3},
            new Object[]{"TXN-002", ExceptionStatus.RESOLVED, false, 0, 3}
        );
        
        when(mutationValidationTransactionTemplate.execute(any())).thenAnswer(invocation -> {
            when(optimizedRepository.getBatchValidationInfo(transactionIds)).thenReturn(batchInfo);
            return invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null);
        });

        // When
        List<OptimizedMutationValidationService.BatchValidationResult> results = 
            validationService.validateBatchOperations(transactionIds);

        // Then
        assertThat(results).hasSize(2);
        
        OptimizedMutationValidationService.BatchValidationResult firstResult = results.get(0);
        assertThat(firstResult.getTransactionId()).isEqualTo("TXN-001");
        assertThat(firstResult.getCanRetry()).isTrue();
        assertThat(firstResult.getCanAcknowledge()).isTrue();
        assertThat(firstResult.getCanResolve()).isTrue();
        
        OptimizedMutationValidationService.BatchValidationResult secondResult = results.get(1);
        assertThat(secondResult.getTransactionId()).isEqualTo("TXN-002");
        assertThat(secondResult.getCanRetry()).isFalse();
        assertThat(secondResult.getCanAcknowledge()).isFalse();
        assertThat(secondResult.getCanResolve()).isFalse();
    }
}