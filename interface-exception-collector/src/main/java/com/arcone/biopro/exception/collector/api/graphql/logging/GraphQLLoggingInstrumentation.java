package com.arcone.biopro.exception.collector.api.graphql.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimplePerformantInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.schema.DataFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * GraphQL instrumentation for comprehensive logging and monitoring.
 * Provides structured logging for all GraphQL operations with performance
 * metrics,
 * security context, and error tracking.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GraphQLLoggingInstrumentation extends SimplePerformantInstrumentation {

    private final ObjectMapper objectMapper;

    @Value("${graphql.monitoring.logging.slow-query-threshold-ms:1000}")
    private long slowQueryThreshold;

    @Value("${graphql.monitoring.logging.slow-field-threshold-ms:100}")
    private long slowFieldThreshold;

    @Override
    public InstrumentationContext<ExecutionResult> beginExecution(
            InstrumentationExecutionParameters parameters,
            InstrumentationState state) {

        String correlationId = UUID.randomUUID().toString();
        String operationName = parameters.getOperation();
        long startTime = System.currentTimeMillis();

        // Set MDC context for structured logging
        MDC.put("correlationId", correlationId);
        MDC.put("operationType", "graphql");
        MDC.put("operationName", operationName != null ? operationName : "anonymous");

        return new InstrumentationContext<ExecutionResult>() {
            @Override
            public void onDispatched(CompletableFuture<ExecutionResult> result) {
                logOperationStart(parameters, correlationId, startTime);
            }

            @Override
            public void onCompleted(ExecutionResult result, Throwable t) {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                logOperationComplete(parameters, result, t, correlationId, duration);

                // Clear MDC context
                MDC.remove("correlationId");
                MDC.remove("operationType");
                MDC.remove("operationName");
            }
        };
    }

    @Override
    public InstrumentationContext<Object> beginFieldFetch(
            InstrumentationFieldFetchParameters parameters,
            InstrumentationState state) {

        String fieldPath = parameters.getExecutionStepInfo().getPath().toString();
        long startTime = System.currentTimeMillis();

        return new InstrumentationContext<Object>() {
            @Override
            public void onDispatched(CompletableFuture<Object> result) {
                // Field fetch started - no action needed
            }

            @Override
            public void onCompleted(Object result, Throwable t) {
                long duration = System.currentTimeMillis() - startTime;

                // Log slow field fetches
                if (duration > getSlowFieldThreshold()) {
                    logSlowFieldFetch(fieldPath, duration, t);
                }

                // Log field errors
                if (t != null) {
                    logFieldError(fieldPath, t);
                }
            }
        };
    }

    /**
     * Logs the start of a GraphQL operation with context information.
     */
    private void logOperationStart(InstrumentationExecutionParameters parameters,
            String correlationId, long startTime) {
        try {
            Map<String, Object> logEvent = createBaseLogEvent(parameters, correlationId);
            logEvent.put("event", "GRAPHQL_OPERATION_START");
            logEvent.put("timestamp", Instant.ofEpochMilli(startTime));
            logEvent.put("query_length", parameters.getQuery().length());
            logEvent.put("variables_count", parameters.getVariables().size());

            log.info("GraphQL Operation Started: {}", objectMapper.writeValueAsString(logEvent));
        } catch (Exception e) {
            log.error("Error logging GraphQL operation start", e);
        }
    }

    /**
     * Logs the completion of a GraphQL operation with results and performance
     * metrics.
     */
    private void logOperationComplete(InstrumentationExecutionParameters parameters,
            ExecutionResult result, Throwable throwable,
            String correlationId, long duration) {
        try {
            Map<String, Object> logEvent = createBaseLogEvent(parameters, correlationId);
            logEvent.put("event", "GRAPHQL_OPERATION_COMPLETE");
            logEvent.put("timestamp", Instant.now());
            logEvent.put("duration_ms", duration);

            // Add result information
            if (result != null) {
                logEvent.put("success", result.getErrors().isEmpty());
                logEvent.put("error_count", result.getErrors().size());
                logEvent.put("has_data", result.getData() != null);

                if (!result.getErrors().isEmpty()) {
                    logEvent.put("error_types", result.getErrors().stream()
                            .map(error -> error.getErrorType().toString())
                            .distinct()
                            .toList());

                    logEvent.put("error_messages", result.getErrors().stream()
                            .map(error -> error.getMessage())
                            .toList());
                }
            }

            // Add exception information if present
            if (throwable != null) {
                logEvent.put("success", false);
                logEvent.put("exception_type", throwable.getClass().getSimpleName());
                logEvent.put("exception_message", throwable.getMessage());
            }

            // Add performance classification
            logEvent.put("performance_category", classifyPerformance(duration));

            // Log at appropriate level based on performance and errors
            if (throwable != null || (result != null && !result.getErrors().isEmpty())) {
                log.error("GraphQL Operation Failed: {}", objectMapper.writeValueAsString(logEvent));
            } else if (duration > getSlowQueryThreshold()) {
                log.warn("GraphQL Operation Slow: {}", objectMapper.writeValueAsString(logEvent));
            } else {
                log.info("GraphQL Operation Completed: {}", objectMapper.writeValueAsString(logEvent));
            }
        } catch (Exception e) {
            log.error("Error logging GraphQL operation completion", e);
        }
    }

    /**
     * Logs slow field fetch operations for performance monitoring.
     */
    private void logSlowFieldFetch(String fieldPath, long duration, Throwable throwable) {
        try {
            Map<String, Object> logEvent = new HashMap<>();
            logEvent.put("event", "GRAPHQL_SLOW_FIELD_FETCH");
            logEvent.put("timestamp", Instant.now());
            logEvent.put("field_path", fieldPath);
            logEvent.put("duration_ms", duration);
            logEvent.put("threshold_ms", getSlowFieldThreshold());

            if (throwable != null) {
                logEvent.put("exception_type", throwable.getClass().getSimpleName());
                logEvent.put("exception_message", throwable.getMessage());
            }

            log.warn("GraphQL Slow Field Fetch: {}", objectMapper.writeValueAsString(logEvent));
        } catch (Exception e) {
            log.error("Error logging slow field fetch", e);
        }
    }

    /**
     * Logs field-level errors for detailed error tracking.
     */
    private void logFieldError(String fieldPath, Throwable throwable) {
        try {
            Map<String, Object> logEvent = new HashMap<>();
            logEvent.put("event", "GRAPHQL_FIELD_ERROR");
            logEvent.put("timestamp", Instant.now());
            logEvent.put("field_path", fieldPath);
            logEvent.put("exception_type", throwable.getClass().getSimpleName());
            logEvent.put("exception_message", throwable.getMessage());

            log.error("GraphQL Field Error: {}", objectMapper.writeValueAsString(logEvent));
        } catch (Exception e) {
            log.error("Error logging field error", e);
        }
    }

    /**
     * Creates the base log event with common information.
     */
    private Map<String, Object> createBaseLogEvent(InstrumentationExecutionParameters parameters,
            String correlationId) {
        Map<String, Object> logEvent = new HashMap<>();

        logEvent.put("correlation_id", correlationId);
        logEvent.put("operation_name", parameters.getOperation());
        logEvent.put("query_hash", calculateQueryHash(parameters.getQuery()));
        logEvent.put("service", "interface-exception-collector-service");
        logEvent.put("api_type", "graphql");

        // Add authentication information
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            logEvent.put("authenticated", true);
            logEvent.put("user_id", authentication.getName());
            logEvent.put("authorities", authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .toList());
        } else {
            logEvent.put("authenticated", false);
        }

        // Add request information
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            logEvent.put("client_ip", getClientIpAddress(request));
            logEvent.put("user_agent", request.getHeader("User-Agent"));
            logEvent.put("request_uri", request.getRequestURI());
            logEvent.put("http_method", request.getMethod());
        }

        return logEvent;
    }

    /**
     * Calculates a hash of the query for tracking and caching purposes.
     */
    private String calculateQueryHash(String query) {
        if (query == null) {
            return "null";
        }
        return String.valueOf(Math.abs(query.hashCode()));
    }

    /**
     * Gets the client IP address from the request, considering proxy headers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Classifies performance based on duration thresholds.
     */
    private String classifyPerformance(long duration) {
        if (duration < 100) {
            return "fast";
        } else if (duration < 500) {
            return "normal";
        } else if (duration < 2000) {
            return "slow";
        } else {
            return "very_slow";
        }
    }

    /**
     * Gets the slow query threshold from configuration.
     */
    private long getSlowQueryThreshold() {
        return slowQueryThreshold;
    }

    /**
     * Gets the slow field threshold from configuration.
     */
    private long getSlowFieldThreshold() {
        return slowFieldThreshold;
    }
}