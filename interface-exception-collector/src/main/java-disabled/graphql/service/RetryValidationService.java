package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.domain.exception.ExceptionNotFoundException;
import com.arcone.biopro.exception.collector.domain.exception.RetryNotAllowedException;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for validating retry operations and user permissions.
 * Ensures that retry requests are valid and users have appropriate permissions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RetryValidationService {

    private final InterfaceExceptionRepository exceptionRepository;
    private final RetryAttemptRepository retryAttemptRepository;

    /**
     * Validates a retry request for a single exception.
     *
     * @param input          the retry input to validate
     * @param authentication the user authentication
     * @throws ExceptionNotFoundException if exception is not found
     * @throws RetryNotAllowedException   if retry is not allowed
     * @throws SecurityException          if user lacks permissions
     */
    public void validateRetryRequest(RetryExceptionInput input, Authentication authentication) {
        log.debug("Validating retry request for transaction: {} by user: {}",
                input.getTransactionId(), authentication.getName());

        // Validate user permissions
        validateUserPermissions(authentication, "retry");

        // Find the exception
        InterfaceException exception = exceptionRepository.findByTransactionId(input.getTransactionId())
                .orElseThrow(() -> new ExceptionNotFoundException(
                        "Exception not found for transaction: " + input.getTransactionId()));

        // Validate exception is retryable
        validateExceptionRetryable(exception);

        // Validate no pending retry exists
        validateNoPendingRetry(exception);

        // Validate exception status allows retry
        validateExceptionStatus(exception);

        // Validate retry count hasn't exceeded maximum
        validateRetryCount(exception);

        log.debug("Retry request validation passed for transaction: {}", input.getTransactionId());
    }

    /**
     * Validates a bulk retry request.
     *
     * @param transactionIds the list of transaction IDs to validate
     * @param authentication the user authentication
     * @throws SecurityException if user lacks permissions
     */
    public void validateBulkRetryRequest(java.util.List<String> transactionIds, Authentication authentication) {
        log.debug("Validating bulk retry request for {} transactions by user: {}",
                transactionIds.size(), authentication.getName());

        // Validate user permissions for bulk operations
        validateUserPermissions(authentication, "bulk_retry");

        // Validate bulk operation limits
        if (transactionIds.size() > 100) {
            throw new IllegalArgumentException("Cannot retry more than 100 exceptions in a single bulk operation");
        }

        if (transactionIds.isEmpty()) {
            throw new IllegalArgumentException("Transaction IDs list cannot be empty");
        }

        // Check for duplicates
        Set<String> uniqueIds = Set.copyOf(transactionIds);
        if (uniqueIds.size() != transactionIds.size()) {
            throw new IllegalArgumentException("Duplicate transaction IDs found in bulk retry request");
        }

        log.debug("Bulk retry request validation passed for {} transactions", transactionIds.size());
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

    /**
     * Validates bulk operation size based on user role.
     */
    public void validateBulkOperationSize(int size, Authentication authentication) {
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (!authorities.contains("ROLE_ADMIN") && size > 10) {
            throw new SecurityException("Non-admin users can only perform bulk operations on up to 10 items");
        }
    }
}