package com.arcone.biopro.exception.collector.api.graphql.validation;

import com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError;
import com.arcone.biopro.exception.collector.api.graphql.exception.GraphQLErrorType;
import com.arcone.biopro.exception.collector.api.graphql.security.RateLimitExceededException;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for creating standardized GraphQL errors with consistent formatting
 * and error extensions for better client error handling and debugging.
 */
public class GraphQLErrorHandler {

    /**
     * Creates a business rule error with specific error code and message.
     *
     * @param errorCode the specific mutation error code
     * @param message   the error message (can override default)
     * @return GraphQLError with business rule classification
     */
    public static GraphQLError createBusinessRuleError(MutationErrorCode errorCode, String message) {
        return GraphQLError.builder()
                .message(message != null ? message : errorCode.getDefaultMessage())
                .code(errorCode.getCode())
                .extensions(createErrorExtensions(errorCode, GraphQLErrorType.BUSINESS_RULE_ERROR))
                .build();
    }

    /**
     * Creates a business rule error with default message.
     *
     * @param errorCode the specific mutation error code
     * @return GraphQLError with business rule classification
     */
    public static GraphQLError createBusinessRuleError(MutationErrorCode errorCode) {
        return createBusinessRuleError(errorCode, null);
    }

    /**
     * Creates a validation error with specific field information.
     *
     * @param errorCode the specific mutation error code
     * @param field     the field that failed validation
     * @param message   the error message (can override default)
     * @return GraphQLError with validation classification
     */
    public static GraphQLError createValidationError(MutationErrorCode errorCode, String field, String message) {
        GraphQLError.GraphQLErrorBuilder builder = GraphQLError.builder()
                .message(message != null ? message : errorCode.getDefaultMessage())
                .code(errorCode.getCode())
                .extensions(createErrorExtensions(errorCode, GraphQLErrorType.VALIDATION_ERROR));

        if (field != null) {
            builder.path(field);
        }

        return builder.build();
    }

    /**
     * Creates a validation error with default message.
     *
     * @param errorCode the specific mutation error code
     * @param field     the field that failed validation
     * @return GraphQLError with validation classification
     */
    public static GraphQLError createValidationError(MutationErrorCode errorCode, String field) {
        return createValidationError(errorCode, field, null);
    }

    /**
     * Creates a security/authorization error.
     *
     * @param errorCode the specific mutation error code
     * @param message   the error message (can override default)
     * @return GraphQLError with authorization classification
     */
    public static GraphQLError createSecurityError(MutationErrorCode errorCode, String message) {
        return GraphQLError.builder()
                .message(message != null ? message : errorCode.getDefaultMessage())
                .code(errorCode.getCode())
                .extensions(createErrorExtensions(errorCode, GraphQLErrorType.AUTHORIZATION_ERROR))
                .build();
    }

    /**
     * Creates a security/authorization error with default message.
     *
     * @param errorCode the specific mutation error code
     * @return GraphQLError with authorization classification
     */
    public static GraphQLError createSecurityError(MutationErrorCode errorCode) {
        return createSecurityError(errorCode, null);
    }

    /**
     * Creates a system/internal error.
     *
     * @param errorCode the specific mutation error code
     * @param message   the error message (can override default)
     * @return GraphQLError with internal error classification
     */
    public static GraphQLError createSystemError(MutationErrorCode errorCode, String message) {
        return GraphQLError.builder()
                .message(message != null ? message : errorCode.getDefaultMessage())
                .code(errorCode.getCode())
                .extensions(createErrorExtensions(errorCode, GraphQLErrorType.INTERNAL_ERROR))
                .build();
    }

    /**
     * Creates a system/internal error with default message.
     *
     * @param errorCode the specific mutation error code
     * @return GraphQLError with internal error classification
     */
    public static GraphQLError createSystemError(MutationErrorCode errorCode) {
        return createSystemError(errorCode, null);
    }

    /**
     * Creates a not found error.
     *
     * @param errorCode the specific mutation error code
     * @param resource  the resource that was not found
     * @return GraphQLError with not found classification
     */
    public static GraphQLError createNotFoundError(MutationErrorCode errorCode, String resource) {
        String message = resource != null 
            ? errorCode.getDefaultMessage() + ": " + resource
            : errorCode.getDefaultMessage();

        return GraphQLError.builder()
                .message(message)
                .code(errorCode.getCode())
                .extensions(createErrorExtensions(errorCode, GraphQLErrorType.NOT_FOUND))
                .build();
    }

