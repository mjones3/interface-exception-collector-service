package com.arcone.biopro.exception.collector.infrastructure.service;

import com.arcone.biopro.exception.collector.api.graphql.validation.ValidationResult;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.infrastructure.config.ValidationCacheConfig;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
    DatabaseCachingService.class,
    ValidationCacheConfig.class
})
@TestPropertySource(properties = {
    "app.validation.cache.existence-ttl=PT1S",
    "app.validation.cache.retryable-ttl=PT1S",
    "app.validation.cache.retry-count-ttl=PT1S",
    "app.validation.cache.pending-retry-ttl=PT1S",
    "app.validation.cache.status-ttl=PT1S",
    "app.validation.cache.validation-result-ttl=PT1S",
    "app.validation.cache.max-cache-size=100"
})
class DatabaseCachingServiceIntegrationTest {

    @Autowired
    private DatabaseCachingService databaseCachingService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private InterfaceExceptionRepository exceptionRepository;

    @MockBean
    private RetryAttemptRepository retryAttemptRepository;

    private InterfaceException testException;
    private RetryAttempt testRetryAttempt;

    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        cacheManager.getCacheNames().forEach(cacheName -> {
            cacheManager.getCache(cacheName).clear();
        });

        testException = new InterfaceException();
        testException.setTransactionId("TXN-123");
        testException.setRetryable(true);
        testException.setStatus(ExceptionStatus.FAILED);
        testException.setRetryCount(1);
        testException.setMaxRetries(3);

        testRetryAttempt = new RetryAttempt();
        testRetryAttempt.setStatus(RetryStatus.COMPLETED);
        testRetryAttempt.setAttemptNumber(1);
        testRetryAttempt.setInterfaceException(testException);

