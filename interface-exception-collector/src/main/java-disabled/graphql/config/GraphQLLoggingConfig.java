package com.arcone.biopro.exception.collector.api.graphql.config;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GraphQL logging instrumentation for structured logging with correlation IDs
 * and comprehensive operation tracking.
 */
@Slf4j
@Component
@ConditionalOnProperty(
    name = "graphql.security.audit-logging.enabled", 
    havingValue = "true", 
    matchIfMissing = true
)
public class GraphQLLoggingConfig extends SimpleInstrumentation {

    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String OPERATION_NAME_KEY = "graphql.operation";
    private static final String OPERATION_TYPE_KEY = "graphql.operationType";
    private static final String USER_ID_KEY = "graphql.userId";
    private static final String EXECUTION_ID_KEY = "graphql.executionId";

    @Override
    public InstrumentationContext<ExecutionResult> beginExecution(
            InstrumentationExecutionParameters parameters,
            InstrumentationState state) {
        
        // Generate correlation ID for this execution
        String correlationId = generateCorrelationId();
        String executionId = generateExecutionId();
        
        // Extract operation details
        String operationName = extractOperationName(parameters);
        String operationType = extractOperationType(parameters);
        String userId = extractUserId(parameters);
        
        // Set up MDC for structured logging
        setupMDC(correlationId, executionId, operationName, operationType, userId);
        
        // Log operation start
        logOperationStart(parameters, correlationId, executionId, operationName, operationType, userId);
        
        Instant startTime = Instant.now();
        
        return new InstrumentationContext<ExecutionResult>() {
            @Override
            public void onCompleted(ExecutionResult result, Throwable t) {
                try {
                    Instant endTime = Instant.now();
                    long durationMs = endTime.toEpochMilli() - startTime.toEpochMilli();
                    
                    // Log operation completion
                    logOperationCompletion(
                        result, t, correlationId, executionId, 
                        operationName, operationType, userId, durationMs
                    );
                    
                    // Log errors if any
                    if (!result.getErrors().isEmpty()) {
                        logGraphQLErrors(result, correlationId, executionId, operationName);
                    }
                    
                } finally {
                    // Clean up MDC
                    clearMDC();
                }
            }
        };
    }

    /**
     * Set up MDC (Mapped Diagnostic Context) for structured logging
     */
    private void setupMDC(String correlationId, String executionId, 
                         String operationName, String operationType, String userId) {
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(EXECUTION_ID_KEY, executionId);
        MDC.put(OPERATION_NAME_KEY, operationName);
        MDC.put(OPERATION_TYPE_KEY, operationType);
        
        if (userId != null) {
            MDC.put(USER_ID_KEY, userId);
        }
        
        // Add timestamp
        MDC.put("timestamp", Instant.now().toString());
    }

    /**
     * Clear MDC after operation completion
     */
    private void clearMDC() {
        MDC.remove(CORRELATION_ID_KEY);
        MDC.remove(EXECUTION_ID_KEY);
        MDC.remove(OPERATION_NAME_KEY);
        MDC.remove(OPERATION_TYPE_KEY);
        MDC.remove(USER_ID_KEY);
        MDC.remove("timestamp");
    }

    /**
     * Log GraphQL operation start
     */
    private void logOperationStart(InstrumentationExecutionParameters parameters,
                                 String correlationId, String executionId,
                                 String operationName, String operationType, String userId) {
        
        Map<String, Object> logData = new HashMap<>();
        logData.put("event", "graphql_operation_start");
        logData.put("correlationId", correlationId);
        logData.put("executionId", executionId);
        logData.put("operationName", operationName);
        logData.put("operationType", operationType);
        logData.put("userId", userId);
        logData.put("timestamp", Instant.now().toString());
        
        // Add query details if logging is enabled
        if (shouldLogQueries()) {
            logData.put("query", parameters.getQuery());
        }
        
        // Add variables if logging is enabled
        if (shouldLogVariables() && parameters.getVariables() != null) {
            logData.put("variables", sanitizeVariables(parameters.getVariables()));
        }
        
        log.info("GraphQL operation started: {}", logData);
    }

    /**
     * Log GraphQL operation completion
     */
    private void logOperationCompletion(ExecutionResult result, Throwable throwable,
                                      String correlationId, String executionId,
                                      String operationName, String operationType,
                                      String userId, long durationMs) {
        
        Map<String, Object> logData = new HashMap<>();
        logData.put("event", "graphql_operation_completed");
        logData.put("correlationId", correlationId);
        logData.put("executionId", executionId);
        logData.put("operationName", operationName);
        logData.put("operationType", operationType);
        logData.put("userId", userId);
        logData.put("durationMs", durationMs);
        logData.put("timestamp", Instant.now().toString());
        logData.put("success", result.getErrors().isEmpty() && throwable == null);
        logData.put("errorCount", result.getErrors().size());
        
        if (throwable != null) {
            logData.put("exception", throwable.getClass().getSimpleName());
            logData.put("exceptionMessage", throwable.getMessage());
        }
        
        // Performance warning for slow operations
        if (durationMs > 1000) {
            logData.put("performance_warning", "slow_operation");
            log.warn("Slow GraphQL operation completed: {}", logData);
        } else {
            log.info("GraphQL operation completed: {}", logData);
        }
    }