    /**
     * Creates a concurrent modification error.
     *
     * @param errorCode the specific mutation error code
     * @param message   the error message (can override default)
     * @return GraphQLError with concurrent modification classification
     */
    public static GraphQLError createConcurrentModificationError(MutationErrorCode errorCode, String message) {
        return GraphQLError.builder()
                .message(message != null ? message : errorCode.getDefaultMessage())
                .code(errorCode.getCode())
                .extensions(createErrorExtensions(errorCode, GraphQLErrorType.CONCURRENT_MODIFICATION_ERROR))
                .build();
    }

    /**
     * Creates error extensions with metadata for debugging and client handling.
     *
     * @param errorCode the mutation error code
     * @param errorType the GraphQL error type
     * @return Map of error extensions
     */
    private static Map<String, Object> createErrorExtensions(MutationErrorCode errorCode, GraphQLErrorType errorType) {
        Map<String, Object> extensions = new HashMap<>();
        
        extensions.put("errorCode", errorCode.getCode());
        extensions.put("category", errorCode.getCategory());
        extensions.put("classification", errorType.getCode());
        extensions.put("timestamp", Instant.now().toString());
        extensions.put("retryable", errorCode.isRetryable());
        extensions.put("clientError", errorCode.isClientError());
        extensions.put("serverError", errorCode.isServerError());
        
        return extensions;
    }

    /**
     * Creates a generic error from an exception with appropriate classification.
     *
     * @param exception the exception to convert
     * @param operation the operation that failed
     * @return GraphQLError with appropriate classification
     */
    public static GraphQLError createFromException(Exception exception, String operation) {
        MutationErrorCode errorCode;
        GraphQLErrorType errorType;

        // Map common exceptions to appropriate error codes
        if (exception instanceof IllegalArgumentException) {
            errorCode = MutationErrorCode.INVALID_FIELD_VALUE;
            errorType = GraphQLErrorType.VALIDATION_ERROR;
        } else if (exception instanceof SecurityException) {
            errorCode = MutationErrorCode.INSUFFICIENT_PERMISSIONS;
            errorType = GraphQLErrorType.AUTHORIZATION_ERROR;
        } else if (exception.getMessage() != null && exception.getMessage().contains("not found")) {
            errorCode = MutationErrorCode.EXCEPTION_NOT_FOUND;
            errorType = GraphQLErrorType.NOT_FOUND;
        } else {
            errorCode = MutationErrorCode.DATABASE_ERROR;
            errorType = GraphQLErrorType.INTERNAL_ERROR;
        }

        String message = operation != null 
            ? operation + " operation failed: " + exception.getMessage()
            : exception.getMessage();

        return GraphQLError.builder()
                .message(message)
                .code(errorCode.getCode())
                .extensions(createErrorExtensions(errorCode, errorType))
                .build();
    }

    /**
     * Creates a validation result with multiple errors.
     *
     * @param errors the list of GraphQL errors
     * @return ValidationResult containing the errors
     */
    public static ValidationResult createValidationFailure(List<GraphQLError> errors) {
        return ValidationResult.failure(errors);
    }

    /**
     * Creates a validation result with a single error.
     *
     * @param error the GraphQL error
     * @return ValidationResult containing the error
     */
    public static ValidationResult createValidationFailure(GraphQLError error) {
        return ValidationResult.failure(error);
    }

    /**
     * Creates a rate limit exceeded error from a RateLimitExceededException.
     *
     * @param exception the rate limit exception
     * @return GraphQLError with rate limit classification
     */
    public static GraphQLError createRateLimitError(RateLimitExceededException exception) {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("errorCode", "RATE_LIMIT_EXCEEDED");
        extensions.put("category", "SECURITY");
        extensions.put("classification", GraphQLErrorType.AUTHORIZATION_ERROR.getCode());
        extensions.put("timestamp", Instant.now().toString());
        extensions.put("retryable", true);
        extensions.put("clientError", true);
        extensions.put("serverError", false);
        extensions.put("userId", exception.getUserId());
        extensions.put("operationType", exception.getOperationType());
        extensions.put("currentCount", exception.getCurrentCount());
        extensions.put("maxAllowed", exception.getMaxAllowed());
        extensions.put("resetTimeMs", exception.getResetTimeMs());

        return GraphQLError.builder()
                .message(exception.getMessage())
                .code("RATE_LIMIT_EXCEEDED")
                .extensions(extensions)
                .build();
    }
}