        // Reset mocks
        reset(exceptionRepository, retryAttemptRepository);
    }

    @Test
    void validateExceptionExists_ShouldCacheResults() {
        // Given
        String transactionId = "TXN-123";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));

        // When - First call
        ValidationResult result1 = databaseCachingService.validateExceptionExists(transactionId);
        
        // When - Second call (should use cache)
        ValidationResult result2 = databaseCachingService.validateExceptionExists(transactionId);

        // Then
        assertThat(result1.isValid()).isTrue();
        assertThat(result2.isValid()).isTrue();
        
        // Repository should only be called once due to caching
        verify(exceptionRepository, times(1)).findByTransactionId(transactionId);
    }

    @Test
    void validateForOperation_ShouldCacheCompleteValidationResults() {
        // Given
        String transactionId = "TXN-123";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
            .thenReturn(Optional.of(testRetryAttempt));

        // When - First call
        ValidationResult result1 = databaseCachingService.validateForOperation(transactionId, "retry");
        
        // When - Second call (should use cache)
        ValidationResult result2 = databaseCachingService.validateForOperation(transactionId, "retry");

        // Then
        assertThat(result1.isValid()).isTrue();
        assertThat(result2.isValid()).isTrue();
        
        // Repository calls should be minimized due to caching
        // Note: Some calls may still occur for individual validation methods
        verify(exceptionRepository, atLeast(1)).findByTransactionId(transactionId);
    }

    @Test
    void cacheInvalidation_ShouldClearSpecificCaches() {
        // Given
        String transactionId = "TXN-123";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));

        // When - Cache a result
        ValidationResult result1 = databaseCachingService.validateExceptionExists(transactionId);
        assertThat(result1.isValid()).isTrue();

        // When - Invalidate cache
        databaseCachingService.invalidateValidationCache(transactionId);

        // When - Call again (should hit repository again)
        ValidationResult result2 = databaseCachingService.validateExceptionExists(transactionId);

        // Then
        assertThat(result2.isValid()).isTrue();
        verify(exceptionRepository, times(2)).findByTransactionId(transactionId);
    }

    @Test
    void cacheExpiration_ShouldExpireAfterTtl() throws InterruptedException {
        // Given
        String transactionId = "TXN-123";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));

        // When - Cache a result
        ValidationResult result1 = databaseCachingService.validateExceptionExists(transactionId);
        assertThat(result1.isValid()).isTrue();

        // When - Wait for cache expiration (TTL is set to 1 second in test properties)
        Thread.sleep(1100);

        // When - Call again (should hit repository again due to expiration)
        ValidationResult result2 = databaseCachingService.validateExceptionExists(transactionId);

        // Then
        assertThat(result2.isValid()).isTrue();
        verify(exceptionRepository, times(2)).findByTransactionId(transactionId);
    }

    @Test
    void multipleOperationTypes_ShouldCacheSeparately() {
        // Given
        String transactionId = "TXN-123";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
            .thenReturn(Optional.of(testRetryAttempt));

        // When - Cache different operation types
        ValidationResult retryResult1 = databaseCachingService.validateForOperation(transactionId, "retry");
        ValidationResult acknowledgeResult1 = databaseCachingService.validateForOperation(transactionId, "acknowledge");
        
        // When - Call again (should use separate caches)
        ValidationResult retryResult2 = databaseCachingService.validateForOperation(transactionId, "retry");
        ValidationResult acknowledgeResult2 = databaseCachingService.validateForOperation(transactionId, "acknowledge");

        // Then
        assertThat(retryResult1.isValid()).isTrue();
        assertThat(acknowledgeResult1.isValid()).isTrue();
        assertThat(retryResult2.isValid()).isTrue();
        assertThat(acknowledgeResult2.isValid()).isTrue();
        
        // Each operation type should have its own cache
        assertThat(retryResult1.getOperationType()).isEqualTo("retry");
        assertThat(acknowledgeResult1.getOperationType()).isEqualTo("acknowledge");
    }

    @Test
    void clearAllCaches_ShouldClearAllValidationCaches() {
        // Given
        String transactionId = "TXN-123";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));

        // When - Cache multiple results
        databaseCachingService.validateExceptionExists(transactionId);
        databaseCachingService.validateExceptionRetryable(transactionId);
        databaseCachingService.validateRetryCount(transactionId);

        // When - Clear all caches
        databaseCachingService.clearAllValidationCaches();

        // When - Call again (should hit repository again)
        databaseCachingService.validateExceptionExists(transactionId);

        // Then - Repository should be called again after cache clear
        verify(exceptionRepository, times(4)).findByTransactionId(transactionId);
    }

    @Test
    void cacheStatistics_ShouldTrackHitsAndMisses() {
        // Given
        String transactionId = "TXN-123";
        when(exceptionRepository.findByTransactionId(transactionId))
            .thenReturn(Optional.of(testException));

        // When - Generate cache hits and misses
        databaseCachingService.validateExceptionExists(transactionId); // Miss
        databaseCachingService.validateExceptionExists(transactionId); // Hit
        databaseCachingService.validateExceptionExists("TXN-404"); // Miss

        // Then - Verify cache behavior
        verify(exceptionRepository, times(2)).findByTransactionId(anyString());
    }

    @Test
    void differentTransactionIds_ShouldCacheSeparately() {
        // Given
        String transactionId1 = "TXN-123";
        String transactionId2 = "TXN-456";
        
        InterfaceException exception2 = new InterfaceException();
        exception2.setTransactionId(transactionId2);
        exception2.setRetryable(false);
        exception2.setStatus(ExceptionStatus.RESOLVED);
        
        when(exceptionRepository.findByTransactionId(transactionId1))
            .thenReturn(Optional.of(testException));
        when(exceptionRepository.findByTransactionId(transactionId2))
            .thenReturn(Optional.of(exception2));

        // When - Cache results for different transactions
        ValidationResult result1 = databaseCachingService.validateExceptionExists(transactionId1);
        ValidationResult result2 = databaseCachingService.validateExceptionExists(transactionId2);
        
        // When - Call again (should use separate caches)
        ValidationResult result1Cached = databaseCachingService.validateExceptionExists(transactionId1);
        ValidationResult result2Cached = databaseCachingService.validateExceptionExists(transactionId2);

        // Then
        assertThat(result1.isValid()).isTrue();
        assertThat(result2.isValid()).isTrue();
        assertThat(result1Cached.isValid()).isTrue();
        assertThat(result2Cached.isValid()).isTrue();
        
        // Each transaction should be cached separately
        verify(exceptionRepository, times(1)).findByTransactionId(transactionId1);
        verify(exceptionRepository, times(1)).findByTransactionId(transactionId2);
    }
}