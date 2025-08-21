package com.arcone.biopro.exception.collector.api.graphql.exception;

import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Enhanced GraphQL exception handler for converting Java exceptions to GraphQL
 * errors.
 * Provides structured error responses with proper error classification, codes,
 * and
 * sanitized messages with comprehensive error extensions.
 */
@Component
@Slf4j
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Autowired
    private GraphQLExceptionMapper exceptionMapper;

    @Value("${app.graphql.error.include-stack-trace:false}")
    private boolean includeStackTrace;

    @Value("${app.graphql.error.include-debug-info:false}")
    private boolean includeDebugInfo;

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        // Generate unique error ID for tracking
        String errorId = UUID.randomUUID().toString();

        // Map exception to GraphQL error type
        GraphQLErrorType errorType = exceptionMapper.mapExceptionToErrorType(ex);

        // Log error with appropriate level based on error type
        logError(errorId, errorType, ex, env);

        // Handle constraint violations specially for detailed field-level errors
        if (ex instanceof ConstraintViolationException) {
            return createConstraintViolationError((ConstraintViolationException) ex, env, errorId, errorType);
        }

        // Create structured error response
        return createStructuredError(ex, env, errorId, errorType);
    }

    /**
     * Logs errors with appropriate level based on error type.
     */
    private void logError(String errorId, GraphQLErrorType errorType, Throwable ex, DataFetchingEnvironment env) {
        String fieldPath = env.getExecutionStepInfo().getPath().toString();
        String operation = env.getOperationDefinition().getOperation().toString();

        if (errorType.isClientError()) {
            log.warn("GraphQL client error [{}] in {} operation at path '{}': {} - {}",
                    errorId, operation, fieldPath, errorType.getCode(), ex.getMessage());
        } else {
            log.error("GraphQL server error [{}] in {} operation at path '{}': {} - {}",
                    errorId, operation, fieldPath, errorType.getCode(), ex.getMessage(), ex);
        }
    }

    /**
     * Creates a structured GraphQL error with comprehensive extensions.
     */
    private GraphQLError createStructuredError(Throwable ex, DataFetchingEnvironment env,
            String errorId, GraphQLErrorType errorType) {
        String userMessage = exceptionMapper.getUserFriendlyMessage(errorType, ex);

        return GraphQLError.newError()
                .errorType(errorType)
                .message(sanitizeErrorMessage(userMessage))
                .location(env.getField().getSourceLocation())
                .path(env.getExecutionStepInfo().getPath())
                .extensions(createErrorExtensions(errorId, errorType, ex, env))
                .build();
    }

    /**
     * Creates a detailed constraint violation error for Bean Validation failures.
     */
    private GraphQLError createConstraintViolationError(ConstraintViolationException ex,
            DataFetchingEnvironment env,
            String errorId,
            GraphQLErrorType errorType) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();

        String message = violations.isEmpty() ? "Validation failed"
                : "Validation failed: " + violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));

        Map<String, Object> extensions = createErrorExtensions(errorId, errorType, ex, env);

        // Add detailed violation information
        extensions.put("violations", violations.stream()
                .map(violation -> {
                    Map<String, Object> violationMap = new HashMap<>();
                    violationMap.put("field", violation.getPropertyPath().toString());
                    violationMap.put("message", violation.getMessage());
                    violationMap.put("rejectedValue",
                            violation.getInvalidValue() != null ? sanitizeValue(violation.getInvalidValue().toString())
                                    : null);
                    violationMap.put("constraint",
                            violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName());
                    return violationMap;
                })
                .collect(Collectors.toList()));

        return GraphQLError.newError()
                .errorType(errorType)
                .message(sanitizeErrorMessage(message))
                .location(env.getField().getSourceLocation())
                .path(env.getExecutionStepInfo().getPath())
                .extensions(extensions)
                .build();
    }

    /**
     * Sanitizes error messages to prevent information leakage.
     */
    private String sanitizeErrorMessage(String message) {
        if (message == null) {
            return "An error occurred";
        }

        // Remove sensitive information patterns
        String sanitized = message
                .replaceAll("(?i)password[=:]\\s*\\S+", "password=***")
                .replaceAll("(?i)token[=:]\\s*\\S+", "token=***")
                .replaceAll("(?i)key[=:]\\s*\\S+", "key=***")
                .replaceAll("(?i)secret[=:]\\s*\\S+", "secret=***")
                .replaceAll("(?i)authorization[=:]\\s*\\S+", "authorization=***")
                .replaceAll("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b", "****-****-****-****") // Credit card
                                                                                                          // numbers
                .replaceAll("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b", "***@***.***"); // Email addresses

        // Limit message length
        if (sanitized.length() > 500) {
            sanitized = sanitized.substring(0, 497) + "...";
        }

        return sanitized;
    }

    /**
     * Sanitizes values to prevent sensitive data exposure.
     */
    private String sanitizeValue(String value) {
        if (value == null) {
            return null;
        }

        // Truncate long values
        if (value.length() > 100) {
            return value.substring(0, 97) + "...";
        }

        return sanitizeErrorMessage(value);
    }

    /**
     * Creates comprehensive error extensions with metadata and context.
     */
    private Map<String, Object> createErrorExtensions(String errorId, GraphQLErrorType errorType,
            Throwable exception, DataFetchingEnvironment env) {
        Map<String, Object> extensions = new HashMap<>();

        // Core error information
        extensions.put("errorId", errorId);
        extensions.put("code", errorType.getCode());
        extensions.put("classification", errorType.name());
        extensions.put("timestamp", Instant.now().toString());
        extensions.put("httpStatus", errorType.getHttpStatusCode());

        // Operation context
        extensions.put("operation", env.getOperationDefinition().getOperation().toString());
        extensions.put("fieldName", env.getField().getName());

        // Add debug information if enabled
        if (includeDebugInfo) {
            extensions.put("exceptionType", exception.getClass().getSimpleName());
            extensions.put("originalMessage", sanitizeErrorMessage(exception.getMessage()));

            if (includeStackTrace && errorType.isServerError()) {
                extensions.put("stackTrace", getStackTraceElements(exception));
            }
        }

        // Add retry information for retryable errors
        if (isRetryableError(errorType)) {
            extensions.put("retryable", true);
            extensions.put("retryAfter", calculateRetryAfter(errorType));
        } else {
            extensions.put("retryable", false);
        }

        return extensions;
    }

    /**
     * Determines if an error type is retryable.
     */
    private boolean isRetryableError(GraphQLErrorType errorType) {
        return errorType == GraphQLErrorType.EXTERNAL_SERVICE_ERROR ||
                errorType == GraphQLErrorType.TIMEOUT_ERROR ||
                errorType == GraphQLErrorType.RATE_LIMIT_ERROR ||
                errorType == GraphQLErrorType.INTERNAL_ERROR;
    }

    /**
     * Calculates retry delay in seconds based on error type.
     */
    private int calculateRetryAfter(GraphQLErrorType errorType) {
        switch (errorType) {
            case RATE_LIMIT_ERROR:
                return 60; // 1 minute
            case EXTERNAL_SERVICE_ERROR:
                return 30; // 30 seconds
            case TIMEOUT_ERROR:
                return 15; // 15 seconds
            case INTERNAL_ERROR:
            default:
                return 120; // 2 minutes
        }
    }

    /**
     * Gets sanitized stack trace elements for debugging.
     */
    private String[] getStackTraceElements(Throwable exception) {
        return java.util.Arrays.stream(exception.getStackTrace())
                .limit(10) // Limit to first 10 stack trace elements
                .map(StackTraceElement::toString)
                .toArray(String[]::new);
    }
}