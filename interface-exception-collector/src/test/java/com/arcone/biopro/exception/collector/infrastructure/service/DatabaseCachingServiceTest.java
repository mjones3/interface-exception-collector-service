package com.arcone.biopro.exception.collector.infrastructure.service;

import com.arcone.biopro.exception.collector.api.graphql.validation.ValidationResult;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseCachingServiceTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private RetryAttemptRepository retryAttemptRepository;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private DatabaseCachingService databaseCachingService;

    private InterfaceException testException;
    private RetryAttempt testRetryAttempt;

    @BeforeEach
    void setUp() {
        testException = new InterfaceException();
        testException.setTransactionId("TXN-123");
        testException.setRetryable(true);
        testException.setStatus(ExceptionStatus.FAILED);
        testException.setRetryCount(1);
        testException.setMaxRetries(3);

        testRetryAttempt = new RetryAttempt();
        testRetryAttempt.setStatus(RetryStatus.PENDING);
        testRetryAttempt.setAttemptNumber(1);
        testRetryAttempt.setInterfaceException(testException);

        // Mock cache manager to return simple caches
        when(cacheManager.getCache(anyString())).thenReturn(new ConcurrentMapCache("test-cache"));
    }

    @Test
    void validateExceptionExists_WhenExceptionExists_ShouldReturnSuccess() {
        // Given
        String transactionId = "TXN-123";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));

        // When
        ValidationResult result = databaseCachingService.validateExceptionExists(transactionId);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getOperationType()).isEqualTo("existence");
        assertThat(result.getTransactionId()).isEqualTo(transactionId);
        verify(exceptionRepository).findByTransactionId(transactionId);
    }

    @Test
    void validateExceptionExists_WhenExceptionNotFound_ShouldReturnFailure() {
        // Given
        String transactionId = "TXN-404";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.empty());

        // When
        ValidationResult result = databaseCachingService.validateExceptionExists(transactionId);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getOperationType()).isEqualTo("existence");
        assertThat(result.getTransactionId()).isEqualTo(transactionId);
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("EXCEPTION_NOT_FOUND");
        verify(exceptionRepository).findByTransactionId(transactionId);
    }

    @Test
    void validateExceptionRetryable_WhenRetryable_ShouldReturnSuccess() {
        // Given
        String transactionId = "TXN-123";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));

        // When
        ValidationResult result = databaseCachingService.validateExceptionRetryable(transactionId);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getOperationType()).isEqualTo("retryable");
        verify(exceptionRepository).findByTransactionId(transactionId);
    }

    @Test
    void validateExceptionRetryable_WhenNotRetryable_ShouldReturnFailure() {
        // Given
        String transactionId = "TXN-123";
        testException.setRetryable(false);
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));

        // When
        ValidationResult result = databaseCachingService.validateExceptionRetryable(transactionId);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getOperationType()).isEqualTo("retryable");
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("NOT_RETRYABLE");
        verify(exceptionRepository).findByTransactionId(transactionId);
    }

    @Test
    void validateRetryCount_WhenWithinLimits_ShouldReturnSuccess() {
        // Given
        String transactionId = "TXN-123";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));

        // When
        ValidationResult result = databaseCachingService.validateRetryCount(transactionId);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getOperationType()).isEqualTo("retry_count");
        verify(exceptionRepository).findByTransactionId(transactionId);
    }

    @Test
    void validateRetryCount_WhenLimitExceeded_ShouldReturnFailure() {
        // Given
        String transactionId = "TXN-123";
        testException.setRetryCount(3);
        testException.setMaxRetries(3);
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));

        // When
        ValidationResult result = databaseCachingService.validateRetryCount(transactionId);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getOperationType()).isEqualTo("retry_count");
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("RETRY_LIMIT_EXCEEDED");
        verify(exceptionRepository).findByTransactionId(transactionId);
    }

    @Test
    void validateNoPendingRetry_WhenNoPendingRetry_ShouldReturnSuccess() {
        // Given
        String transactionId = "TXN-123";
        testRetryAttempt.setStatus(RetryStatus.COMPLETED);
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
            .thenReturn(Optional.of(testRetryAttempt));

        // When
        ValidationResult result = databaseCachingService.validateNoPendingRetry(transactionId);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getOperationType()).isEqualTo("pending_retry");
        verify(exceptionRepository).findByTransactionId(transactionId);
        verify(retryAttemptRepository).findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException);
    }

    @Test
    void validateNoPendingRetry_WhenPendingRetryExists_ShouldReturnFailure() {
        // Given
        String transactionId = "TXN-123";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
            .thenReturn(Optional.of(testRetryAttempt));

        // When
        ValidationResult result = databaseCachingService.validateNoPendingRetry(transactionId);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getOperationType()).isEqualTo("pending_retry");
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("PENDING_RETRY_EXISTS");
        verify(exceptionRepository).findByTransactionId(transactionId);
        verify(retryAttemptRepository).findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException);
    }

    @Test
    void validateExceptionStatus_WhenStatusAllowsRetry_ShouldReturnSuccess() {
        // Given
        String transactionId = "TXN-123";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));

        // When
        ValidationResult result = databaseCachingService.validateExceptionStatus(transactionId);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getOperationType()).isEqualTo("status");
        verify(exceptionRepository).findByTransactionId(transactionId);
    }

    @Test
    void validateExceptionStatus_WhenStatusDoesNotAllowRetry_ShouldReturnFailure() {
        // Given
        String transactionId = "TXN-123";
        testException.setStatus(ExceptionStatus.RESOLVED);
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));

        // When
        ValidationResult result = databaseCachingService.validateExceptionStatus(transactionId);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getOperationType()).isEqualTo("status");
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("INVALID_STATUS_TRANSITION");
        verify(exceptionRepository).findByTransactionId(transactionId);
    }

    @Test
    void validateForOperation_RetryOperation_WhenAllValidationsPassed_ShouldReturnSuccess() {
        // Given
        String transactionId = "TXN-123";
        testRetryAttempt.setStatus(RetryStatus.COMPLETED);
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
            .thenReturn(Optional.of(testRetryAttempt));

        // When
        ValidationResult result = databaseCachingService.validateForOperation(transactionId, "retry");

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getOperationType()).isEqualTo("retry");
        verify(exceptionRepository, atLeastOnce()).findByTransactionId(transactionId);
    }

    @Test
    void validateForOperation_AcknowledgeOperation_WhenValidationsPassed_ShouldReturnSuccess() {
        // Given
        String transactionId = "TXN-123";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));

        // When
        ValidationResult result = databaseCachingService.validateForOperation(transactionId, "acknowledge");

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getOperationType()).isEqualTo("acknowledge");
        verify(exceptionRepository, atLeastOnce()).findByTransactionId(transactionId);
    }

    @Test
    void validateForOperation_ResolveOperation_WhenValidationsPassed_ShouldReturnSuccess() {
        // Given
        String transactionId = "TXN-123";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));

        // When
        ValidationResult result = databaseCachingService.validateForOperation(transactionId, "resolve");

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getOperationType()).isEqualTo("resolve");
        verify(exceptionRepository, atLeastOnce()).findByTransactionId(transactionId);
    }

    @Test
    void validateForOperation_CancelOperation_WhenPendingRetryExists_ShouldReturnSuccess() {
        // Given
        String transactionId = "TXN-123";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
            .thenReturn(Optional.of(testRetryAttempt));

        // When
        ValidationResult result = databaseCachingService.validateForOperation(transactionId, "cancel");

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getOperationType()).isEqualTo("cancel");
        verify(exceptionRepository, atLeastOnce()).findByTransactionId(transactionId);
        verify(retryAttemptRepository).findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException);
    }

    @Test
    void validateForOperation_CancelOperation_WhenNoPendingRetry_ShouldReturnFailure() {
        // Given
        String transactionId = "TXN-123";
        testRetryAttempt.setStatus(RetryStatus.COMPLETED);
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
            .thenReturn(Optional.of(testRetryAttempt));

        // When
        ValidationResult result = databaseCachingService.validateForOperation(transactionId, "cancel");

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getOperationType()).isEqualTo("cancel");
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("NO_PENDING_RETRY_TO_CANCEL");
        verify(exceptionRepository, atLeastOnce()).findByTransactionId(transactionId);
        verify(retryAttemptRepository).findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException);
    }

    @Test
    void validateForOperation_UnknownOperation_ShouldReturnFailure() {
        // Given
        String transactionId = "TXN-123";
        String operationType = "unknown";

        // When
        ValidationResult result = databaseCachingService.validateForOperation(transactionId, operationType);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getOperationType()).isEqualTo(operationType);
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("INVALID_OPERATION_TYPE");
    }

    @Test
    void invalidateValidationCache_ShouldLogDebugMessage() {
        // Given
        String transactionId = "TXN-123";

        // When
        databaseCachingService.invalidateValidationCache(transactionId);

        // Then - No exception should be thrown and method should complete
        // This test mainly verifies the method signature and basic functionality
    }

    @Test
    void invalidateOperationValidationCache_ShouldLogDebugMessage() {
        // Given
        String transactionId = "TXN-123";
        String operationType = "retry";

        // When
        databaseCachingService.invalidateOperationValidationCache(transactionId, operationType);

        // Then - No exception should be thrown and method should complete
        // This test mainly verifies the method signature and basic functionality
    }

    @Test
    void clearAllValidationCaches_ShouldLogInfoMessage() {
        // When
        databaseCachingService.clearAllValidationCaches();

        // Then - No exception should be thrown and method should complete
        // This test mainly verifies the method signature and basic functionality
    }
}