    /**
     * Log GraphQL errors with detailed information
     */
    private void logGraphQLErrors(ExecutionResult result, String correlationId, 
                                String executionId, String operationName) {
        
        result.getErrors().forEach(error -> {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("event", "graphql_error");
            errorData.put("correlationId", correlationId);
            errorData.put("executionId", executionId);
            errorData.put("operationName", operationName);
            errorData.put("errorType", error.getErrorType() != null ? 
                error.getErrorType().toString() : "unknown");
            errorData.put("errorMessage", error.getMessage());
            errorData.put("timestamp", Instant.now().toString());
            
            if (error.getLocations() != null && !error.getLocations().isEmpty()) {
                errorData.put("locations", error.getLocations());
            }
            
            if (error.getPath() != null && !error.getPath().isEmpty()) {
                errorData.put("path", error.getPath());
            }
            
            if (error.getExtensions() != null && !error.getExtensions().isEmpty()) {
                errorData.put("extensions", error.getExtensions());
            }
            
            log.error("GraphQL error occurred: {}", errorData);
        });
    }

    /**
     * Extract operation name from parameters
     */
    private String extractOperationName(InstrumentationExecutionParameters parameters) {
        if (parameters.getOperation() != null && parameters.getOperation().getName() != null) {
            return parameters.getOperation().getName();
        }
        return "anonymous";
    }

    /**
     * Extract operation type from parameters
     */
    private String extractOperationType(InstrumentationExecutionParameters parameters) {
        if (parameters.getOperation() != null && 
            parameters.getOperation().getOperation() != null) {
            return parameters.getOperation().getOperation().toString().toLowerCase();
        }
        return "unknown";
    }

    /**
     * Extract user ID from GraphQL context
     */
    private String extractUserId(InstrumentationExecutionParameters parameters) {
        try {
            // Try to extract user ID from GraphQL context
            Object userId = parameters.getGraphQLContext().get("userId");
            if (userId != null) {
                return userId.toString();
            }
            
            // Try to extract from authentication context
            Object auth = parameters.getGraphQLContext().get("authentication");
            if (auth != null) {
                return auth.toString();
            }
            
            return "anonymous";
        } catch (Exception e) {
            log.debug("Failed to extract user ID from GraphQL context", e);
            return "unknown";
        }
    }

    /**
     * Generate unique correlation ID
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generate unique execution ID
     */
    private String generateExecutionId() {
        return UUID.randomUUID().toString().substring(0, 12);
    }

    /**
     * Check if query logging is enabled
     */
    private boolean shouldLogQueries() {
        // This should be configurable via application properties
        // For security reasons, query logging should be disabled in production
        return Boolean.parseBoolean(
            System.getProperty("graphql.security.audit-logging.log-queries", "false")
        );
    }

    /**
     * Check if variable logging is enabled
     */
    private boolean shouldLogVariables() {
        // This should be configurable via application properties
        // For security reasons, variable logging should be disabled in production
        return Boolean.parseBoolean(
            System.getProperty("graphql.security.audit-logging.log-variables", "false")
        );
    }

    /**
     * Sanitize variables to remove sensitive information
     */
    private Map<String, Object> sanitizeVariables(Map<String, Object> variables) {
        Map<String, Object> sanitized = new HashMap<>();
        
        variables.forEach((key, value) -> {
            // Remove sensitive fields
            if (isSensitiveField(key)) {
                sanitized.put(key, "[REDACTED]");
            } else {
                sanitized.put(key, value);
            }
        });
        
        return sanitized;
    }

    /**
     * Check if a field contains sensitive information
     */
    private boolean isSensitiveField(String fieldName) {
        String lowerFieldName = fieldName.toLowerCase();
        return lowerFieldName.contains("password") ||
               lowerFieldName.contains("token") ||
               lowerFieldName.contains("secret") ||
               lowerFieldName.contains("key") ||
               lowerFieldName.contains("auth");
    }

    /**
     * Create structured log entry for audit purposes
     */
    public static void logAuditEvent(String eventType, String userId, 
                                   String operationName, Map<String, Object> details) {
        
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "graphql_audit");
        auditData.put("eventType", eventType);
        auditData.put("userId", userId);
        auditData.put("operationName", operationName);
        auditData.put("timestamp", Instant.now().toString());
        auditData.put("correlationId", MDC.get(CORRELATION_ID_KEY));
        
        if (details != null) {
            auditData.putAll(details);
        }
        
        log.info("GraphQL audit event: {}", auditData);
    }
}