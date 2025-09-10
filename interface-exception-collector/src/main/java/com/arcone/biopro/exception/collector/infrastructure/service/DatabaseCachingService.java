package com.arcone.biopro.exception.collector.infrastructure.service;

import com.arcone.biopro.exception.collector.api.graphql.validation.ValidationResult;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * Service for caching validation results without Redis dependency.
 * Uses Spring's simple in-memory cache to improve performance of frequently accessed validation data.
 * 
 * Cache TTL is configured through application properties and cache invalidation occurs
 * when exception status changes to prevent stale data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseCachingService {

    private final InterfaceExceptionRepository exceptionRepository;
    private final RetryAttemptRepository retryAttemptRepository;

    // Cache keys for different validation types
    private static final String EXCEPTION_EXISTENCE_CACHE = "exception-existence";
    private static final String EXCEPTION_RETRYABLE_CACHE = "exception-retryable";
    private static final String RETRY_COUNT_CACHE = "retry-count";
    private static final String PENDING_RETRY_CACHE = "pending-retry";
    private static final String EXCEPTION_STATUS_CACHE = "exception-status";
    private static final String VALIDATION_RESULT_CACHE = "validation-result";

    /**
     * Caches exception existence validation results.
     * 
     * @param transactionId the transaction ID to validate
     * @return ValidationResult indicating if exception exists
     */
    @Cacheable(value = EXCEPTION_EXISTENCE_CACHE, key = "#transactionId")
    public ValidationResult validateExceptionExists(String transactionId) {
        log.debug("Validating exception existence for transaction: {} (cache miss)", transactionId);
        
        Optional<InterfaceException> exception = exceptionRepository.findByTransactionId(transactionId);
        
        if (exception.isPresent()) {
            log.debug("Exception exists for transaction: {}", transactionId);
            return ValidationResult.success("existence", transactionId);
        } else {
            log.debug("Exception not found for transaction: {}", transactionId);
            return ValidationResult.failure("existence", transactionId, 
                java.util.List.of(createNotFoundError(transactionId)));
        }
    }

    /**
     * Caches exception retryable status validation.
     * 
     * @param transactionId the transaction ID to validate
     * @return ValidationResult indicating if exception is retryable
     */
    @Cacheable(value = EXCEPTION_RETRYABLE_CACHE, key = "#transactionId")
    public ValidationResult validateExceptionRetryable(String transactionId) {
        log.debug("Validating exception retryable status for transaction: {} (cache miss)", transactionId);
        
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
        
        if (exceptionOpt.isEmpty()) {
            return ValidationResult.failure("retryable", transactionId, 
                java.util.List.of(createNotFoundError(transactionId)));
        }
        
        InterfaceException exception = exceptionOpt.get();
        
        if (!exception.getRetryable()) {
            log.debug("Exception is not retryable for transaction: {}", transactionId);
            return ValidationResult.failure("retryable", transactionId, 
                java.util.List.of(createNotRetryableError(transactionId)));
        }
        
        log.debug("Exception is retryable for transaction: {}", transactionId);
        return ValidationResult.success("retryable", transactionId);
    }

    /**
     * Caches retry count validation results.
     * 
     * @param transactionId the transaction ID to validate
     * @return ValidationResult indicating if retry count is within limits
     */
    @Cacheable(value = RETRY_COUNT_CACHE, key = "#transactionId")
    public ValidationResult validateRetryCount(String transactionId) {
        log.debug("Validating retry count for transaction: {} (cache miss)", transactionId);
        
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
        
        if (exceptionOpt.isEmpty()) {
            return ValidationResult.failure("retry_count", transactionId, 
                java.util.List.of(createNotFoundError(transactionId)));
        }
        
        InterfaceException exception = exceptionOpt.get();
        
        if (exception.getRetryCount() >= exception.getMaxRetries()) {
            log.debug("Retry count exceeded for transaction: {} ({}/{})", 
                transactionId, exception.getRetryCount(), exception.getMaxRetries());
            return ValidationResult.failure("retry_count", transactionId, 
                java.util.List.of(createRetryLimitExceededError(transactionId, 
                    exception.getRetryCount(), exception.getMaxRetries())));
        }
        
        log.debug("Retry count within limits for transaction: {} ({}/{})", 
            transactionId, exception.getRetryCount(), exception.getMaxRetries());
        return ValidationResult.success("retry_count", transactionId);
    }

    /**
     * Caches pending retry validation results.
     * 
     * @param transactionId the transaction ID to validate
     * @return ValidationResult indicating if there are pending retries
     */
    @Cacheable(value = PENDING_RETRY_CACHE, key = "#transactionId")
    public ValidationResult validateNoPendingRetry(String transactionId) {
        log.debug("Validating no pending retry for transaction: {} (cache miss)", transactionId);
        
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
        
        if (exceptionOpt.isEmpty()) {
            return ValidationResult.failure("pending_retry", transactionId, 
                java.util.List.of(createNotFoundError(transactionId)));
        }
        
        InterfaceException exception = exceptionOpt.get();
        Optional<RetryAttempt> latestAttempt = retryAttemptRepository
            .findTopByInterfaceExceptionOrderByAttemptNumberDesc(exception);
        
        if (latestAttempt.isPresent() && 
            latestAttempt.get().getStatus() == com.arcone.biopro.exception.collector.domain.enums.RetryStatus.PENDING) {
            log.debug("Pending retry found for transaction: {}", transactionId);
            return ValidationResult.failure("pending_retry", transactionId, 
                java.util.List.of(createPendingRetryError(transactionId)));
        }
        
        log.debug("No pending retry for transaction: {}", transactionId);
        return ValidationResult.success("pending_retry", transactionId);
    }

    /**
     * Caches exception status validation results.
     * 
     * @param transactionId the transaction ID to validate
     * @return ValidationResult indicating if exception status allows operations
     */
    @Cacheable(value = EXCEPTION_STATUS_CACHE, key = "#transactionId")
    public ValidationResult validateExceptionStatus(String transactionId) {
        log.debug("Validating exception status for transaction: {} (cache miss)", transactionId);
        
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
        
        if (exceptionOpt.isEmpty()) {
            return ValidationResult.failure("status", transactionId, 
                java.util.List.of(createNotFoundError(transactionId)));
        }
        
        InterfaceException exception = exceptionOpt.get();
        Set<ExceptionStatus> nonRetryableStatuses = Set.of(
            ExceptionStatus.RESOLVED,
            ExceptionStatus.CLOSED
        );
        
        if (nonRetryableStatuses.contains(exception.getStatus())) {
            log.debug("Exception status does not allow retry for transaction: {} (status: {})", 
                transactionId, exception.getStatus());
            return ValidationResult.failure("status", transactionId, 
                java.util.List.of(createInvalidStatusError(transactionId, exception.getStatus())));
        }
        
        log.debug("Exception status allows operations for transaction: {} (status: {})", 
            transactionId, exception.getStatus());
        return ValidationResult.success("status", transactionId);
    }

    /**
     * Caches complete validation results for retry operations.
     * This is a higher-level cache that combines multiple validation checks.
     * 
     * @param transactionId the transaction ID to validate
     * @param operationType the type of operation (retry, acknowledge, resolve, cancel)
     * @return ValidationResult with complete validation status
     */
    @Cacheable(value = VALIDATION_RESULT_CACHE, key = "#transactionId + ':' + #operationType")
    public ValidationResult validateForOperation(String transactionId, String operationType) {
        log.debug("Validating {} operation for transaction: {} (cache miss)", operationType, transactionId);
        
        // Perform comprehensive validation based on operation type
        switch (operationType.toLowerCase()) {
            case "retry":
                return validateForRetryOperation(transactionId);
            case "acknowledge":
                return validateForAcknowledgeOperation(transactionId);
            case "resolve":
                return validateForResolveOperation(transactionId);
            case "cancel":
                return validateForCancelOperation(transactionId);
            default:
                log.warn("Unknown operation type: {}", operationType);
                return ValidationResult.failure(operationType, transactionId, 
                    java.util.List.of(createInvalidOperationError(operationType)));
        }
    }

    /**
     * Invalidates all caches for a specific transaction ID.
     * This should be called when exception status changes to prevent stale data.
     * 
     * @param transactionId the transaction ID to invalidate
     */
    @CacheEvict(value = {
        EXCEPTION_EXISTENCE_CACHE,
        EXCEPTION_RETRYABLE_CACHE,
        RETRY_COUNT_CACHE,
        PENDING_RETRY_CACHE,
        EXCEPTION_STATUS_CACHE,
        VALIDATION_RESULT_CACHE
    }, key = "#transactionId")
    public void invalidateValidationCache(String transactionId) {
        log.debug("Invalidating validation cache for transaction: {}", transactionId);
    }

    /**
     * Invalidates validation result cache for specific operation.
     * 
     * @param transactionId the transaction ID
     * @param operationType the operation type
     */
    @CacheEvict(value = VALIDATION_RESULT_CACHE, key = "#transactionId + ':' + #operationType")
    public void invalidateOperationValidationCache(String transactionId, String operationType) {
        log.debug("Invalidating {} operation validation cache for transaction: {}", 
            operationType, transactionId);
    }

    /**
     * Clears all validation caches.
     * Useful for maintenance or when cache becomes inconsistent.
     */
    @CacheEvict(value = {
        EXCEPTION_EXISTENCE_CACHE,
        EXCEPTION_RETRYABLE_CACHE,
        RETRY_COUNT_CACHE,
        PENDING_RETRY_CACHE,
        EXCEPTION_STATUS_CACHE,
        VALIDATION_RESULT_CACHE
    }, allEntries = true)
    public void clearAllValidationCaches() {
        log.info("Clearing all validation caches");
    }

    // Private helper methods for specific operation validations

    private ValidationResult validateForRetryOperation(String transactionId) {
        // Check existence
        ValidationResult existenceResult = validateExceptionExists(transactionId);
        if (!existenceResult.isValid()) {
            return existenceResult;
        }

        // Check retryable status
        ValidationResult retryableResult = validateExceptionRetryable(transactionId);
        if (!retryableResult.isValid()) {
            return retryableResult;
        }

        // Check retry count
        ValidationResult retryCountResult = validateRetryCount(transactionId);
        if (!retryCountResult.isValid()) {
            return retryCountResult;
        }

        // Check no pending retry
        ValidationResult pendingRetryResult = validateNoPendingRetry(transactionId);
        if (!pendingRetryResult.isValid()) {
            return pendingRetryResult;
        }

        // Check status
        ValidationResult statusResult = validateExceptionStatus(transactionId);
        if (!statusResult.isValid()) {
            return statusResult;
        }

        return ValidationResult.success("retry", transactionId);
    }

    private ValidationResult validateForAcknowledgeOperation(String transactionId) {
        // Check existence
        ValidationResult existenceResult = validateExceptionExists(transactionId);
        if (!existenceResult.isValid()) {
            return existenceResult;
        }

        // Check status (resolved/closed exceptions cannot be acknowledged)
        ValidationResult statusResult = validateExceptionStatus(transactionId);
        if (!statusResult.isValid()) {
            return statusResult;
        }

        return ValidationResult.success("acknowledge", transactionId);
    }

    private ValidationResult validateForResolveOperation(String transactionId) {
        // Check existence
        ValidationResult existenceResult = validateExceptionExists(transactionId);
        if (!existenceResult.isValid()) {
            return existenceResult;
        }

        // For resolve, we only need to check existence
        // Resolved exceptions can be re-resolved with different methods
        return ValidationResult.success("resolve", transactionId);
    }

    private ValidationResult validateForCancelOperation(String transactionId) {
        // Check existence
        ValidationResult existenceResult = validateExceptionExists(transactionId);
        if (!existenceResult.isValid()) {
            return existenceResult;
        }

        // For cancel, we need to check if there's a pending retry to cancel
        // This is the opposite of the retry validation
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
        if (exceptionOpt.isEmpty()) {
            return ValidationResult.failure("cancel", transactionId, 
                java.util.List.of(createNotFoundError(transactionId)));
        }

        InterfaceException exception = exceptionOpt.get();
        Optional<RetryAttempt> latestAttempt = retryAttemptRepository
            .findTopByInterfaceExceptionOrderByAttemptNumberDesc(exception);

        if (latestAttempt.isEmpty() || 
            latestAttempt.get().getStatus() != com.arcone.biopro.exception.collector.domain.enums.RetryStatus.PENDING) {
            return ValidationResult.failure("cancel", transactionId, 
                java.util.List.of(createNoPendingRetryToCancelError(transactionId)));
        }

        return ValidationResult.success("cancel", transactionId);
    }

    // Error creation helper methods

    private com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError createNotFoundError(String transactionId) {
        return com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError.builder()
            .message("Exception not found for transaction: " + transactionId)
            .code("EXCEPTION_NOT_FOUND")
            .build();
    }

    private com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError createNotRetryableError(String transactionId) {
        return com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError.builder()
            .message("Exception is not retryable for transaction: " + transactionId)
            .code("NOT_RETRYABLE")
            .build();
    }

    private com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError createRetryLimitExceededError(
            String transactionId, int currentCount, int maxRetries) {
        return com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError.builder()
            .message(String.format("Retry limit exceeded for transaction: %s (%d/%d)", 
                transactionId, currentCount, maxRetries))
            .code("RETRY_LIMIT_EXCEEDED")
            .build();
    }

    private com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError createPendingRetryError(String transactionId) {
        return com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError.builder()
            .message("A retry is already pending for transaction: " + transactionId)
            .code("PENDING_RETRY_EXISTS")
            .build();
    }

    private com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError createInvalidStatusError(
            String transactionId, ExceptionStatus status) {
        return com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError.builder()
            .message(String.format("Exception cannot be processed due to status: %s for transaction: %s", 
                status, transactionId))
            .code("INVALID_STATUS_TRANSITION")
            .build();
    }

    private com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError createInvalidOperationError(String operationType) {
        return com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError.builder()
            .message("Invalid operation type: " + operationType)
            .code("INVALID_OPERATION_TYPE")
            .build();
    }

    private com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError createNoPendingRetryToCancelError(String transactionId) {
        return com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError.builder()
            .message("No pending retry found to cancel for transaction: " + transactionId)
            .code("NO_PENDING_RETRY_TO_CANCEL")
            .build();
    }
}