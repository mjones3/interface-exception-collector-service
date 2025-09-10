package com.arcone.biopro.exception.collector.infrastructure.service;

import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionStatusChangedEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.RetryAttemptCompletedEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.RetryAttemptStartedEvent;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheInvalidationServiceTest {

    @Mock
    private DatabaseCachingService databaseCachingService;

    @InjectMocks
    private CacheInvalidationService cacheInvalidationService;

    private ExceptionStatusChangedEvent statusChangedEvent;
    private RetryAttemptStartedEvent retryStartedEvent;
    private RetryAttemptCompletedEvent retryCompletedEvent;

    @BeforeEach
    void setUp() {
        statusChangedEvent = ExceptionStatusChangedEvent.create(
            "TXN-123",
            ExceptionStatus.FAILED,
            ExceptionStatus.ACKNOWLEDGED,
            "test-user",
            "Status change reason",
            "Status change notes"
        );

        retryStartedEvent = RetryAttemptStartedEvent.create(
            "TXN-123",
            1,
            "test-user",
            "Retry reason",
            null,
            "Retry notes"
        );

        retryCompletedEvent = RetryAttemptCompletedEvent.create(
            "TXN-123",
            1,
            RetryStatus.COMPLETED,
            true,
            null,
            1000L
        );
    }

    @Test
    void handleExceptionStatusChanged_ShouldInvalidateAllCaches() {
        // When
        cacheInvalidationService.handleExceptionStatusChanged(statusChangedEvent);

        // Then
        verify(databaseCachingService).invalidateValidationCache("TXN-123");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "retry");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "acknowledge");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "resolve");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "cancel");
    }

    @Test
    void handleExceptionStatusChanged_WhenCacheServiceThrowsException_ShouldContinueExecution() {
        // Given
        doThrow(new RuntimeException("Cache error"))
            .when(databaseCachingService).invalidateValidationCache("TXN-123");

        // When - Should not throw exception
        cacheInvalidationService.handleExceptionStatusChanged(statusChangedEvent);

        // Then
        verify(databaseCachingService).invalidateValidationCache("TXN-123");
        // Other invalidations should still be attempted despite the exception
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "retry");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "acknowledge");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "resolve");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "cancel");
    }

    @Test
    void handleRetryAttemptStarted_ShouldInvalidateRetryRelatedCaches() {
        // When
        cacheInvalidationService.handleRetryAttemptStarted(retryStartedEvent);

        // Then
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "retry");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "cancel");
        verify(databaseCachingService, never()).invalidateValidationCache(anyString());
        verify(databaseCachingService, never()).invalidateOperationValidationCache(anyString(), eq("acknowledge"));
        verify(databaseCachingService, never()).invalidateOperationValidationCache(anyString(), eq("resolve"));
    }

    @Test
    void handleRetryAttemptStarted_WhenCacheServiceThrowsException_ShouldContinueExecution() {
        // Given
        doThrow(new RuntimeException("Cache error"))
            .when(databaseCachingService).invalidateOperationValidationCache("TXN-123", "retry");

        // When - Should not throw exception
        cacheInvalidationService.handleRetryAttemptStarted(retryStartedEvent);

        // Then
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "retry");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "cancel");
    }

    @Test
    void handleRetryAttemptCompleted_WhenSuccessful_ShouldInvalidateAllCaches() {
        // When
        cacheInvalidationService.handleRetryAttemptCompleted(retryCompletedEvent);

        // Then
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "retry");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "cancel");
        verify(databaseCachingService).invalidateValidationCache("TXN-123");
    }

    @Test
    void handleRetryAttemptCompleted_WhenFailed_ShouldInvalidateRetryRelatedCaches() {
        // Given
        RetryAttemptCompletedEvent failedRetryEvent = RetryAttemptCompletedEvent.create(
            "TXN-123",
            1,
            RetryStatus.FAILED,
            false,
            "Retry failed",
            1000L
        );

        // When
        cacheInvalidationService.handleRetryAttemptCompleted(failedRetryEvent);

        // Then
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "retry");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "cancel");
        verify(databaseCachingService, never()).invalidateValidationCache("TXN-123");
    }

    @Test
    void handleRetryAttemptCompleted_WhenCacheServiceThrowsException_ShouldContinueExecution() {
        // Given
        doThrow(new RuntimeException("Cache error"))
            .when(databaseCachingService).invalidateOperationValidationCache("TXN-123", "retry");

        // When - Should not throw exception
        cacheInvalidationService.handleRetryAttemptCompleted(retryCompletedEvent);

        // Then
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "retry");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "cancel");
        verify(databaseCachingService).invalidateValidationCache("TXN-123");
    }

    @Test
    void manuallyInvalidateTransaction_ShouldInvalidateAllCaches() {
        // When
        cacheInvalidationService.manuallyInvalidateTransaction("TXN-123");

        // Then
        verify(databaseCachingService).invalidateValidationCache("TXN-123");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "retry");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "acknowledge");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "resolve");
        verify(databaseCachingService).invalidateOperationValidationCache("TXN-123", "cancel");
    }

    @Test
    void manuallyInvalidateTransaction_WhenCacheServiceThrowsException_ShouldThrowRuntimeException() {
        // Given
        doThrow(new RuntimeException("Cache error"))
            .when(databaseCachingService).invalidateValidationCache("TXN-123");

        // When & Then
        assertThatThrownBy(() -> cacheInvalidationService.manuallyInvalidateTransaction("TXN-123"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Cache invalidation failed for transaction: TXN-123")
            .hasCauseInstanceOf(RuntimeException.class);

        verify(databaseCachingService).invalidateValidationCache("TXN-123");
    }

    @Test
    void clearAllCaches_ShouldCallClearAllValidationCaches() {
        // When
        cacheInvalidationService.clearAllCaches();

        // Then
        verify(databaseCachingService).clearAllValidationCaches();
    }

    @Test
    void clearAllCaches_WhenCacheServiceThrowsException_ShouldThrowRuntimeException() {
        // Given
        doThrow(new RuntimeException("Cache error"))
            .when(databaseCachingService).clearAllValidationCaches();

        // When & Then
        assertThatThrownBy(() -> cacheInvalidationService.clearAllCaches())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to clear all validation caches")
            .hasCauseInstanceOf(RuntimeException.class);

        verify(databaseCachingService).clearAllValidationCaches();
    }
}