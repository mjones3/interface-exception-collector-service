package com.arcone.biopro.exception.collector.api.graphql.exception;

import com.arcone.biopro.exception.collector.domain.exception.ExceptionNotFoundException;
import com.arcone.biopro.exception.collector.domain.exception.ExceptionProcessingException;
import com.arcone.biopro.exception.collector.domain.exception.RetryNotAllowedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import jakarta.validation.ConstraintViolationException;
import java.util.concurrent.TimeoutException;

/**
 * Maps Java exceptions to GraphQL error types for consistent error handling.
 */
@Component
@Slf4j
public class GraphQLExceptionMapper {

    /**
     * Maps a Java exception to the appropriate GraphQL error type.
     */
    public GraphQLErrorType mapExceptionToErrorType(Throwable exception) {
        if (exception == null) {
            return GraphQLErrorType.INTERNAL_ERROR;
        }

        // Validation errors
        if (exception instanceof IllegalArgumentException ||
                exception instanceof ConstraintViolationException) {
            return GraphQLErrorType.VALIDATION_ERROR;
        }

        // Authorization errors
        if (exception instanceof AccessDeniedException ||
                exception instanceof SecurityException) {
            return GraphQLErrorType.AUTHORIZATION_ERROR;
        }

        // Not found errors
        if (exception instanceof ExceptionNotFoundException) {
            return GraphQLErrorType.NOT_FOUND;
        }

        // Business rule errors
        if (exception instanceof RetryNotAllowedException ||
                exception instanceof ExceptionProcessingException) {
            return GraphQLErrorType.BUSINESS_RULE_ERROR;
        }

        // External service errors
        if (exception instanceof ResourceAccessException ||
                isExternalServiceException(exception)) {
            return GraphQLErrorType.EXTERNAL_SERVICE_ERROR;
        }

        // Timeout errors
        if (exception instanceof TimeoutException ||
                isTimeoutException(exception)) {
            return GraphQLErrorType.TIMEOUT_ERROR;
        }

        // Concurrent modification errors
        if (exception instanceof OptimisticLockingFailureException) {
            return GraphQLErrorType.CONCURRENT_MODIFICATION_ERROR;
        }

        // Database errors
        if (exception instanceof DataAccessException) {
            log.error("Database error occurred", exception);
            return GraphQLErrorType.INTERNAL_ERROR;
        }

        // Rate limiting errors (if using rate limiting library)
        if (isRateLimitException(exception)) {
            return GraphQLErrorType.RATE_LIMIT_ERROR;
        }

        // Query complexity errors (if using query complexity analysis)
        if (isQueryComplexityException(exception)) {
            return GraphQLErrorType.QUERY_COMPLEXITY_ERROR;
        }

        // Default to internal error for unknown exceptions
        log.error("Unmapped exception type: {}", exception.getClass().getName(), exception);
        return GraphQLErrorType.INTERNAL_ERROR;
    }

    /**
     * Determines if the exception is related to external service failures.
     */
    private boolean isExternalServiceException(Throwable exception) {
        String message = exception.getMessage();
        if (message == null) {
            return false;
        }

        return message.contains("Connection refused") ||
                message.contains("Service unavailable") ||
                message.contains("Circuit breaker") ||
                exception.getClass().getName().contains("ServiceException");
    }

    /**
     * Determines if the exception is related to timeouts.
     */
    private boolean isTimeoutException(Throwable exception) {
        String message = exception.getMessage();
        if (message == null) {
            return false;
        }

        return message.contains("timeout") ||
                message.contains("timed out") ||
                exception.getClass().getName().contains("Timeout");
    }

    /**
     * Determines if the exception is related to rate limiting.
     */
    private boolean isRateLimitException(Throwable exception) {
        String className = exception.getClass().getName();
        return className.contains("RateLimit") ||
                className.contains("Throttle") ||
                (exception.getMessage() != null &&
                        exception.getMessage().contains("rate limit"));
    }

    /**
     * Determines if the exception is related to query complexity.
     */
    private boolean isQueryComplexityException(Throwable exception) {
        String className = exception.getClass().getName();
        return className.contains("QueryComplexity") ||
                className.contains("MaxQueryDepth") ||
                (exception.getMessage() != null &&
                        (exception.getMessage().contains("query complexity") ||
                                exception.getMessage().contains("query depth")));
    }

    /**
     * Gets a user-friendly error message for the given exception type.
     */
    public String getUserFriendlyMessage(GraphQLErrorType errorType, Throwable exception) {
        switch (errorType) {
            case VALIDATION_ERROR:
                return "The provided input is invalid. Please check your request and try again.";
            case AUTHORIZATION_ERROR:
                return "You don't have permission to perform this operation.";
            case NOT_FOUND:
                return "The requested resource was not found.";
            case BUSINESS_RULE_ERROR:
                return getBusinessRuleMessage(exception);
            case EXTERNAL_SERVICE_ERROR:
                return "An external service is currently unavailable. Please try again later.";
            case RATE_LIMIT_ERROR:
                return "Too many requests. Please wait before trying again.";
            case QUERY_COMPLEXITY_ERROR:
                return "The query is too complex. Please simplify your request.";
            case TIMEOUT_ERROR:
                return "The operation took too long to complete. Please try again.";
            case CONCURRENT_MODIFICATION_ERROR:
                return "The resource was modified by another user. Please refresh and try again.";
            case INTERNAL_ERROR:
            default:
                return "An internal error occurred. Please try again later.";
        }
    }

    /**
     * Gets a specific business rule error message.
     */
    private String getBusinessRuleMessage(Throwable exception) {
        if (exception instanceof RetryNotAllowedException) {
            return "This exception cannot be retried at this time.";
        }
        if (exception instanceof ExceptionProcessingException) {
            return "Unable to process the exception due to business rules.";
        }
        return "The operation violates business rules.";
    }
}