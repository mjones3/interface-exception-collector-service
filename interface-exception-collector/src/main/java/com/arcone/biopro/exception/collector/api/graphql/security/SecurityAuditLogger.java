package com.arcone.biopro.exception.collector.api.graphql.security;

import com.arcone.biopro.exception.collector.domain.entity.MutationAuditLog;
import com.arcone.biopro.exception.collector.infrastructure.repository.MutationAuditLogRepository;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Security audit logging for all GraphQL operations.
 * Logs authentication, authorization, and operation details for security monitoring.
 * Enhanced to support comprehensive mutation audit logging with database persistence.
 * 
 * Requirements: 5.3, 5.5, 6.4
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditLogger extends SimpleInstrumentation {

    private final ObjectMapper objectMapper;
    private final MutationAuditLogRepository auditLogRepository;

    // List of mutation operations that require detailed audit logging
    private static final List<String> MUTATION_OPERATIONS = List.of(
        "retryException", "bulkRetryExceptions", "cancelRetry",
        "acknowledgeException", "bulkAcknowledgeExceptions", "resolveException"
    );

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

    /**
     * Logs a mutation operation attempt with comprehensive audit information.
     * 
     * @param operationType the type of mutation operation
     * @param transactionId the transaction ID being operated on
     * @param performedBy the user performing the operation
     * @param inputData the input parameters for the operation
     * @param operationId unique identifier for this operation instance
     * @param correlationId correlation ID for tracing
     */
    public void logMutationAttempt(MutationAuditLog.OperationType operationType,
                                  String transactionId,
                                  String performedBy,
                                  Object inputData,
                                  String operationId,
                                  String correlationId) {
        try {
            String inputJson = inputData != null ? objectMapper.writeValueAsString(inputData) : null;
            
            MutationAuditLog auditLog = MutationAuditLog.builder()
                .operationType(operationType)
                .transactionId(transactionId)
                .performedBy(performedBy)
                .performedAt(Instant.now())
                .inputData(inputJson)
                .resultStatus(MutationAuditLog.ResultStatus.SUCCESS) // Will be updated on completion
                .operationId(operationId)
                .correlationId(correlationId)
                .clientIp(getCurrentClientIp())
                .userAgent(getCurrentUserAgent())
                .build();

            auditLogRepository.save(auditLog);
            
            log.info("Mutation audit logged: operation={}, transactionId={}, user={}, operationId={}", 
                operationType, transactionId, performedBy, operationId);
                
        } catch (Exception e) {
            log.error("Failed to log mutation attempt: operation={}, transactionId={}, error={}", 
                operationType, transactionId, e.getMessage(), e);
        }
    }

    /**
     * Logs the completion of a mutation operation with result details.
     * 
     * @param operationId the operation ID to update
     * @param success whether the operation was successful
     * @param errors any errors that occurred
     * @param executionTimeMs execution time in milliseconds
     */
    public void logMutationResult(String operationId,
                                 boolean success,
                                 List<Object> errors,
                                 long executionTimeMs) {
        try {
            // Find the existing audit log entry by operation ID
            List<MutationAuditLog> existingLogs = auditLogRepository.findByOperationIdOrderByPerformedAtAsc(operationId);
            
            if (!existingLogs.isEmpty()) {
                MutationAuditLog auditLog = existingLogs.get(0); // Get the first (should be only one)
                
                // Update with result information
                auditLog.setResultStatus(success ? 
                    MutationAuditLog.ResultStatus.SUCCESS : 
                    MutationAuditLog.ResultStatus.FAILURE);
                
                if (errors != null && !errors.isEmpty()) {
                    String errorJson = objectMapper.writeValueAsString(errors);
                    auditLog.setErrorDetails(errorJson);
                }
                
                auditLog.setExecutionTimeMs((int) executionTimeMs);
                
                auditLogRepository.save(auditLog);
                
                log.info("Mutation result logged: operationId={}, success={}, executionTime={}ms", 
                    operationId, success, executionTimeMs);
            } else {
                log.warn("No existing audit log found for operationId: {}", operationId);
            }
            
        } catch (Exception e) {
            log.error("Failed to log mutation result: operationId={}, error={}", 
                operationId, e.getMessage(), e);
        }
    }

    /**
     * Logs a bulk operation result with partial success information.
     * 
     * @param operationId the operation ID to update
     * @param successCount number of successful operations
     * @param failureCount number of failed operations
     * @param errors any errors that occurred
     * @param executionTimeMs execution time in milliseconds
     */
    public void logBulkMutationResult(String operationId,
                                     int successCount,
                                     int failureCount,
                                     List<Object> errors,
                                     long executionTimeMs) {
        try {
            List<MutationAuditLog> existingLogs = auditLogRepository.findByOperationIdOrderByPerformedAtAsc(operationId);
            
            if (!existingLogs.isEmpty()) {
                MutationAuditLog auditLog = existingLogs.get(0);
                
                // Determine result status based on success/failure counts
                MutationAuditLog.ResultStatus resultStatus;
                if (failureCount == 0) {
                    resultStatus = MutationAuditLog.ResultStatus.SUCCESS;
                } else if (successCount == 0) {
                    resultStatus = MutationAuditLog.ResultStatus.FAILURE;
                } else {
                    resultStatus = MutationAuditLog.ResultStatus.PARTIAL_SUCCESS;
                }
                
                auditLog.setResultStatus(resultStatus);
                
                // Create summary of bulk operation results
                Map<String, Object> resultSummary = new HashMap<>();
                resultSummary.put("successCount", successCount);
                resultSummary.put("failureCount", failureCount);
                resultSummary.put("totalCount", successCount + failureCount);
                
                if (errors != null && !errors.isEmpty()) {
                    resultSummary.put("errors", errors);
                }
                
                String resultJson = objectMapper.writeValueAsString(resultSummary);
                auditLog.setErrorDetails(resultJson);
                auditLog.setExecutionTimeMs((int) executionTimeMs);
                
                auditLogRepository.save(auditLog);
                
                log.info("Bulk mutation result logged: operationId={}, success={}, failure={}, executionTime={}ms", 
                    operationId, successCount, failureCount, executionTimeMs);
            }
            
        } catch (Exception e) {
            log.error("Failed to log bulk mutation result: operationId={}, error={}", 
                operationId, e.getMessage(), e);
        }
    }

    /**
     * Gets the current client IP address from the request context.
     */
    private String getCurrentClientIp() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                return getClientIpAddress(request);
            }
        } catch (Exception e) {
            log.debug("Could not get client IP: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Gets the current user agent from the request context.
     */
    private String getCurrentUserAgent() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Could not get user agent: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Generates a unique operation ID for tracking mutation operations.
     */
    public String generateOperationId(String operationType, String transactionId) {
        return String.format("%s_%s_%d", operationType.toUpperCase(), 
            transactionId.replaceAll("[^a-zA-Z0-9]", ""), System.currentTimeMillis());
    }

    /**
     * Generates a correlation ID for tracing operations across systems.
     */
    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}