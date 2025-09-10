package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.ResolveExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.validation.ValidationResult;
import com.arcone.biopro.exception.collector.api.graphql.validation.MutationErrorCode;
import com.arcone.biopro.exception.collector.api.graphql.validation.GraphQLErrorHandler;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput.RetryPriority;
import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import com.arcone.biopro.exception.collector.domain.exception.ExceptionNotFoundException;
import com.arcone.biopro.exception.collector.domain.exception.RetryNotAllowedException;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Enhanced service for validating retry operations and user permissions.
 * Provides detailed validation with specific error codes and categorization
 * for better client error handling and debugging support.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RetryValidationService {

    private final InterfaceExceptionRepository exceptionRepository;
    private final RetryAttemptRepository retryAttemptRepository;
    private final com.arcone.biopro.exception.collector.infrastructure.service.DatabaseCachingService databaseCachingService;

    // Validation constants
    private static final int MAX_REASON_LENGTH = 500;
    private static final int MAX_NOTES_LENGTH = 1000;
    private static final int MAX_BULK_OPERATION_SIZE = 100;
    private static final int MAX_BULK_OPERATION_SIZE_NON_ADMIN = 10;
    private static final Pattern TRANSACTION_ID_PATTERN = Pattern.compile("^[A-Za-z0-9\\-_]{1,50}$");

    /**
     * Enhanced validation for retry requests with detailed error categorization.
     * Uses caching to improve performance for frequently accessed validation data.
     *
     * @param input          the retry input to validate
     * @param authentication the user authentication
     * @return ValidationResult with detailed error information
     */
    public ValidationResult validateRetryOperation(RetryExceptionInput input, Authentication authentication) {
        log.debug("Validating retry request for transaction: {} by user: {}",
                input.getTransactionId(), authentication.getName());

        List<com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError> errors = new ArrayList<>();

        // Validate input format and structure
        validateRetryInputFormat(input, errors);

        // Validate user permissions
        validateUserPermissions(authentication, "retry", errors);

        // If basic validation failed, return early
        if (!errors.isEmpty()) {
            return ValidationResult.failure("retry", input.getTransactionId(), errors);
        }

        // Use cached validation for better performance
        ValidationResult cachedResult = databaseCachingService.validateForOperation(
            input.getTransactionId(), "retry");
        
        if (!cachedResult.isValid()) {
            log.debug("Cached retry validation failed for transaction: {} with {} errors", 
                input.getTransactionId(), cachedResult.getErrors().size());
            return cachedResult;
        }

        log.debug("Retry request validation passed for transaction: {}", input.getTransactionId());
        return cachedResult;
    }

    /**
     * Enhanced validation for acknowledge requests.
     *
     * @param input          the acknowledge input to validate
     * @param authentication the user authentication
     * @return ValidationResult with detailed error information
     */
    public ValidationResult validateAcknowledgeOperation(AcknowledgeExceptionInput input, Authentication authentication) {
        log.debug("Validating acknowledge request for transaction: {} by user: {}",
                input.getTransactionId(), authentication.getName());

        List<com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError> errors = new ArrayList<>();

        // Validate input format
        validateAcknowledgeInputFormat(input, errors);

        // Validate user permissions
        validateUserPermissions(authentication, "acknowledge", errors);

        // If basic validation failed, return early
        if (!errors.isEmpty()) {
            return ValidationResult.failure("acknowledge", input.getTransactionId(), errors);
        }

        // Find and validate the exception
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(input.getTransactionId());
        if (exceptionOpt.isEmpty()) {
            errors.add(GraphQLErrorHandler.createNotFoundError(
                    MutationErrorCode.EXCEPTION_NOT_FOUND, 
                    input.getTransactionId()));
            return ValidationResult.failure("acknowledge", input.getTransactionId(), errors);
        }

        InterfaceException exception = exceptionOpt.get();

        // Validate business rules for acknowledgment
        validateAcknowledgeBusinessRules(exception, errors);

        if (errors.isEmpty()) {
            log.debug("Acknowledge request validation passed for transaction: {}", input.getTransactionId());
            return ValidationResult.success("acknowledge", input.getTransactionId());
        } else {
            log.debug("Acknowledge request validation failed for transaction: {} with {} errors", 
                    input.getTransactionId(), errors.size());
            return ValidationResult.failure("acknowledge", input.getTransactionId(), errors);
        }
    }

    /**
     * Enhanced validation for resolve requests.
     *
     * @param input          the resolve input to validate
     * @param authentication the user authentication
     * @return ValidationResult with detailed error information
     */
    public ValidationResult validateResolveOperation(ResolveExceptionInput input, Authentication authentication) {
        log.debug("Validating resolve request for transaction: {} by user: {}",
                input.getTransactionId(), authentication.getName());

        List<com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError> errors = new ArrayList<>();

        // Validate input format
        validateResolveInputFormat(input, errors);

        // Validate user permissions
        validateUserPermissions(authentication, "resolve", errors);

        // If basic validation failed, return early
        if (!errors.isEmpty()) {
            return ValidationResult.failure("resolve", input.getTransactionId(), errors);
        }

        // Find and validate the exception
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(input.getTransactionId());
        if (exceptionOpt.isEmpty()) {
            errors.add(GraphQLErrorHandler.createNotFoundError(
                    MutationErrorCode.EXCEPTION_NOT_FOUND, 
                    input.getTransactionId()));
            return ValidationResult.failure("resolve", input.getTransactionId(), errors);
        }

        InterfaceException exception = exceptionOpt.get();

        // Validate business rules for resolution
        validateResolveBusinessRules(exception, errors);

        if (errors.isEmpty()) {
            log.debug("Resolve request validation passed for transaction: {}", input.getTransactionId());
            return ValidationResult.success("resolve", input.getTransactionId());
        } else {
            log.debug("Resolve request validation failed for transaction: {} with {} errors", 
                    input.getTransactionId(), errors.size());
            return ValidationResult.failure("resolve", input.getTransactionId(), errors);
        }
    }

    /**
     * Enhanced validation for cancel retry requests.
     *
     * @param transactionId  the transaction ID
     * @param reason         the cancellation reason
     * @param authentication the user authentication
     * @return ValidationResult with detailed error information
     */
    public ValidationResult validateCancelRetryOperation(String transactionId, String reason, Authentication authentication) {
        log.debug("Validating cancel retry request for transaction: {} by user: {}",
                transactionId, authentication.getName());

        List<com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError> errors = new ArrayList<>();

        // Validate input format
        validateCancelRetryInputFormat(transactionId, reason, errors);

        // Validate user permissions
        validateUserPermissions(authentication, "cancel_retry", errors);

        // If basic validation failed, return early
        if (!errors.isEmpty()) {
            return ValidationResult.failure("cancel_retry", transactionId, errors);
        }

        // Find and validate the exception
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
        if (exceptionOpt.isEmpty()) {
            errors.add(GraphQLErrorHandler.createNotFoundError(
                    MutationErrorCode.EXCEPTION_NOT_FOUND, 
                    transactionId));
            return ValidationResult.failure("cancel_retry", transactionId, errors);
        }

        InterfaceException exception = exceptionOpt.get();

        // Validate business rules for cancellation
        validateCancelRetryBusinessRules(exception, errors);

        if (errors.isEmpty()) {
            log.debug("Cancel retry request validation passed for transaction: {}", transactionId);
            return ValidationResult.success("cancel_retry", transactionId);
        } else {
            log.debug("Cancel retry request validation failed for transaction: {} with {} errors", 
                    transactionId, errors.size());
            return ValidationResult.failure("cancel_retry", transactionId, errors);
        }
    }

    /**
     * Enhanced validation for bulk retry requests.
     *
     * @param transactionIds the list of transaction IDs to validate
     * @param authentication the user authentication
     * @return ValidationResult with detailed error information
     */
    public ValidationResult validateBulkRetryRequest(List<String> transactionIds, Authentication authentication) {
        log.debug("Validating bulk retry request for {} transactions by user: {}",
                transactionIds.size(), authentication.getName());

        List<com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError> errors = new ArrayList<>();

        // Validate bulk operation permissions and limits
        validateBulkOperationFormat(transactionIds, authentication, errors);

        if (errors.isEmpty()) {
            log.debug("Bulk retry request validation passed for {} transactions", transactionIds.size());
            return ValidationResult.success("bulk_retry", "bulk_operation");
        } else {
            log.debug("Bulk retry request validation failed with {} errors", errors.size());
            return ValidationResult.failure("bulk_retry", "bulk_operation", errors);
        }
    }

    /**
     * Validates a cancel retry request.
     *
     * @param transactionId  the transaction ID
     * @param authentication the user authentication
     * @throws ExceptionNotFoundException if exception is not found
     * @throws RetryNotAllowedException   if cancel is not allowed
     * @throws SecurityException          if user lacks permissions
     */
    public void validateCancelRetryRequest(String transactionId, Authentication authentication) {
        log.debug("Validating cancel retry request for transaction: {} by user: {}",
                transactionId, authentication.getName());

        // Validate user permissions
        validateUserPermissions(authentication, "cancel_retry");

        // Find the exception
        InterfaceException exception = exceptionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ExceptionNotFoundException(
                        "Exception not found for transaction: " + transactionId));

        // Validate there's a pending retry to cancel
        Optional<RetryAttempt> latestAttempt = retryAttemptRepository
                .findTopByInterfaceExceptionOrderByAttemptNumberDesc(exception);

        if (latestAttempt.isEmpty()) {
            throw new RetryNotAllowedException("No retry attempts found for transaction: " + transactionId);
        }

        if (latestAttempt.get().getStatus() != RetryStatus.PENDING) {
            throw new RetryNotAllowedException(
                    "No pending retry found to cancel for transaction: " + transactionId +
                            ", current status: " + latestAttempt.get().getStatus());
        }

        log.debug("Cancel retry request validation passed for transaction: {}", transactionId);
    }

    /**
     * Validates user permissions for retry operations.
     */
    private void validateUserPermissions(Authentication authentication, String operation) {
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean hasPermission = authorities.contains("ROLE_ADMIN") ||
                authorities.contains("ROLE_OPERATIONS");

        if (!hasPermission) {
            log.warn("User {} lacks permissions for operation: {}, authorities: {}",
                    authentication.getName(), operation, authorities);
            throw new SecurityException("Insufficient permissions for " + operation + " operation");
        }

        // Additional validation for bulk operations
        if ("bulk_retry".equals(operation) && !authorities.contains("ROLE_ADMIN")) {
            // Only admins can perform bulk operations with more than 10 items
            // This check will be done in the calling method
        }

        log.debug("User {} has valid permissions for operation: {}", authentication.getName(), operation);
    }

    /**
     * Validates that an exception is retryable.
     */
    private void validateExceptionRetryable(InterfaceException exception) {
        if (!exception.getRetryable()) {
            throw new RetryNotAllowedException(
                    "Exception is not retryable for transaction: " + exception.getTransactionId());
        }
    }

    /**
     * Validates that no pending retry exists for the exception.
     */
    private void validateNoPendingRetry(InterfaceException exception) {
        Optional<RetryAttempt> latestAttempt = retryAttemptRepository
                .findTopByInterfaceExceptionOrderByAttemptNumberDesc(exception);

        if (latestAttempt.isPresent() && latestAttempt.get().getStatus() == RetryStatus.PENDING) {
            throw new RetryNotAllowedException(
                    "A retry is already pending for transaction: " + exception.getTransactionId());
        }
    }

    /**
     * Validates that the exception status allows retry.
     */
    private void validateExceptionStatus(InterfaceException exception) {
        Set<ExceptionStatus> nonRetryableStatuses = Set.of(
                ExceptionStatus.RESOLVED,
                ExceptionStatus.CLOSED);

        if (nonRetryableStatuses.contains(exception.getStatus())) {
            throw new RetryNotAllowedException(
                    "Exception cannot be retried due to status: " + exception.getStatus() +
                            " for transaction: " + exception.getTransactionId());
        }
    }

    /**
     * Validates that the retry count hasn't exceeded the maximum allowed.
     */
    private void validateRetryCount(InterfaceException exception) {
        if (exception.getRetryCount() >= exception.getMaxRetries()) {
            throw new RetryNotAllowedException(
                    "Maximum retry count (" + exception.getMaxRetries() + ") exceeded for transaction: " +
                            exception.getTransactionId());
        }
    }

    // ========== Input Format Validation Methods ==========

    /**
     * Validates retry input format and structure.
     */
    private void validateRetryInputFormat(RetryExceptionInput input, List<com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError> errors) {
        // Validate transaction ID
        if (!StringUtils.hasText(input.getTransactionId())) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.MISSING_REQUIRED_FIELD, 
                    "transactionId", 
                    "Transaction ID is required"));
        } else if (!TRANSACTION_ID_PATTERN.matcher(input.getTransactionId()).matches()) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.INVALID_TRANSACTION_ID, 
                    "transactionId", 
                    "Transaction ID format is invalid"));
        }

        // Validate reason
        if (!StringUtils.hasText(input.getReason())) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.MISSING_REQUIRED_FIELD, 
                    "reason", 
                    "Reason is required"));
        } else if (input.getReason().length() > MAX_REASON_LENGTH) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.INVALID_REASON_LENGTH, 
                    "reason", 
                    "Reason exceeds maximum length of " + MAX_REASON_LENGTH + " characters"));
        }

        // Validate priority if provided
        if (input.getPriority() != null) {
            try {
                RetryPriority.valueOf(input.getPriority().name());
            } catch (IllegalArgumentException e) {
                errors.add(GraphQLErrorHandler.createValidationError(
                        MutationErrorCode.INVALID_PRIORITY_VALUE, 
                        "priority", 
                        "Invalid retry priority value"));
            }
        }

        // Validate notes if provided
        if (input.getNotes() != null && input.getNotes().length() > MAX_NOTES_LENGTH) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.INVALID_NOTES_LENGTH, 
                    "notes", 
                    "Notes exceed maximum length of " + MAX_NOTES_LENGTH + " characters"));
        }
    }

    /**
     * Validates acknowledge input format and structure.
     */
    private void validateAcknowledgeInputFormat(AcknowledgeExceptionInput input, List<com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError> errors) {
        // Validate transaction ID
        if (!StringUtils.hasText(input.getTransactionId())) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.MISSING_REQUIRED_FIELD, 
                    "transactionId", 
                    "Transaction ID is required"));
        } else if (!TRANSACTION_ID_PATTERN.matcher(input.getTransactionId()).matches()) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.INVALID_TRANSACTION_ID, 
                    "transactionId", 
                    "Transaction ID format is invalid"));
        }

        // Validate reason
        if (!StringUtils.hasText(input.getReason())) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.MISSING_REQUIRED_FIELD, 
                    "reason", 
                    "Reason is required"));
        } else if (input.getReason().length() > MAX_REASON_LENGTH) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.INVALID_REASON_LENGTH, 
                    "reason", 
                    "Reason exceeds maximum length of " + MAX_REASON_LENGTH + " characters"));
        }

        // Validate notes if provided
        if (input.getNotes() != null && input.getNotes().length() > MAX_NOTES_LENGTH) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.INVALID_NOTES_LENGTH, 
                    "notes", 
                    "Notes exceed maximum length of " + MAX_NOTES_LENGTH + " characters"));
        }
    }

    /**
     * Validates resolve input format and structure.
     */
    private void validateResolveInputFormat(ResolveExceptionInput input, List<com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError> errors) {
        // Validate transaction ID
        if (!StringUtils.hasText(input.getTransactionId())) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.MISSING_REQUIRED_FIELD, 
                    "transactionId", 
                    "Transaction ID is required"));
        } else if (!TRANSACTION_ID_PATTERN.matcher(input.getTransactionId()).matches()) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.INVALID_TRANSACTION_ID, 
                    "transactionId", 
                    "Transaction ID format is invalid"));
        }

        // Validate resolution method
        if (input.getResolutionMethod() == null) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.MISSING_REQUIRED_FIELD, 
                    "resolutionMethod", 
                    "Resolution method is required"));
        } else {
            try {
                ResolutionMethod.valueOf(input.getResolutionMethod().name());
            } catch (IllegalArgumentException e) {
                errors.add(GraphQLErrorHandler.createValidationError(
                        MutationErrorCode.INVALID_RESOLUTION_METHOD, 
                        "resolutionMethod", 
                        "Invalid resolution method"));
            }
        }

        // Validate resolution notes if provided
        if (input.getResolutionNotes() != null && input.getResolutionNotes().length() > MAX_NOTES_LENGTH) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.INVALID_NOTES_LENGTH, 
                    "resolutionNotes", 
                    "Resolution notes exceed maximum length of " + MAX_NOTES_LENGTH + " characters"));
        }
    }

    /**
     * Validates cancel retry input format and structure.
     */
    private void validateCancelRetryInputFormat(String transactionId, String reason, List<com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError> errors) {
        // Validate transaction ID
        if (!StringUtils.hasText(transactionId)) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.MISSING_REQUIRED_FIELD, 
                    "transactionId", 
                    "Transaction ID is required"));
        } else if (!TRANSACTION_ID_PATTERN.matcher(transactionId).matches()) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.INVALID_TRANSACTION_ID, 
                    "transactionId", 
                    "Transaction ID format is invalid"));
        }

        // Validate reason
        if (!StringUtils.hasText(reason)) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.MISSING_REQUIRED_FIELD, 
                    "reason", 
                    "Cancellation reason is required"));
        } else if (reason.length() > MAX_REASON_LENGTH) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.INVALID_REASON_LENGTH, 
                    "reason", 
                    "Reason exceeds maximum length of " + MAX_REASON_LENGTH + " characters"));
        }
    }

    /**
     * Validates bulk operation format and permissions.
     */
    private void validateBulkOperationFormat(List<String> transactionIds, Authentication authentication, List<com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError> errors) {
        // Validate list is not empty
        if (transactionIds == null || transactionIds.isEmpty()) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.MISSING_REQUIRED_FIELD, 
                    "transactionIds", 
                    "Transaction IDs list cannot be empty"));
            return;
        }

        // Validate bulk operation size based on user role
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean isAdmin = authorities.contains("ROLE_ADMIN");
        int maxSize = isAdmin ? MAX_BULK_OPERATION_SIZE : MAX_BULK_OPERATION_SIZE_NON_ADMIN;

        if (transactionIds.size() > maxSize) {
            errors.add(GraphQLErrorHandler.createSecurityError(
                    MutationErrorCode.BULK_SIZE_EXCEEDED, 
                    "Bulk operation size exceeds maximum allowed limit of " + maxSize + " for your role"));
            return;
        }

        // Check for duplicates
        Set<String> uniqueIds = Set.copyOf(transactionIds);
        if (uniqueIds.size() != transactionIds.size()) {
            errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.INVALID_FIELD_VALUE, 
                    "transactionIds", 
                    "Duplicate transaction IDs found in bulk operation"));
        }

        // Validate each transaction ID format
        for (int i = 0; i < transactionIds.size(); i++) {
            String transactionId = transactionIds.get(i);
            if (!StringUtils.hasText(transactionId)) {
                errors.add(GraphQLErrorHandler.createValidationError(
                        MutationErrorCode.MISSING_REQUIRED_FIELD, 
                        "transactionIds[" + i + "]", 
                        "Transaction ID at index " + i + " is empty"));
            } else if (!TRANSACTION_ID_PATTERN.matcher(transactionId).matches()) {
                errors.add(GraphQLErrorHandler.createValidationError(
                        MutationErrorCode.INVALID_TRANSACTION_ID, 
                        "transactionIds[" + i + "]", 
                        "Transaction ID at index " + i + " has invalid format"));
            }
        }
    }

    // ========== Business Rule Validation Methods ==========

    /**
     * Validates business rules for retry operations.
     */
    private void validateRetryBusinessRules(InterfaceException exception, List<com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError> errors) {
        // Check if exception is retryable
        if (!exception.getRetryable()) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                    MutationErrorCode.NOT_RETRYABLE, 
                    "Exception is marked as not retryable"));
        }

        // Check exception status
        Set<ExceptionStatus> nonRetryableStatuses = Set.of(
                ExceptionStatus.RESOLVED,
                ExceptionStatus.CLOSED);

        if (nonRetryableStatuses.contains(exception.getStatus())) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                    MutationErrorCode.INVALID_STATUS_TRANSITION, 
                    "Exception cannot be retried due to status: " + exception.getStatus()));
        }

        // Check retry count limits
        if (exception.getRetryCount() >= exception.getMaxRetries()) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                    MutationErrorCode.RETRY_LIMIT_EXCEEDED, 
                    "Maximum retry count (" + exception.getMaxRetries() + ") exceeded"));
        }

        // Check for pending retry
        Optional<RetryAttempt> latestAttempt = retryAttemptRepository
                .findTopByInterfaceExceptionOrderByAttemptNumberDesc(exception);

        if (latestAttempt.isPresent() && latestAttempt.get().getStatus() == RetryStatus.PENDING) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                    MutationErrorCode.PENDING_RETRY_EXISTS, 
                    "A retry operation is already pending for this exception"));
        }
    }

    /**
     * Validates business rules for acknowledge operations.
     */
    private void validateAcknowledgeBusinessRules(InterfaceException exception, List<com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError> errors) {
        // Check if already resolved
        if (exception.getStatus() == ExceptionStatus.RESOLVED) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                    MutationErrorCode.ALREADY_RESOLVED, 
                    "Exception is already resolved and cannot be acknowledged"));
        }

        // Check if already acknowledged
        if (exception.getStatus() == ExceptionStatus.ACKNOWLEDGED) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                    MutationErrorCode.ALREADY_ACKNOWLEDGED, 
                    "Exception is already acknowledged"));
        }

        // Check if closed
        if (exception.getStatus() == ExceptionStatus.CLOSED) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                    MutationErrorCode.INVALID_STATUS_TRANSITION, 
                    "Exception is closed and cannot be acknowledged"));
        }
    }

    /**
     * Validates business rules for resolve operations.
     */
    private void validateResolveBusinessRules(InterfaceException exception, List<com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError> errors) {
        // Check if already resolved
        if (exception.getStatus() == ExceptionStatus.RESOLVED) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                    MutationErrorCode.ALREADY_RESOLVED, 
                    "Exception is already resolved"));
        }

        // Check if closed
        if (exception.getStatus() == ExceptionStatus.CLOSED) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                    MutationErrorCode.INVALID_STATUS_TRANSITION, 
                    "Exception is closed and cannot be resolved"));
        }

        // Check for pending retry (optional business rule - may want to resolve even with pending retry)
        Optional<RetryAttempt> latestAttempt = retryAttemptRepository
                .findTopByInterfaceExceptionOrderByAttemptNumberDesc(exception);

        if (latestAttempt.isPresent() && latestAttempt.get().getStatus() == RetryStatus.PENDING) {
            // This could be a warning rather than an error, depending on business rules
            log.warn("Resolving exception {} with pending retry attempt", exception.getTransactionId());
        }
    }

    /**
     * Validates business rules for cancel retry operations.
     */
    private void validateCancelRetryBusinessRules(InterfaceException exception, List<com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError> errors) {
        // Find the latest retry attempt
        Optional<RetryAttempt> latestAttempt = retryAttemptRepository
                .findTopByInterfaceExceptionOrderByAttemptNumberDesc(exception);

        if (latestAttempt.isEmpty()) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                    MutationErrorCode.NO_PENDING_RETRY, 
                    "No retry attempts found for this exception"));
            return;
        }

        RetryAttempt attempt = latestAttempt.get();

        // Check if retry is in a cancellable state
        if (attempt.getStatus() != RetryStatus.PENDING) {
            if (attempt.getStatus() == RetryStatus.SUCCESS) {
                errors.add(GraphQLErrorHandler.createBusinessRuleError(
                        MutationErrorCode.RETRY_ALREADY_COMPLETED, 
                        "Retry has already completed and cannot be cancelled"));
            } else {
                errors.add(GraphQLErrorHandler.createBusinessRuleError(
                        MutationErrorCode.CANCELLATION_NOT_ALLOWED, 
                        "Retry is not in a cancellable state: " + attempt.getStatus()));
            }
        }
    }

    // ========== Permission Validation Methods ==========

    /**
     * Enhanced user permission validation with detailed error reporting.
     */
    private void validateUserPermissions(Authentication authentication, String operation, List<com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError> errors) {
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean hasPermission = authorities.contains("ROLE_ADMIN") ||
                authorities.contains("ROLE_OPERATIONS");

        if (!hasPermission) {
            log.warn("User {} lacks permissions for operation: {}, authorities: {}",
                    authentication.getName(), operation, authorities);
            errors.add(GraphQLErrorHandler.createSecurityError(
                    MutationErrorCode.INSUFFICIENT_PERMISSIONS, 
                    "Insufficient permissions for " + operation + " operation"));
        }

        // Additional validation for bulk operations
        if (operation.startsWith("bulk_") && !authorities.contains("ROLE_ADMIN")) {
            // This will be checked in bulk operation format validation
            log.debug("Non-admin user {} attempting bulk operation: {}", authentication.getName(), operation);
        }

        log.debug("User {} permission validation for operation: {} - {}", 
                authentication.getName(), operation, hasPermission ? "PASSED" : "FAILED");
    }

    // ========== Legacy Methods (for backward compatibility) ==========

    /**
     * Legacy method for backward compatibility.
     * @deprecated Use validateRetryOperation instead
     */
    @Deprecated
    public void validateRetryRequest(RetryExceptionInput input, Authentication authentication) {
        ValidationResult result = validateRetryOperation(input, authentication);
        if (!result.isValid()) {
            // Convert to legacy exceptions for backward compatibility
            com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError firstError = result.getErrors().get(0);
            if (firstError.getCode().startsWith("BUSINESS_001")) {
                throw new ExceptionNotFoundException(firstError.getMessage());
            } else if (firstError.getCode().startsWith("BUSINESS_") || firstError.getCode().startsWith("RETRY_")) {
                throw new RetryNotAllowedException(firstError.getMessage());
            } else if (firstError.getCode().startsWith("SECURITY_")) {
                throw new SecurityException(firstError.getMessage());
            } else {
                throw new IllegalArgumentException(firstError.getMessage());
            }
        }
    }

    /**
     * Legacy method for backward compatibility.
     * @deprecated Use validateRetryOperation instead
     */
    @Deprecated
    public void validateRetryRequestLegacy(RetryExceptionInput input, Authentication authentication) {
        ValidationResult result = validateRetryOperation(input, authentication);
        if (!result.isValid()) {
            // Convert to legacy exceptions for backward compatibility
            com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError firstError = result.getErrors().get(0);
            if (firstError.getCode().startsWith("BUSINESS_001")) {
                throw new ExceptionNotFoundException(firstError.getMessage());
            } else if (firstError.getCode().startsWith("BUSINESS_") || firstError.getCode().startsWith("RETRY_")) {
                throw new RetryNotAllowedException(firstError.getMessage());
            } else if (firstError.getCode().startsWith("SECURITY_")) {
                throw new SecurityException(firstError.getMessage());
            } else {
                throw new IllegalArgumentException(firstError.getMessage());
            }
        }
    }

    /**
     * Legacy method for backward compatibility.
     * @deprecated Use validateBulkRetryRequest instead
     */
    @Deprecated
    public void validateBulkOperationSize(int size, Authentication authentication) {
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (!authorities.contains("ROLE_ADMIN") && size > MAX_BULK_OPERATION_SIZE_NON_ADMIN) {
            throw new SecurityException("Non-admin users can only perform bulk operations on up to " + 
                    MAX_BULK_OPERATION_SIZE_NON_ADMIN + " items");
        }
    }
}