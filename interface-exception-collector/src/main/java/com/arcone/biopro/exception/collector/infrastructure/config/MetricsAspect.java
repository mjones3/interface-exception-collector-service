package com.arcone.biopro.exception.collector.config;

import com.arcone.biopro.exception.collector.application.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;

/**
 * Aspect for automatically adding metrics to API endpoints and service methods.
 * Provides automatic metrics collection as per requirements US-016, US-017.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsAspect {

    private final MetricsService metricsService;

    /**
     * Around advice for REST controller methods to measure API response times.
     */
    @Around("execution(* com.arcone.biopro.exception.collector.api.controller.*.*(..))")
    public Object measureApiResponseTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        try {
            Object result = joinPoint.proceed();

            Duration duration = Duration.between(start, Instant.now());
            String endpoint = getEndpointFromRequest();
            String method = getHttpMethodFromRequest();

            // Record successful API call
            metricsService.recordApiResponseTime(duration, endpoint, method, 200);

            log.debug("API call completed successfully: {}.{} took {}ms",
                    className, methodName, duration.toMillis());

            return result;

        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());
            String endpoint = getEndpointFromRequest();
            String method = getHttpMethodFromRequest();

            // Record failed API call (assume 500 for exceptions)
            metricsService.recordApiResponseTime(duration, endpoint, method, 500);

            log.error("API call failed: {}.{} took {}ms, error: {}",
                    className, methodName, duration.toMillis(), e.getMessage());

            throw e;
        }
    }

    /**
     * Around advice for service layer methods to measure processing times.
     */
    @Around("execution(* com.arcone.biopro.exception.collector.application.service.*.*(..))")
    public Object measureServiceMethodTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // Skip metrics service to avoid circular calls
        if ("MetricsService".equals(className)) {
            return joinPoint.proceed();
        }

        try {
            Object result = joinPoint.proceed();

            Duration duration = Duration.between(start, Instant.now());

            log.debug("Service method completed: {}.{} took {}ms",
                    className, methodName, duration.toMillis());

            return result;

        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());

            log.error("Service method failed: {}.{} took {}ms, error: {}",
                    className, methodName, duration.toMillis(), e.getMessage());

            throw e;
        }
    }

    /**
     * Around advice for repository methods to measure database operation times.
     */
    @Around("execution(* com.arcone.biopro.exception.collector.infrastructure.repository.*.*(..))")
    public Object measureDatabaseOperationTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        try {
            Object result = joinPoint.proceed();

            Duration duration = Duration.between(start, Instant.now());
            metricsService.recordDatabaseOperation(duration, methodName, true);

            log.debug("Database operation completed: {}.{} took {}ms",
                    className, methodName, duration.toMillis());

            return result;

        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());
            metricsService.recordDatabaseOperation(duration, methodName, false);

            log.error("Database operation failed: {}.{} took {}ms, error: {}",
                    className, methodName, duration.toMillis(), e.getMessage());

            throw e;
        }
    }

    /**
     * Around advice for Kafka consumer methods to measure message processing times.
     */
    @Around("execution(* com.arcone.biopro.exception.collector.infrastructure.kafka.consumer.*.*(..))")
    public Object measureKafkaConsumerTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        try {
            Object result = joinPoint.proceed();

            Duration duration = Duration.between(start, Instant.now());
            String topicName = extractTopicFromClassName(className);
            metricsService.recordKafkaMessageConsumed(topicName, true);

            log.debug("Kafka message processed: {}.{} took {}ms",
                    className, methodName, duration.toMillis());

            return result;

        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());
            String topicName = extractTopicFromClassName(className);
            metricsService.recordKafkaMessageConsumed(topicName, false);

            log.error("Kafka message processing failed: {}.{} took {}ms, error: {}",
                    className, methodName, duration.toMillis(), e.getMessage());

            throw e;
        }
    }

    /**
     * Around advice for Kafka producer methods to measure message publishing times.
     */
    @Around("execution(* com.arcone.biopro.exception.collector.infrastructure.kafka.publisher.*.*(..))")
    public Object measureKafkaProducerTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        try {
            Object result = joinPoint.proceed();

            Duration duration = Duration.between(start, Instant.now());
            String topicName = extractTopicFromClassName(className);
            metricsService.recordKafkaMessageProduced(topicName, true);

            log.debug("Kafka message published: {}.{} took {}ms",
                    className, methodName, duration.toMillis());

            return result;

        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());
            String topicName = extractTopicFromClassName(className);
            metricsService.recordKafkaMessageProduced(topicName, false);

            log.error("Kafka message publishing failed: {}.{} took {}ms, error: {}",
                    className, methodName, duration.toMillis(), e.getMessage());

            throw e;
        }
    }

    /**
     * Around advice for external service client methods.
     */
    @Around("execution(* com.arcone.biopro.exception.collector.infrastructure.client.*.*(..))")
    public Object measureExternalServiceCallTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String serviceName = extractServiceNameFromClassName(className);

        try {
            Object result = joinPoint.proceed();

            Duration duration = Duration.between(start, Instant.now());
            metricsService.recordExternalServiceCall(duration, serviceName, true);

            log.debug("External service call completed: {}.{} to {} took {}ms",
                    className, methodName, serviceName, duration.toMillis());

            return result;

        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());
            metricsService.recordExternalServiceCall(duration, serviceName, false);

            log.error("External service call failed: {}.{} to {} took {}ms, error: {}",
                    className, methodName, serviceName, duration.toMillis(), e.getMessage());

            throw e;
        }
    }

    private String getEndpointFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getRequestURI();
            }
        } catch (Exception e) {
            log.debug("Could not extract endpoint from request", e);
        }
        return "unknown";
    }

    private String getHttpMethodFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getMethod();
            }
        } catch (Exception e) {
            log.debug("Could not extract HTTP method from request", e);
        }
        return "unknown";
    }

    private String extractTopicFromClassName(String className) {
        // Extract topic name from consumer/producer class names
        if (className.contains("Order")) {
            return "order-events";
        } else if (className.contains("Collection")) {
            return "collection-events";
        } else if (className.contains("Distribution")) {
            return "distribution-events";
        } else if (className.contains("Validation")) {
            return "validation-events";
        } else if (className.contains("Alert")) {
            return "alert-events";
        } else if (className.contains("Exception")) {
            return "exception-events";
        }
        return "unknown-topic";
    }

    private String extractServiceNameFromClassName(String className) {
        // Extract service name from client class names
        if (className.contains("Order")) {
            return "order-service";
        } else if (className.contains("Collection")) {
            return "collection-service";
        } else if (className.contains("Distribution")) {
            return "distribution-service";
        }
        return "unknown-service";
    }
}