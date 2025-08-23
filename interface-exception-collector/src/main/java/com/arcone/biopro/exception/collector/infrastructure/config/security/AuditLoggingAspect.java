package com.arcone.biopro.exception.collector.infrastructure.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect for auditing data access and modifications
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLoggingAspect {

    private final ObjectMapper objectMapper;

    /**
     * Audit all repository operations
     */
    @Before("execution(* com.arcone.biopro.exception.collector.infrastructure.repository.*.*(..))")
    public void auditRepositoryAccess(JoinPoint joinPoint) {
        String username = getCurrentUsername();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        Map<String, Object> auditEvent = createAuditEvent(
                "DATA_ACCESS",
                username,
                className + "." + methodName,
                args);

        log.info("AUDIT: {}", serializeAuditEvent(auditEvent));
    }

    /**
     * Audit all service layer modifications
     */
    @Before("execution(* com.arcone.biopro.exception.collector.application.service.*.save*(..)) || " +
            "execution(* com.arcone.biopro.exception.collector.application.service.*.update*(..)) || " +
            "execution(* com.arcone.biopro.exception.collector.application.service.*.delete*(..)) || " +
            "execution(* com.arcone.biopro.exception.collector.application.service.*.acknowledge*(..)) || " +
            "execution(* com.arcone.biopro.exception.collector.application.service.*.resolve*(..))")
    public void auditDataModification(JoinPoint joinPoint) {
        String username = getCurrentUsername();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        Map<String, Object> auditEvent = createAuditEvent(
                "DATA_MODIFICATION",
                username,
                className + "." + methodName,
                args);

        log.info("AUDIT: {}", serializeAuditEvent(auditEvent));
    }

    /**
     * Audit retry operations
     */
    @AfterReturning(pointcut = "execution(* com.arcone.biopro.exception.collector.application.service.RetryService.retry*(..))", returning = "result")
    public void auditRetryOperation(JoinPoint joinPoint, Object result) {
        String username = getCurrentUsername();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        Map<String, Object> auditEvent = createAuditEvent(
                "RETRY_OPERATION",
                username,
                "RetryService." + methodName,
                args);

        auditEvent.put("result", result != null ? result.toString() : "null");

        log.info("AUDIT: {}", serializeAuditEvent(auditEvent));
    }

    /**
     * Audit API access
     */
    @Before("execution(* com.arcone.biopro.exception.collector.api.controller.*.*(..))")
    public void auditApiAccess(JoinPoint joinPoint) {
        String username = getCurrentUsername();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        Map<String, Object> auditEvent = createAuditEvent(
                "API_ACCESS",
                username,
                className + "." + methodName,
                args);

        log.info("AUDIT: {}", serializeAuditEvent(auditEvent));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous";
    }

    private Map<String, Object> createAuditEvent(String eventType, String username, String operation, Object[] args) {
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", eventType);
        auditEvent.put("timestamp", Instant.now().toString());
        auditEvent.put("username", username);
        auditEvent.put("operation", operation);
        auditEvent.put("arguments", sanitizeArguments(args));

        return auditEvent;
    }

    private Object[] sanitizeArguments(Object[] args) {
        if (args == null) {
            return new Object[0];
        }

        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) {
                        return "null";
                    }

                    String argString = arg.toString();
                    // Mask potential sensitive data
                    if (argString.toLowerCase().contains("password") ||
                            argString.toLowerCase().contains("token") ||
                            argString.toLowerCase().contains("secret")) {
                        return "[MASKED]";
                    }

                    // Limit argument length to prevent log bloat
                    if (argString.length() > 200) {
                        return argString.substring(0, 200) + "...";
                    }

                    return argString;
                })
                .toArray();
    }

    private String serializeAuditEvent(Map<String, Object> auditEvent) {
        try {
            return objectMapper.writeValueAsString(auditEvent);
        } catch (Exception e) {
            log.error("Failed to serialize audit event", e);
            return auditEvent.toString();
        }
    }
}