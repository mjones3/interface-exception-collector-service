package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError;
import com.arcone.biopro.exception.collector.api.graphql.validation.GraphQLErrorHandler;
import com.arcone.biopro.exception.collector.api.graphql.validation.MutationErrorCode;
import com.arcone.biopro.exception.collector.api.graphql.validation.ValidationResult;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Validation service for cancel retry operations.
 * Provides comprehensive validation for retry cancellation requests including
 * business rule validation, state checking, and concurrent operation handling.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CancelRetryValidationService {

    private final InterfaceExceptionRepository exceptionRepository;
    private final RetryAttemptRepository retryAttemptRepository;

    /**
     * Validates a cancel retry operation request.
     * Performs comprehensive validation including transaction ID format,
     * exception existence, retry state, and business rules.
     *
     * @param transactionId  the transaction ID to cancel retry for
     * @param reason         the cancellation reason
     * @param authentication the current user authentication
     * @return ValidationResult containing validation status and any errors
     */
    public ValidationResult validateCancelRetryOperation(String transactionId, String reason, Authentication authentication) {
        log.debug("Validating cancel retry operation for transaction: {} by user: {}", 
                transactionId, authentication.getName());

        List<GraphQLError> errors = new ArrayList<>();

        // Validate transaction ID format
        if (!isValidTransactionId(transactionId)) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.INVALID_TRANSACTION_ID,
                    "Transaction ID format is invalid: " + transactionId
            ));
        }

        // Validate reason
        if (reason == null || reason.trim().isEmpty()) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.MISSING_REQUIRED_FIELD,
                    "Cancellation reason is required"
            ));
        } else if (reason.length() > 500) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.INVALID_REASON_LENGTH,
                    "Cancellation reason exceeds maximum length of 500 characters"
            ));
        }

        // If basic validation fails, return early
        if (!errors.isEmpty()) {
            return ValidationResult.builder()
                    .valid(false)
                    .errors(errors)
                    .build();
        }

        // Validate exception exists
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
        if (exceptionOpt.isEmpty()) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                    MutationErrorCode.EXCEPTION_NOT_FOUND,
                    "Exception not found for transaction ID: " + transactionId
            ));
            return ValidationResult.builder()
                    .valid(false)
                    .errors(errors)
                    .build();
        }

        InterfaceException exception = exceptionOpt.get();

        // Validate exception state allows cancellation
        if (exception.getStatus() == ExceptionStatus.RESOLVED || 
            exception.getStatus() == ExceptionStatus.CLOSED) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                    MutationErrorCode.CANCELLATION_NOT_ALLOWED,
                    "Cannot cancel retry for resolved or closed exception"
            ));
        }

        // Find the latest retry attempt
        Optional<RetryAttempt> latestAttemptOpt = retryAttemptRepository
                .findTopByInterfaceExceptionOrderByAttemptNumberDesc(exception);

        if (latestAttemptOpt.isEmpty()) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                    MutationErrorCode.NO_PENDING_RETRY,
                    "No retry attempts found for this exception"
            ));
        } else {
            RetryAttempt latestAttempt = latestAttemptOpt.get();
            
            // Validate retry is in a cancellable state
            if (latestAttempt.getStatus() != RetryStatus.PENDING) {
                if (latestAttempt.getStatus() == RetryStatus.SUCCESS) {
                    errors.add(GraphQLErrorHandler.createBusinessRuleError(
                            MutationErrorCode.RETRY_ALREADY_COMPLETED,
                            "Retry has already completed successfully and cannot be cancelled"
                    ));
                } else if (latestAttempt.getStatus() == RetryStatus.FAILED) {
                    errors.add(GraphQLErrorHandler.createBusinessRuleError(
                            MutationErrorCode.RETRY_ALREADY_COMPLETED,
                            "Retry has already failed and cannot be cancelled"
                    ));
                } else if (latestAttempt.getStatus() == RetryStatus.CANCELLED) {
                    errors.add(GraphQLErrorHandler.createBusinessRuleError(
                            MutationErrorCode.RETRY_ALREADY_COMPLETED,
                            "Retry has already been cancelled"
                    ));
                } else {
                    errors.add(GraphQLErrorHandler.createBusinessRuleError(
                            MutationErrorCode.NO_PENDING_RETRY,
                            "No pending retry found to cancel (current status: " + latestAttempt.getStatus() + ")"
                    ));
                }
            }

            // Check if retry has been running too long (might be stuck)
            if (latestAttempt.getStatus() == RetryStatus.PENDING && 
                latestAttempt.getInitiatedAt() != null &&
                latestAttempt.getInitiatedAt().isBefore(java.time.OffsetDateTime.now().minusHours(1))) {
                log.warn("Retry has been pending for over 1 hour for transaction: {}, attempt: {}", 
                        transactionId, latestAttempt.getAttemptNumber());
                // This is allowed but we log it as potentially stuck
            }
        }

        ValidationResult result = ValidationResult.builder()
                .valid(errors.isEmpty())
                .errors(errors)
                .build();

        if (result.isValid()) {
            log.debug("Cancel retry validation passed for transaction: {}", transactionId);
        } else {
            log.warn("Cancel retry validation failed for transaction: {} with {} errors", 
                    transactionId, errors.size());
        }

        return result;
    }

    /**
     * Validates transaction ID format.
     * Checks for null, empty, and basic format requirements.
     *
     * @param transactionId the transaction ID to validate
     * @return true if valid format, false otherwise
     */
    private boolean isValidTransactionId(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            return false;
        }

        // Basic format validation - should be alphanumeric with hyphens/underscores
        String trimmed = transactionId.trim();
        if (trimmed.length() < 3 || trimmed.length() > 100) {
            return false;
        }

        // Check for valid characters (letters, numbers, hyphens, underscores)
        return trimmed.matches("^[a-zA-Z0-9_-]+$");
    }

    /**
     * Checks if a retry can be cancelled based on current state.
     * This method provides additional business logic validation beyond basic validation.
     *
     * @param transactionId the transaction ID
     * @return true if retry can be cancelled, false otherwise
     */
    public boolean canCancelRetry(String transactionId) {
        try {
            Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
            if (exceptionOpt.isEmpty()) {
                return false;
            }

            InterfaceException exception = exceptionOpt.get();

            // Cannot cancel if exception is resolved or closed
            if (exception.getStatus() == ExceptionStatus.RESOLVED || 
                exception.getStatus() == ExceptionStatus.CLOSED) {
                return false;
            }

            // Check for pending retry
            Optional<RetryAttempt> latestAttemptOpt = retryAttemptRepository
                    .findTopByInterfaceExceptionOrderByAttemptNumberDesc(exception);

            if (latestAttemptOpt.isEmpty()) {
                return false;
            }

            RetryAttempt latestAttempt = latestAttemptOpt.get();
            return latestAttempt.getStatus() == RetryStatus.PENDING;

        } catch (Exception e) {
            log.error("Error checking if retry can be cancelled for transaction: {}, error: {}", 
                    transactionId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gets detailed information about why a retry cannot be cancelled.
     * Useful for providing specific error messages to users.
     *
     * @param transactionId the transaction ID
     * @return detailed reason why cancellation is not allowed, or null if cancellation is allowed
     */
    public String getCancellationBlockedReason(String transactionId) {
        try {
            Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
            if (exceptionOpt.isEmpty()) {
                return "Exception not found for transaction ID: " + transactionId;
            }

            InterfaceException exception = exceptionOpt.get();

            if (exception.getStatus() == ExceptionStatus.RESOLVED) {
                return "Exception is already resolved and cannot have retries cancelled";
            }

            if (exception.getStatus() == ExceptionStatus.CLOSED) {
                return "Exception is closed and cannot have retries cancelled";
            }

            Optional<RetryAttempt> latestAttemptOpt = retryAttemptRepository
                    .findTopByInterfaceExceptionOrderByAttemptNumberDesc(exception);

            if (latestAttemptOpt.isEmpty()) {
                return "No retry attempts found for this exception";
            }

            RetryAttempt latestAttempt = latestAttemptOpt.get();
            
            switch (latestAttempt.getStatus()) {
                case SUCCESS:
                    return "Latest retry has already completed successfully";
                case FAILED:
                    return "Latest retry has already failed";
                case CANCELLED:
                    return "Latest retry has already been cancelled";
                case PENDING:
                    return null; // Cancellation is allowed
                default:
                    return "Retry is in an unknown state: " + latestAttempt.getStatus();
            }

        } catch (Exception e) {
            log.error("Error getting cancellation blocked reason for transaction: {}, error: {}", 
                    transactionId, e.getMessage(), e);
            return "Unable to determine cancellation status due to system error";
        }
    }
}