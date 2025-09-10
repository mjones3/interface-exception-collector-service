package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError;
import com.arcone.biopro.exception.collector.api.graphql.validation.GraphQLErrorHandler;
import com.arcone.biopro.exception.collector.api.graphql.validation.MutationErrorCode;
import com.arcone.biopro.exception.collector.api.graphql.validation.ValidationResult;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
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
 * Enhanced validation service for GraphQL acknowledgment operations.
 * Implements business rules and security validations for acknowledgment requests
 * with standardized error handling and detailed error categorization.
 * Supports requirements 2.2, 2.4, 6.2, 6.3, 6.4, and 7.3.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AcknowledgmentValidationService {

    private final InterfaceExceptionRepository exceptionRepository;

    // Validation constants
    private static final int MAX_REASON_LENGTH = 500;
    private static final int MAX_NOTES_LENGTH = 1000;
    private static final int MAX_BULK_ACKNOWLEDGMENT_SIZE = 100;
    private static final int MAX_BULK_ACKNOWLEDGMENT_SIZE_NON_ADMIN = 10;
    private static final Pattern TRANSACTION_ID_PATTERN = Pattern.compile("^[A-Za-z0-9\\-_]{1,50}$");

    /**
     * Enhanced validation for acknowledgment requests with detailed error categorization.
     *
     * @param input          the acknowledgment input to validate
     * @param authentication the user authentication context
     * @return ValidationResult with detailed error information
     */
    public ValidationResult validateAcknowledgmentOperation(AcknowledgeExceptionInput input, Authentication authentication) {
        log.debug("Validating acknowledgment request for transaction: {} by user: {}",
                input.getTransactionId(), authentication.getName());

        List<GraphQLError> errors = new ArrayList<>();

        // Validate input format and structure
        validateAcknowledgmentInputFormat(input, errors);

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
        validateAcknowledgmentBusinessRules(exception, errors);

        if (errors.isEmpty()) {
            log.debug("Acknowledgment request validation passed for transaction: {}", input.getTransactionId());
            return ValidationResult.success("acknowledge", input.getTransactionId());
        } else {
            log.debug("Acknowledgment request validation failed for transaction: {} with {} errors", 
                    input.getTransactionId(), errors.size());
            return ValidationResult.failure("acknowledge", input.getTransactionId(), errors);
        }
    }

    /**
     * Legacy validation method for backward compatibility.
     * @deprecated Use validateAcknowledgmentOperation instead
     */
    @Deprecated
    public void validateAcknowledgmentRequest(AcknowledgeExceptionInput input, Authentication authentication) {
        ValidationResult result = validateAcknowledgmentOperation(input, authentication);
        if (!result.isValid()) {
            // Convert to legacy exceptions for backward compatibility
            GraphQLError firstError = result.getErrors().get(0);
            throw new IllegalArgumentException(firstError.getMessage());
        }
    }

    /**
     * Enhanced validation for bulk acknowledgment requests.
     *
     * @param transactionIds the list of transaction IDs to acknowledge
     * @param authentication the user authentication context
     * @return ValidationResult with detailed error information
     */
    public ValidationResult validateBulkAcknowledgmentOperation(List<String> transactionIds, Authentication authentication) {
        log.debug("Validating bulk acknowledgment request for {} transactions by user: {}",
                transactionIds.size(), authentication.getName());

        List<GraphQLError> errors = new ArrayList<>();

        // Validate bulk operation format and permissions
        validateBulkOperationFormat(transactionIds, authentication, errors);

        if (errors.isEmpty()) {
            log.debug("Bulk acknowledgment request validation passed for {} transactions", transactionIds.size());
            return ValidationResult.success("bulk_acknowledge", "bulk_operation");
        } else {
            log.debug("Bulk acknowledgment request validation failed with {} errors", errors.size());
            return ValidationResult.failure("bulk_acknowledge", "bulk_operation", errors);
        }
    }

    /**
     * Legacy validation method for backward compatibility.
     * @deprecated Use validateBulkAcknowledgmentOperation instead
     */
    @Deprecated
    public void validateBulkAcknowledgmentRequest(List<String> transactionIds, Authentication authentication) {
        ValidationResult result = validateBulkAcknowledgmentOperation(transactionIds, authentication);
        if (!result.isValid()) {
            // Convert to legacy exceptions for backward compatibility
            GraphQLError firstError = result.getErrors().get(0);
            throw new IllegalArgumentException(firstError.getMessage());
        }
    }

    /**
     * Legacy validation method for backward compatibility.
     * @deprecated Use validateBulkAcknowledgmentOperation instead
     */
    @Deprecated
    public void validateBulkOperationSize(int operationSize, Authentication authentication) {
        List<String> dummyIds = new ArrayList<>();
        for (int i = 0; i < operationSize; i++) {
            dummyIds.add("dummy-" + i);
        }
        ValidationResult result = validateBulkAcknowledgmentOperation(dummyIds, authentication);
        if (!result.isValid()) {
            GraphQLError firstError = result.getErrors().get(0);
            throw new IllegalArgumentException(firstError.getMessage());
        }
    }

    // ========== Input Format Validation Methods ==========

    /**
     * Validates acknowledgment input format and structure.
     */
    private void validateAcknowledgmentInputFormat(AcknowledgeExceptionInput input, List<GraphQLError> errors) {
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
                    "Acknowledgment reason is required"));
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
     * Validates bulk operation format and permissions.
     */
    private void validateBulkOperationFormat(List<String> transactionIds, Authentication authentication, List<GraphQLError> errors) {
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
        int maxSize = isAdmin ? MAX_BULK_ACKNOWLEDGMENT_SIZE : MAX_BULK_ACKNOWLEDGMENT_SIZE_NON_ADMIN;

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
     * Validates business rules for acknowledgment operations.
     */
    private void validateAcknowledgmentBusinessRules(InterfaceException exception, List<GraphQLError> errors) {
        // Check if already resolved
        if (exception.getStatus() == ExceptionStatus.RESOLVED) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                    MutationErrorCode.ALREADY_RESOLVED, 
                    "Exception is already resolved and cannot be acknowledged"));
        }

        // Check if closed
        if (exception.getStatus() == ExceptionStatus.CLOSED) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                    MutationErrorCode.INVALID_STATUS_TRANSITION, 
                    "Exception is closed and cannot be acknowledged"));
        }

        // Allow re-acknowledgment but log it
        if (exception.getStatus() == ExceptionStatus.ACKNOWLEDGED) {
            log.info("Re-acknowledging exception {} that is already acknowledged by: {}", 
                    exception.getTransactionId(), exception.getAcknowledgedBy());
        }

        // Validate exception is not too old (business rule)
        if (exception.getCreatedAt() != null) {
            long daysSinceCreation = java.time.Duration.between(
                exception.getCreatedAt().toInstant(), 
                java.time.Instant.now()
            ).toDays();
            
            if (daysSinceCreation > 90) {
                log.warn("Acknowledging very old exception: {} (created {} days ago)", 
                        exception.getTransactionId(), daysSinceCreation);
                // Allow but log warning for audit purposes
            }
        }
    }

    // ========== Permission Validation Methods ==========

    /**
     * Enhanced user permission validation with detailed error reporting.
     */
    private void validateUserPermissions(Authentication authentication, String operation, List<GraphQLError> errors) {
        if (authentication == null || authentication.getName() == null) {
            errors.add(GraphQLErrorHandler.createSecurityError(
                    MutationErrorCode.INSUFFICIENT_PERMISSIONS, 
                    "User authentication is required"));
            return;
        }

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
     * @deprecated Use validateAcknowledgmentOperation instead
     */
    @Deprecated
    private void validateExceptionState(String transactionId) {
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
        
        if (exceptionOpt.isEmpty()) {
            throw new IllegalArgumentException("Exception not found with transaction ID: " + transactionId);
        }

        InterfaceException exception = exceptionOpt.get();
        List<GraphQLError> errors = new ArrayList<>();
        validateAcknowledgmentBusinessRules(exception, errors);
        
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.get(0).getMessage());
        }
    }

    /**
     * Legacy method for backward compatibility.
     * @deprecated Use validateUserPermissions with error list instead
     */
    @Deprecated
    private void validateUserPermissions(Authentication authentication, String operation) {
        List<GraphQLError> errors = new ArrayList<>();
        validateUserPermissions(authentication, operation, errors);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.get(0).getMessage());
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