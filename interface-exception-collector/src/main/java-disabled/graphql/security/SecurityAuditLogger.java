package com.arcone.biopro.exception.collector.api.graphql.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Security audit logging for all GraphQL operations.
 * Logs authentication, authorization, and operation details for security
 * monitoring.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditLogger extends SimpleInstrumentation {

    private final ObjectMapper objectMapper;

    @Override
    public InstrumentationContext<ExecutionResult> beginExecution(
            InstrumentationExecutionParameters parameters,
            InstrumentationState state) {

        String correlationId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        return new InstrumentationContext<ExecutionResult>() {
            @Override
            public void onDispatched(CompletableFuture<ExecutionResult> result) {
                logOperationStart(parameters, correlationId, startTime);
            }

            @Override
            public void onCompleted(ExecutionResult result, Throwable t) {
                long endTime = System.currentTimeMillis();
                logOperationComplete(parameters, result, t, correlationId, startTime, endTime);
            }
        };
    }

    /**
     * Logs the start of a GraphQL operation with security context.
     */
    private void logOperationStart(InstrumentationExecutionParameters parameters,
            String correlationId, long startTime) {
        try {
            Map<String, Object> auditEvent = createBaseAuditEvent(parameters, correlationId);
            auditEvent.put("event", "GRAPHQL_OPERATION_START");
            auditEvent.put("timestamp", Instant.ofEpochMilli(startTime));

            log.info("GraphQL Security Audit: {}", objectMapper.writeValueAsString(auditEvent));
        } catch (Exception e) {
            log.error("Error logging GraphQL operation start", e);
        }
    }

    /**
     * Logs the completion of a GraphQL operation with results and security context.
     */
    private void logOperationComplete(InstrumentationExecutionParameters parameters,
            ExecutionResult result, Throwable throwable,
            String correlationId, long startTime, long endTime) {
        try {
            Map<String, Object> auditEvent = createBaseAuditEvent(parameters, correlationId);
            auditEvent.put("event", "GRAPHQL_OPERATION_COMPLETE");
            auditEvent.put("timestamp", Instant.ofEpochMilli(endTime));
            auditEvent.put("duration_ms", endTime - startTime);

            // Add result information
            if (result != null) {
                auditEvent.put("success", result.getErrors().isEmpty());
                auditEvent.put("error_count", result.getErrors().size());

                if (!result.getErrors().isEmpty()) {
                    auditEvent.put("error_types", result.getErrors().stream()
                            .map(error -> error.getErrorType().toString())
                            .distinct()
                            .toList());
                }
            }

            // Add exception information if present
            if (throwable != null) {
                auditEvent.put("success", false);
                auditEvent.put("exception_type", throwable.getClass().getSimpleName());
                auditEvent.put("exception_message", throwable.getMessage());

                // Log security-related exceptions at higher level
                if (isSecurityException(throwable)) {
                    auditEvent.put("security_violation", true);
                    log.warn("GraphQL Security Violation: {}", objectMapper.writeValueAsString(auditEvent));
                } else {
                    log.info("GraphQL Security Audit: {}", objectMapper.writeValueAsString(auditEvent));
                }
            } else {
                log.info("GraphQL Security Audit: {}", objectMapper.writeValueAsString(auditEvent));
            }
        } catch (Exception e) {
            log.error("Error logging GraphQL operation completion", e);
        }
    }

    /**
     * Creates the base audit event with common security information.
     */
    private Map<String, Object> createBaseAuditEvent(InstrumentationExecutionParameters parameters,
            String correlationId) {
        Map<String, Object> auditEvent = new HashMap<>();

        auditEvent.put("correlation_id", correlationId);
        auditEvent.put("operation_name", parameters.getOperation());
        auditEvent.put("query_hash", calculateQueryHash(parameters.getQuery()));

        // Add authentication information
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            auditEvent.put("authenticated", true);
            auditEvent.put("user_id", authentication.getName());
            auditEvent.put("authorities", authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .toList());
        } else {
            auditEvent.put("authenticated", false);
        }

        // Add request information
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            auditEvent.put("client_ip", getClientIpAddress(request));
            auditEvent.put("user_agent", request.getHeader("User-Agent"));
            auditEvent.put("request_uri", request.getRequestURI());
            auditEvent.put("http_method", request.getMethod());
        }

        // Add query information (sanitized)
        auditEvent.put("query_length", parameters.getQuery().length());
        auditEvent.put("variables_count", parameters.getVariables().size());

        return auditEvent;
    }

    /**
     * Calculates a hash of the query for tracking purposes.
     */
    private String calculateQueryHash(String query) {
        if (query == null) {
            return "null";
        }

        // Simple hash for audit purposes
        return String.valueOf(query.hashCode());
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
     * Checks if an exception is security-related and should be logged at higher
     * level.
     */
    private boolean isSecurityException(Throwable throwable) {
        return throwable instanceof RateLimitExceededException ||
                throwable instanceof QueryNotAllowedException ||
                throwable instanceof org.springframework.security.access.AccessDeniedException ||
                throwable.getClass().getName().contains("AuthenticationException");
    }
}