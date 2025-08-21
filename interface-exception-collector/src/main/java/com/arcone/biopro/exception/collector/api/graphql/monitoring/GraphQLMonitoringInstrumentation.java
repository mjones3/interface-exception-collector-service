package com.arcone.biopro.exception.collector.api.graphql.monitoring;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimplePerformantInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * GraphQL instrumentation for monitoring and event publishing.
 * Publishes events for completed operations that can be consumed by
 * alerting and metrics systems.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GraphQLMonitoringInstrumentation extends SimplePerformantInstrumentation {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public InstrumentationContext<ExecutionResult> beginExecution(
            InstrumentationExecutionParameters parameters,
            InstrumentationState state) {

        long startTime = System.currentTimeMillis();
        String operationType = determineOperationType(parameters.getQuery());
        String operationName = parameters.getOperation();

        return new InstrumentationContext<ExecutionResult>() {
            @Override
            public void onDispatched(CompletableFuture<ExecutionResult> result) {
                // Operation started - could add start event if needed
            }

            @Override
            public void onCompleted(ExecutionResult result, Throwable t) {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                publishOperationEvent(parameters, result, t, operationType, operationName, duration);
            }
        };
    }

    /**
     * Publishes a GraphQL operation event for monitoring and alerting.
     */
    private void publishOperationEvent(InstrumentationExecutionParameters parameters,
            ExecutionResult result, Throwable throwable,
            String operationType, String operationName, long duration) {
        try {
            // Determine success status
            boolean success = throwable == null && (result == null || result.getErrors().isEmpty());

            // Extract error information
            List<GraphQLOperationEvent.ErrorInfo> errors = null;
            if (result != null && !result.getErrors().isEmpty()) {
                errors = result.getErrors().stream()
                        .map(error -> GraphQLOperationEvent.ErrorInfo.builder()
                                .type(error.getErrorType().toString())
                                .message(error.getMessage())
                                .path(error.getPath() != null ? error.getPath().toString() : null)
                                .classification(classifyError(error.getErrorType().toString()))
                                .build())
                        .toList();
            } else if (throwable != null) {
                errors = List.of(GraphQLOperationEvent.ErrorInfo.builder()
                        .type(throwable.getClass().getSimpleName())
                        .message(throwable.getMessage())
                        .classification(classifyException(throwable))
                        .build());
            }

            // Get user context
            String userId = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                userId = authentication.getName();
            }

            // Get client IP
            String clientIp = null;
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                clientIp = getClientIpAddress(request);
            }

            // Create and publish event
            GraphQLOperationEvent event = new GraphQLOperationEvent(
                    this,
                    operationType,
                    operationName,
                    calculateQueryHash(parameters.getQuery()),
                    duration,
                    success,
                    errors,
                    userId,
                    clientIp);

            eventPublisher.publishEvent(event);

        } catch (Exception e) {
            log.error("Error publishing GraphQL operation event", e);
        }
    }

    /**
     * Determines the operation type from the GraphQL query.
     */
    private String determineOperationType(String query) {
        if (query == null) {
            return "unknown";
        }

        String trimmedQuery = query.trim().toLowerCase();
        if (trimmedQuery.startsWith("mutation")) {
            return "mutation";
        } else if (trimmedQuery.startsWith("subscription")) {
            return "subscription";
        } else {
            return "query";
        }
    }

    /**
     * Calculates a hash of the query for tracking purposes.
     */
    private String calculateQueryHash(String query) {
        if (query == null) {
            return "null";
        }
        return String.valueOf(Math.abs(query.hashCode()));
    }

    /**
     * Classifies GraphQL errors for alerting purposes.
     */
    private String classifyError(String errorType) {
        return switch (errorType.toLowerCase()) {
            case "validationerror" -> "validation";
            case "executionerror" -> "execution";
            case "datafetchingexception" -> "data_fetching";
            case "invalidsynatxerror" -> "syntax";
            default -> "unknown";
        };
    }

    /**
     * Classifies exceptions for alerting purposes.
     */
    private String classifyException(Throwable throwable) {
        String className = throwable.getClass().getSimpleName().toLowerCase();

        if (className.contains("security") || className.contains("authentication") ||
                className.contains("authorization")) {
            return "security";
        } else if (className.contains("timeout")) {
            return "timeout";
        } else if (className.contains("connection") || className.contains("database")) {
            return "database";
        } else if (className.contains("validation")) {
            return "validation";
        } else {
            return "system";
        }
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
}