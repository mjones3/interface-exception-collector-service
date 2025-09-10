package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError;
import com.arcone.biopro.exception.collector.api.graphql.dto.ResolveExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.validation.GraphQLErrorHandler;
import com.arcone.biopro.exception.collector.api.graphql.validation.MutationErrorCode;
import com.arcone.biopro.exception.collector.api.graphql.validation.ValidationResult;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service for validating resolution operations with enhanced business rule validation
 * and proper state transition checking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResolutionValidationService {

    private final InterfaceExceptionRepository exceptionRepository;

    /**
     * Valid statuses that can be resolved.
     */
    private static final Set<ExceptionStatus> RESOLVABLE_STATUSES = Set.of(
        ExceptionStatus.NEW,
        ExceptionStatus.ACKNOWLEDGED,
        ExceptionStatus.RETRIED_FAILED,
        ExceptionStatus.ESCALATED
    );

    /**
     * Valid resolution methods for different scenarios.
     */
    private static final Set<ResolutionMethod> VALID_RESOLUTION_METHODS = Set.of(
        ResolutionMethod.RETRY_SUCCESS,
        ResolutionMethod.MANUAL_RESOLUTION,
        ResolutionMethod.CUSTOMER_RESOLVED,
        ResolutionMethod.AUTOMATED
    );

    /**
     * Validates a resolution operation with enhanced business rule checking.
     *
     * @param input          the resolution input to validate
     * @param authentication the current user authentication
     * @return ValidationResult with detailed error information
     */
    public ValidationResult validateResolutionOperation(ResolveExceptionInput input, Authentication authentication) {
        log.debug("Validating resolution operation for transaction: {}", input.getTransactionId());

        List<GraphQLError> errors = new ArrayList<>();

        // Basic input validation
        validateBasicInput(input, errors);

        // If basic validation fails, return early
        if (!errors.isEmpty()) {
            return ValidationResult.failure("resolve", input.getTransactionId(), errors);
        }

        // Find and validate exception exists
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(input.getTransactionId());
        if (exceptionOpt.isEmpty()) {
            errors.add(GraphQLErrorHandler.createNotFoundError(
                MutationErrorCode.EXCEPTION_NOT_FOUND, 
                "Transaction ID: " + input.getTransactionId()
            ));
            return ValidationResult.failure("resolve", input.getTransactionId(), errors);
        }

        InterfaceException exception = exceptionOpt.get();

        // Validate exception state and business rules
        validateExceptionState(exception, input, errors);
        validateResolutionMethod(input.getResolutionMethod(), exception, errors);
        validateStateTransition(exception, errors);

        boolean isValid = errors.isEmpty();
        log.debug("Resolution validation completed for transaction: {}, valid: {}, errors: {}", 
                 input.getTransactionId(), isValid, errors.size());

        return isValid 
            ? ValidationResult.success("resolve", input.getTransactionId())
            : ValidationResult.failure("resolve", input.getTransactionId(), errors);
    }

    /**
     * Validates basic input parameters.
     */
    private void validateBasicInput(ResolveExceptionInput input, List<GraphQLError> errors) {
        // Transaction ID validation
        if (input.getTransactionId() == null || input.getTransactionId().trim().isEmpty()) {
            errors.add(GraphQLErrorHandler.createValidationError(
                MutationErrorCode.MISSING_REQUIRED_FIELD, 
                "transactionId", 
                "Transaction ID is required"
            ));
        } else if (input.getTransactionId().length() > 255) {
            errors.add(GraphQLErrorHandler.createValidationError(
                MutationErrorCode.INVALID_FIELD_VALUE, 
                "transactionId", 
                "Transaction ID must not exceed 255 characters"
            ));
        }

        // Resolution method validation
        if (input.getResolutionMethod() == null) {
            errors.add(GraphQLErrorHandler.createValidationError(
                MutationErrorCode.MISSING_REQUIRED_FIELD, 
                "resolutionMethod", 
                "Resolution method is required"
            ));
        }

        // Resolution notes validation (optional field)
        if (input.getResolutionNotes() != null && input.getResolutionNotes().length() > 2000) {
            errors.add(GraphQLErrorHandler.createValidationError(
                MutationErrorCode.INVALID_NOTES_LENGTH, 
                "resolutionNotes", 
                "Resolution notes must not exceed 2000 characters"
            ));
        }
    }

    /**
     * Validates the exception state for resolution eligibility.
     */
    private void validateExceptionState(InterfaceException exception, ResolveExceptionInput input, List<GraphQLError> errors) {
        ExceptionStatus currentStatus = exception.getStatus();

        // Check if exception is already resolved
        if (currentStatus == ExceptionStatus.RESOLVED) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                MutationErrorCode.ALREADY_RESOLVED,
                "Exception is already resolved and cannot be resolved again"
            ));
            return;
        }

        // Check if exception is closed
        if (currentStatus == ExceptionStatus.CLOSED) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                MutationErrorCode.INVALID_EXCEPTION_STATE,
                "Exception is closed and cannot be resolved"
            ));
            return;
        }

        // Check if exception is in a resolvable state
        if (!RESOLVABLE_STATUSES.contains(currentStatus)) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                MutationErrorCode.INVALID_STATUS_TRANSITION,
                String.format("Exception with status '%s' cannot be resolved. Valid statuses are: %s", 
                             currentStatus, RESOLVABLE_STATUSES)
            ));
        }

        // Special validation for RETRIED_SUCCESS status
        if (currentStatus == ExceptionStatus.RETRIED_SUCCESS && 
            input.getResolutionMethod() != ResolutionMethod.RETRY_SUCCESS) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                MutationErrorCode.INVALID_RESOLUTION_DATA,
                "Exception with RETRIED_SUCCESS status must use RETRY_SUCCESS resolution method"
            ));
        }
    }

    /**
     * Validates the resolution method is appropriate for the exception context.
     */
    private void validateResolutionMethod(ResolutionMethod resolutionMethod, InterfaceException exception, List<GraphQLError> errors) {
        if (resolutionMethod == null) {
            return; // Already validated in basic input validation
        }

        // Check if resolution method is valid
        if (!VALID_RESOLUTION_METHODS.contains(resolutionMethod)) {
            errors.add(GraphQLErrorHandler.createValidationError(
                MutationErrorCode.INVALID_RESOLUTION_METHOD,
                "resolutionMethod",
                String.format("Invalid resolution method '%s'. Valid methods are: %s", 
                             resolutionMethod, VALID_RESOLUTION_METHODS)
            ));
            return;
        }

        ExceptionStatus currentStatus = exception.getStatus();

        // Validate resolution method against current status
        switch (resolutionMethod) {
            case RETRY_SUCCESS:
                if (currentStatus != ExceptionStatus.RETRIED_SUCCESS) {
                    errors.add(GraphQLErrorHandler.createBusinessRuleError(
                        MutationErrorCode.INVALID_RESOLUTION_DATA,
                        "RETRY_SUCCESS resolution method can only be used for exceptions with RETRIED_SUCCESS status"
                    ));
                }
                break;

            case MANUAL_RESOLUTION:
                // Manual resolution is valid for most states, but not for RETRIED_SUCCESS
                if (currentStatus == ExceptionStatus.RETRIED_SUCCESS) {
                    errors.add(GraphQLErrorHandler.createBusinessRuleError(
                        MutationErrorCode.INVALID_RESOLUTION_DATA,
                        "MANUAL_RESOLUTION cannot be used for exceptions with RETRIED_SUCCESS status"
                    ));
                }
                break;

            case CUSTOMER_RESOLVED:
                // Customer resolved is valid for acknowledged or escalated exceptions
                if (currentStatus != ExceptionStatus.ACKNOWLEDGED && currentStatus != ExceptionStatus.ESCALATED) {
                    errors.add(GraphQLErrorHandler.createBusinessRuleError(
                        MutationErrorCode.INVALID_RESOLUTION_DATA,
                        "CUSTOMER_RESOLVED can only be used for acknowledged or escalated exceptions"
                    ));
                }
                break;

            case AUTOMATED:
                // Automated resolution should not be used through manual GraphQL operations
                log.warn("AUTOMATED resolution method used through GraphQL for transaction: {}", exception.getTransactionId());
                break;

            default:
                errors.add(GraphQLErrorHandler.createValidationError(
                    MutationErrorCode.INVALID_RESOLUTION_METHOD,
                    "resolutionMethod",
                    "Unsupported resolution method: " + resolutionMethod
                ));
        }
    }

    /**
     * Validates that the state transition is valid.
     */
    private void validateStateTransition(InterfaceException exception, List<GraphQLError> errors) {
        ExceptionStatus currentStatus = exception.getStatus();

        // Check for concurrent modifications by verifying the exception hasn't changed
        // This is a simplified check - in a real system you might use optimistic locking
        if (currentStatus == ExceptionStatus.RESOLVED || currentStatus == ExceptionStatus.CLOSED) {
            errors.add(GraphQLErrorHandler.createBusinessRuleError(
                MutationErrorCode.CONCURRENT_MODIFICATION,
                "Exception status has changed since validation began. Please refresh and try again."
            ));
        }
    }

    /**
     * Checks if an exception can be resolved based on its current state.
     *
     * @param transactionId the transaction ID to check
     * @return true if the exception can be resolved
     */
    public boolean canResolve(String transactionId) {
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
        if (exceptionOpt.isEmpty()) {
            return false;
        }

        InterfaceException exception = exceptionOpt.get();
        ExceptionStatus status = exception.getStatus();

        return RESOLVABLE_STATUSES.contains(status);
    }

    /**
     * Gets the valid resolution methods for a given exception status.
     *
     * @param status the exception status
     * @return set of valid resolution methods
     */
    public Set<ResolutionMethod> getValidResolutionMethods(ExceptionStatus status) {
        switch (status) {
            case RETRIED_SUCCESS:
                return Set.of(ResolutionMethod.RETRY_SUCCESS);
            case ACKNOWLEDGED:
            case ESCALATED:
                return Set.of(ResolutionMethod.MANUAL_RESOLUTION, ResolutionMethod.CUSTOMER_RESOLVED);
            case NEW:
            case RETRIED_FAILED:
                return Set.of(ResolutionMethod.MANUAL_RESOLUTION);
            default:
                return Set.of();
        }
    }
}