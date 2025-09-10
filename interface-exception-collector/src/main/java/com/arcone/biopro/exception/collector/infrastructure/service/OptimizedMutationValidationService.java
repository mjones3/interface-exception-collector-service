package com.arcone.biopro.exception.collector.infrastructure.service;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.infrastructure.config.MutationQueryTimeoutConfig;
import com.arcone.biopro.exception.collector.infrastructure.repository.OptimizedExceptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Optimized service for mutation validation operations.
 * Uses the OptimizedExceptionRepository to perform fast validation
 * queries for GraphQL mutation operations with appropriate timeouts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OptimizedMutationValidationService {

    private final OptimizedExceptionRepository optimizedRepository;
    private final TransactionTemplate mutationValidationTransactionTemplate;
    private final MutationQueryTimeoutConfig timeoutConfig;

    // Predefined status lists for different mutation types
    private static final List<ExceptionStatus> RETRYABLE_STATUSES = Arrays.asList(
        ExceptionStatus.NEW,
        ExceptionStatus.RETRIED_FAILED,
        ExceptionStatus.ESCALATED
    );

    private static final List<ExceptionStatus> ACKNOWLEDGEABLE_STATUSES = Arrays.asList(
        ExceptionStatus.NEW,
        ExceptionStatus.RETRIED_FAILED,
        ExceptionStatus.ESCALATED
    );

    private static final List<ExceptionStatus> RESOLVABLE_STATUSES = Arrays.asList(
        ExceptionStatus.NEW,
        ExceptionStatus.ACKNOWLEDGED,
        ExceptionStatus.RETRIED_FAILED,
        ExceptionStatus.ESCALATED
    );

    /**
     * Validates if an exception can be retried.
     * Performs optimized validation checking existence, retryable flag,
     * status, and retry limits in minimal database queries.
     *
     * @param transactionId the transaction ID to validate
     * @return validation result with details
     */
    @Transactional(readOnly = true, timeout = 10)
    public MutationValidationResult validateRetryOperation(String transactionId) {
        log.debug("Validating retry operation for transaction ID: {}", transactionId);
        
        return mutationValidationTransactionTemplate.execute(status -> {
            try {
                // Fast existence and basic validation check
                Object[] validationInfo = optimizedRepository.getValidationInfo(transactionId);
                if (validationInfo == null) {
                    return MutationValidationResult.failure("Exception not found", "EXCEPTION_NOT_FOUND");
                }

                Long id = (Long) validationInfo[0];
                ExceptionStatus exceptionStatus = (ExceptionStatus) validationInfo[1];
                Boolean retryable = (Boolean) validationInfo[2];
                Integer retryCount = (Integer) validationInfo[3];
                Integer maxRetries = (Integer) validationInfo[4];

                // Check if retryable
                if (!retryable) {
                    return MutationValidationResult.failure("Exception is not retryable", "NOT_RETRYABLE");
                }

                // Check status
                if (!RETRYABLE_STATUSES.contains(exceptionStatus)) {
                    return MutationValidationResult.failure(
                        "Exception status does not allow retry: " + exceptionStatus, 
                        "INVALID_STATUS_FOR_RETRY"
                    );
                }

                // Check retry limits
                if (retryCount >= maxRetries) {
                    return MutationValidationResult.failure(
                        "Maximum retry attempts exceeded: " + retryCount + "/" + maxRetries, 
                        "RETRY_LIMIT_EXCEEDED"
                    );
                }

                // Check for pending retries
                long pendingRetries = optimizedRepository.countPendingRetries(transactionId);
                if (pendingRetries > 0) {
                    return MutationValidationResult.failure(
                        "Exception has pending retry attempts", 
                        "PENDING_RETRY_EXISTS"
                    );
                }

                log.debug("Retry validation successful for transaction ID: {}", transactionId);
                return MutationValidationResult.success("Retry operation is valid");

            } catch (Exception e) {
                log.error("Error validating retry operation for transaction ID: {}", transactionId, e);
                return MutationValidationResult.failure("Validation error: " + e.getMessage(), "VALIDATION_ERROR");
            }
        });
    }

    /**
     * Validates if an exception can be acknowledged.
     * Performs optimized validation checking existence and status.
     *
     * @param transactionId the transaction ID to validate
     * @return validation result with details
     */
    @Transactional(readOnly = true, timeout = 5)
    public MutationValidationResult validateAcknowledgeOperation(String transactionId) {
        log.debug("Validating acknowledge operation for transaction ID: {}", transactionId);
        
        return mutationValidationTransactionTemplate.execute(status -> {
            try {
                ExceptionStatus exceptionStatus = optimizedRepository.getStatusByTransactionId(transactionId);
                if (exceptionStatus == null) {
                    return MutationValidationResult.failure("Exception not found", "EXCEPTION_NOT_FOUND");
                }

                if (!ACKNOWLEDGEABLE_STATUSES.contains(exceptionStatus)) {
                    return MutationValidationResult.failure(
                        "Exception status does not allow acknowledgment: " + exceptionStatus, 
                        "INVALID_STATUS_FOR_ACKNOWLEDGE"
                    );
                }

                log.debug("Acknowledge validation successful for transaction ID: {}", transactionId);
                return MutationValidationResult.success("Acknowledge operation is valid");

            } catch (Exception e) {
                log.error("Error validating acknowledge operation for transaction ID: {}", transactionId, e);
                return MutationValidationResult.failure("Validation error: " + e.getMessage(), "VALIDATION_ERROR");
            }
        });
    }

    /**
     * Validates if an exception can be resolved.
     * Performs optimized validation checking existence and status.
     *
     * @param transactionId the transaction ID to validate
     * @return validation result with details
     */
    @Transactional(readOnly = true, timeout = 5)
    public MutationValidationResult validateResolveOperation(String transactionId) {
        log.debug("Validating resolve operation for transaction ID: {}", transactionId);
        
        return mutationValidationTransactionTemplate.execute(status -> {
            try {
                ExceptionStatus exceptionStatus = optimizedRepository.getStatusByTransactionId(transactionId);
                if (exceptionStatus == null) {
                    return MutationValidationResult.failure("Exception not found", "EXCEPTION_NOT_FOUND");
                }

                if (!RESOLVABLE_STATUSES.contains(exceptionStatus)) {
                    return MutationValidationResult.failure(
                        "Exception status does not allow resolution: " + exceptionStatus, 
                        "INVALID_STATUS_FOR_RESOLVE"
                    );
                }

                log.debug("Resolve validation successful for transaction ID: {}", transactionId);
                return MutationValidationResult.success("Resolve operation is valid");

            } catch (Exception e) {
                log.error("Error validating resolve operation for transaction ID: {}", transactionId, e);
                return MutationValidationResult.failure("Validation error: " + e.getMessage(), "VALIDATION_ERROR");
            }
        });
    }

    /**
     * Validates if retry operations can be cancelled for an exception.
     * Checks for active retry attempts that can be cancelled.
     *
     * @param transactionId the transaction ID to validate
     * @return validation result with details
     */
    @Transactional(readOnly = true, timeout = 5)
    public MutationValidationResult validateCancelRetryOperation(String transactionId) {
        log.debug("Validating cancel retry operation for transaction ID: {}", transactionId);
        
        return mutationValidationTransactionTemplate.execute(status -> {
            try {
                boolean exists = optimizedRepository.existsByTransactionIdOptimized(transactionId);
                if (!exists) {
                    return MutationValidationResult.failure("Exception not found", "EXCEPTION_NOT_FOUND");
                }

                boolean hasCancellableRetries = optimizedRepository.hasCancellableRetries(transactionId);
                if (!hasCancellableRetries) {
                    return MutationValidationResult.failure(
                        "No active retry attempts to cancel", 
                        "NO_CANCELLABLE_RETRIES"
                    );
                }

                log.debug("Cancel retry validation successful for transaction ID: {}", transactionId);
                return MutationValidationResult.success("Cancel retry operation is valid");

            } catch (Exception e) {
                log.error("Error validating cancel retry operation for transaction ID: {}", transactionId, e);
                return MutationValidationResult.failure("Validation error: " + e.getMessage(), "VALIDATION_ERROR");
            }
        });
    }

    /**
     * Finds an exception optimized for retry operations.
     * Uses the optimized repository to load only necessary data.
     *
     * @param transactionId the transaction ID to find
     * @return optional containing the exception if found and retryable
     */
    @Transactional(readOnly = true, timeout = 10)
    public Optional<InterfaceException> findRetryableException(String transactionId) {
        log.debug("Finding retryable exception for transaction ID: {}", transactionId);
        
        return mutationValidationTransactionTemplate.execute(status -> {
            try {
                return optimizedRepository.findRetryableExceptionByTransactionId(transactionId, RETRYABLE_STATUSES);
            } catch (Exception e) {
                log.error("Error finding retryable exception for transaction ID: {}", transactionId, e);
                return Optional.empty();
            }
        });
    }

    /**
     * Finds an exception optimized for acknowledgment operations.
     *
     * @param transactionId the transaction ID to find
     * @return optional containing the exception if found and acknowledgeable
     */
    @Transactional(readOnly = true, timeout = 5)
    public Optional<InterfaceException> findAcknowledgeableException(String transactionId) {
        log.debug("Finding acknowledgeable exception for transaction ID: {}", transactionId);
        
        return mutationValidationTransactionTemplate.execute(status -> {
            try {
                return optimizedRepository.findAcknowledgeableExceptionByTransactionId(transactionId, ACKNOWLEDGEABLE_STATUSES);
            } catch (Exception e) {
                log.error("Error finding acknowledgeable exception for transaction ID: {}", transactionId, e);
                return Optional.empty();
            }
        });
    }

    /**
     * Finds an exception optimized for resolution operations.
     *
     * @param transactionId the transaction ID to find
     * @return optional containing the exception if found and resolvable
     */
    @Transactional(readOnly = true, timeout = 5)
    public Optional<InterfaceException> findResolvableException(String transactionId) {
        log.debug("Finding resolvable exception for transaction ID: {}", transactionId);
        
        return mutationValidationTransactionTemplate.execute(status -> {
            try {
                return optimizedRepository.findResolvableExceptionByTransactionId(transactionId, RESOLVABLE_STATUSES);
            } catch (Exception e) {
                log.error("Error finding resolvable exception for transaction ID: {}", transactionId, e);
                return Optional.empty();
            }
        });
    }

    /**
     * Finds an exception with active retries for cancel operations.
     *
     * @param transactionId the transaction ID to find
     * @return optional containing the exception if found with active retries
     */
    @Transactional(readOnly = true, timeout = 10)
    public Optional<InterfaceException> findExceptionWithActiveRetries(String transactionId) {
        log.debug("Finding exception with active retries for transaction ID: {}", transactionId);
        
        return mutationValidationTransactionTemplate.execute(status -> {
            try {
                return optimizedRepository.findExceptionWithActiveRetries(transactionId);
            } catch (Exception e) {
                log.error("Error finding exception with active retries for transaction ID: {}", transactionId, e);
                return Optional.empty();
            }
        });
    }

    /**
     * Finds an exception for update operations with optimistic locking.
     *
     * @param transactionId the transaction ID to find
     * @return optional containing the exception if found
     */
    @Transactional(timeout = 10)
    public Optional<InterfaceException> findExceptionForUpdate(String transactionId) {
        log.debug("Finding exception for update with transaction ID: {}", transactionId);
        
        try {
            return optimizedRepository.findByTransactionIdForUpdate(transactionId);
        } catch (Exception e) {
            log.error("Error finding exception for update with transaction ID: {}", transactionId, e);
            return Optional.empty();
        }
    }

    /**
     * Performs batch validation for multiple transaction IDs.
     * Optimized for bulk operations to reduce database round trips.
     *
     * @param transactionIds list of transaction IDs to validate
     * @return list of validation results
     */
    @Transactional(readOnly = true, timeout = 30)
    public List<BatchValidationResult> validateBatchOperations(List<String> transactionIds) {
        log.debug("Performing batch validation for {} transaction IDs", transactionIds.size());
        
        return mutationValidationTransactionTemplate.execute(status -> {
            try {
                List<Object[]> batchInfo = optimizedRepository.getBatchValidationInfo(transactionIds);
                
                return batchInfo.stream()
                    .map(info -> {
                        String txnId = (String) info[0];
                        ExceptionStatus exceptionStatus = (ExceptionStatus) info[1];
                        Boolean retryable = (Boolean) info[2];
                        Integer retryCount = (Integer) info[3];
                        Integer maxRetries = (Integer) info[4];
                        
                        return BatchValidationResult.builder()
                            .transactionId(txnId)
                            .status(exceptionStatus)
                            .retryable(retryable)
                            .retryCount(retryCount)
                            .maxRetries(maxRetries)
                            .canRetry(retryable && RETRYABLE_STATUSES.contains(exceptionStatus) && retryCount < maxRetries)
                            .canAcknowledge(ACKNOWLEDGEABLE_STATUSES.contains(exceptionStatus))
                            .canResolve(RESOLVABLE_STATUSES.contains(exceptionStatus))
                            .build();
                    })
                    .toList();
                    
            } catch (Exception e) {
                log.error("Error performing batch validation", e);
                return List.of();
            }
        });
    }

    /**
     * Result class for mutation validation operations.
     */
    public static class MutationValidationResult {
        private final boolean valid;
        private final String message;
        private final String errorCode;

        private MutationValidationResult(boolean valid, String message, String errorCode) {
            this.valid = valid;
            this.message = message;
            this.errorCode = errorCode;
        }

        public static MutationValidationResult success(String message) {
            return new MutationValidationResult(true, message, null);
        }

        public static MutationValidationResult failure(String message, String errorCode) {
            return new MutationValidationResult(false, message, errorCode);
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public String getErrorCode() { return errorCode; }
    }

    /**
     * Result class for batch validation operations.
     */
    @lombok.Builder
    @lombok.Data
    public static class BatchValidationResult {
        private String transactionId;
        private ExceptionStatus status;
        private Boolean retryable;
        private Integer retryCount;
        private Integer maxRetries;
        private Boolean canRetry;
        private Boolean canAcknowledge;
        private Boolean canResolve;
    }
}