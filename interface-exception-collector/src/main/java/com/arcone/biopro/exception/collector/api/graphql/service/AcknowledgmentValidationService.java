package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionInput;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Validation service for GraphQL acknowledgment operations.
 * Implements business rules and security validations for acknowledgment requests.
 * Supports requirements 3.4, 3.5, 6.4, and 7.1.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AcknowledgmentValidationService {

    private final InterfaceExceptionRepository exceptionRepository;

    // Maximum number of exceptions that can be acknowledged in a single bulk operation
    private static final int MAX_BULK_ACKNOWLEDGMENT_SIZE = 100;

    /**
     * Validates a single acknowledgment request.
     * Checks input validation, business rules, and user permissions.
     *
     * @param input the acknowledgment input to validate
     * @param authentication the user authentication context
     * @throws IllegalArgumentException if validation fails
     */
    public void validateAcknowledgmentRequest(AcknowledgeExceptionInput input, Authentication authentication) {
        log.debug("Validating acknowledgment request for transaction: {} by user: {}",
                input.getTransactionId(), authentication.getName());

        // Validate input parameters
        validateInputParameters(input);

        // Validate user permissions
        validateUserPermissions(authentication, "acknowledge exceptions");

        // Validate exception exists and can be acknowledged
        validateExceptionState(input.getTransactionId());

        // Validate assignment if specified
        if (input.getAssignedTo() != null && !input.getAssignedTo().trim().isEmpty()) {
            validateAssignment(input.getAssignedTo(), authentication);
        }

        log.debug("Acknowledgment request validation passed for transaction: {}", input.getTransactionId());
    }

    /**
     * Validates a bulk acknowledgment request.
     * Checks bulk operation limits and individual exception states.
     *
     * @param transactionIds the list of transaction IDs to acknowledge
     * @param authentication the user authentication context
     * @throws IllegalArgumentException if validation fails
     */
    public void validateBulkAcknowledgmentRequest(List<String> transactionIds, Authentication authentication) {
        log.debug("Validating bulk acknowledgment request for {} transactions by user: {}",
                transactionIds.size(), authentication.getName());

        // Validate transaction IDs list
        if (transactionIds == null || transactionIds.isEmpty()) {
            throw new IllegalArgumentException("Transaction IDs list cannot be empty");
        }

        // Check for duplicates
        long uniqueCount = transactionIds.stream().distinct().count();
        if (uniqueCount != transactionIds.size()) {
            throw new IllegalArgumentException("Duplicate transaction IDs found in bulk acknowledgment request");
        }

        // Validate each transaction ID format
        for (String transactionId : transactionIds) {
            if (transactionId == null || transactionId.trim().isEmpty()) {
                throw new IllegalArgumentException("Transaction ID cannot be null or empty");
            }
            if (transactionId.length() > 255) {
                throw new IllegalArgumentException("Transaction ID exceeds maximum length: " + transactionId);
            }
        }

        // Validate user permissions for bulk operations
        validateUserPermissions(authentication, "perform bulk acknowledgment operations");

        log.debug("Bulk acknowledgment request validation passed for {} transactions", transactionIds.size());
    }

    /**
     * Validates the size of bulk operations based on user permissions.
     *
     * @param operationSize the number of items in the bulk operation
     * @param authentication the user authentication context
     * @throws IllegalArgumentException if the operation size exceeds limits
     */
    public void validateBulkOperationSize(int operationSize, Authentication authentication) {
        if (operationSize > MAX_BULK_ACKNOWLEDGMENT_SIZE) {
            throw new IllegalArgumentException(
                    String.format("Bulk acknowledgment size (%d) exceeds maximum allowed (%d)",
                            operationSize, MAX_BULK_ACKNOWLEDGMENT_SIZE));
        }

        // Additional size restrictions for non-admin users
        if (!hasRole(authentication, "ADMIN") && operationSize > 50) {
            throw new IllegalArgumentException(
                    "Non-admin users cannot acknowledge more than 50 exceptions at once");
        }
    }

    /**
     * Validates input parameters for acknowledgment requests.
     *
     * @param input the acknowledgment input to validate
     * @throws IllegalArgumentException if input validation fails
     */
    private void validateInputParameters(AcknowledgeExceptionInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Acknowledgment input cannot be null");
        }

        if (input.getTransactionId() == null || input.getTransactionId().trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID is required");
        }

        if (input.getTransactionId().length() > 255) {
            throw new IllegalArgumentException("Transaction ID exceeds maximum length");
        }

        if (input.getReason() == null || input.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Acknowledgment reason is required");
        }

        if (input.getReason().length() > 1000) {
            throw new IllegalArgumentException("Acknowledgment reason exceeds maximum length (1000 characters)");
        }

        if (input.getNotes() != null && input.getNotes().length() > 2000) {
            throw new IllegalArgumentException("Acknowledgment notes exceed maximum length (2000 characters)");
        }

        if (input.getAssignedTo() != null && input.getAssignedTo().length() > 255) {
            throw new IllegalArgumentException("Assigned user ID exceeds maximum length");
        }
    }

    /**
     * Validates user permissions for acknowledgment operations.
     *
     * @param authentication the user authentication context
     * @param operation the operation being performed (for error messages)
     * @throws IllegalArgumentException if user lacks required permissions
     */
    private void validateUserPermissions(Authentication authentication, String operation) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("User authentication is required");
        }

        // Check if user has required roles
        if (!hasRole(authentication, "OPERATIONS") && !hasRole(authentication, "ADMIN")) {
            throw new IllegalArgumentException(
                    String.format("User '%s' does not have permission to %s",
                            authentication.getName(), operation));
        }
    }

    /**
     * Validates that an exception exists and can be acknowledged.
     *
     * @param transactionId the transaction ID to validate
     * @throws IllegalArgumentException if the exception cannot be acknowledged
     */
    private void validateExceptionState(String transactionId) {
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
        
        if (exceptionOpt.isEmpty()) {
            throw new IllegalArgumentException("Exception not found with transaction ID: " + transactionId);
        }

        InterfaceException exception = exceptionOpt.get();
        ExceptionStatus status = exception.getStatus();

        // Check if exception is in a state that allows acknowledgment
        if (status == ExceptionStatus.RESOLVED) {
            throw new IllegalArgumentException(
                    "Cannot acknowledge exception - it has already been resolved");
        }

        if (status == ExceptionStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Cannot acknowledge exception - it has been closed");
        }

        // Check if already acknowledged by the same user
        if (status == ExceptionStatus.ACKNOWLEDGED && exception.getAcknowledgedBy() != null) {
            log.info("Exception {} is already acknowledged by user: {}", 
                    transactionId, exception.getAcknowledgedBy());
            // Allow re-acknowledgment to update notes or assignment
        }
    }

    /**
     * Validates assignment of exceptions to users.
     *
     * @param assignedTo the user ID to assign the exception to
     * @param authentication the current user authentication context
     * @throws IllegalArgumentException if assignment validation fails
     */
    private void validateAssignment(String assignedTo, Authentication authentication) {
        // Basic validation - in a real system, you might check if the assigned user exists
        if (assignedTo.trim().isEmpty()) {
            throw new IllegalArgumentException("Assigned user ID cannot be empty");
        }

        // Non-admin users can only assign to themselves
        if (!hasRole(authentication, "ADMIN") && !assignedTo.equals(authentication.getName())) {
            throw new IllegalArgumentException(
                    "Non-admin users can only assign exceptions to themselves");
        }
    }

    /**
     * Checks if the authenticated user has a specific role.
     *
     * @param authentication the user authentication context
     * @param role the role to check for
     * @return true if the user has the role, false otherwise
     */
    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_" + role) || authority.equals(role));
    }
}