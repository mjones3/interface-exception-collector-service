package com.arcone.biopro.exception.collector.infrastructure.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * Logging interceptor for RSocket interactions with structured logging and correlation IDs.
 * Provides comprehensive logging for mock server interactions with traceability.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "app.rsocket.mock-server.enabled", havingValue = "true")
public class RSocketLoggingInterceptor {

    private static final String CORRELATION_ID_KEY = "rsocket.correlation_id";
    private static final String EXTERNAL_ID_KEY = "rsocket.external_id";
    private static final String OPERATION_KEY = "rsocket.operation";
    private static final String DURATION_KEY = "rsocket.duration_ms";
    private static final String SUCCESS_KEY = "rsocket.success";
    private static final String ERROR_TYPE_KEY = "rsocket.error_type";
    private static final String SERVICE_KEY = "rsocket.service";
    private static final String TRANSACTION_ID_KEY = "rsocket.transaction_id";

    /**
     * Log the start of an RSocket call with correlation tracking.
     */
    public String logRSocketCallStart(String externalId, String operation, String transactionId) {
        String correlationId = generateCorrelationId();
        
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(EXTERNAL_ID_KEY, externalId);
        MDC.put(OPERATION_KEY, operation);
        MDC.put(SERVICE_KEY, "mock-rsocket-server");
        MDC.put(TRANSACTION_ID_KEY, transactionId);
        
        log.info("Starting RSocket call to mock server: externalId={}, operation={}, transactionId={}", 
                externalId, operation, transactionId);
        
        return correlationId;
    }

    /**
     * Log successful completion of an RSocket call.
     */
    public void logRSocketCallSuccess(String correlationId, String externalId, String operation, 
                                    Duration duration, boolean dataRetrieved) {
        setCorrelationContext(correlationId, externalId, operation, duration, true);
        
        log.info("RSocket call completed successfully: dataRetrieved={}, duration={}ms", 
                dataRetrieved, duration.toMillis());
        
        clearMDC();
    }

    /**
     * Log failed RSocket call with error details.
     */
    public void logRSocketCallFailure(String correlationId, String externalId, String operation, 
                                    Duration duration, String errorType, String errorMessage) {
        setCorrelationContext(correlationId, externalId, operation, duration, false);
        MDC.put(ERROR_TYPE_KEY, errorType);
        
        log.warn("RSocket call failed: errorType={}, errorMessage={}, duration={}ms", 
                errorType, errorMessage, duration.toMillis());
        
        clearMDC();
    }

    /**
     * Log timeout events.
     */
    public void logRSocketTimeout(String correlationId, String externalId, String operation, Duration timeout) {
        setCorrelationContext(correlationId, externalId, operation, timeout, false);
        MDC.put(ERROR_TYPE_KEY, "TIMEOUT");
        
        log.warn("RSocket call timed out after {}ms", timeout.toMillis());
        
        clearMDC();
    }

    /**
     * Log circuit breaker events.
     */
    public void logCircuitBreakerEvent(String externalId, String operation, String eventType, String state) {
        MDC.put(EXTERNAL_ID_KEY, externalId);
        MDC.put(OPERATION_KEY, operation);
        MDC.put(SERVICE_KEY, "mock-rsocket-server");
        MDC.put("circuit_breaker.event_type", eventType);
        MDC.put("circuit_breaker.state", state);
        
        log.warn("Circuit breaker event: eventType={}, state={}, externalId={}", 
                eventType, state, externalId);
        
        clearMDC();
    }

    /**
     * Log retry attempts.
     */
    public void logRetryAttempt(String correlationId, String externalId, String operation, int attemptNumber) {
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(EXTERNAL_ID_KEY, externalId);
        MDC.put(OPERATION_KEY, operation);
        MDC.put(SERVICE_KEY, "mock-rsocket-server");
        MDC.put("retry.attempt_number", String.valueOf(attemptNumber));
        
        log.info("Retrying RSocket call: attempt={}, externalId={}", attemptNumber, externalId);
        
        clearMDC();
    }

    /**
     * Log connection events.
     */
    public void logConnectionEvent(String eventType, String serverHost, int serverPort, String details) {
        MDC.put(SERVICE_KEY, "mock-rsocket-server");
        MDC.put("connection.event_type", eventType);
        MDC.put("connection.host", serverHost);
        MDC.put("connection.port", String.valueOf(serverPort));
        
        log.info("RSocket connection event: eventType={}, host={}:{}, details={}", 
                eventType, serverHost, serverPort, details);
        
        clearMDC();
    }

    /**
     * Log health check events.
     */
    public void logHealthCheck(boolean isHealthy, Duration duration, String details) {
        MDC.put(SERVICE_KEY, "mock-rsocket-server");
        MDC.put("health_check.result", String.valueOf(isHealthy));
        MDC.put(DURATION_KEY, String.valueOf(duration.toMillis()));
        
        if (isHealthy) {
            log.debug("Mock server health check passed: duration={}ms", duration.toMillis());
        } else {
            log.warn("Mock server health check failed: duration={}ms, details={}", 
                    duration.toMillis(), details);
        }
        
        clearMDC();
    }

    /**
     * Log configuration events.
     */
    public void logConfigurationEvent(String eventType, String configKey, String configValue) {
        MDC.put(SERVICE_KEY, "mock-rsocket-server");
        MDC.put("config.event_type", eventType);
        MDC.put("config.key", configKey);
        
        log.info("RSocket configuration event: eventType={}, key={}, value={}", 
                eventType, configKey, configValue);
        
        clearMDC();
    }

    private void setCorrelationContext(String correlationId, String externalId, String operation, 
                                     Duration duration, boolean success) {
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(EXTERNAL_ID_KEY, externalId);
        MDC.put(OPERATION_KEY, operation);
        MDC.put(DURATION_KEY, String.valueOf(duration.toMillis()));
        MDC.put(SUCCESS_KEY, String.valueOf(success));
        MDC.put(SERVICE_KEY, "mock-rsocket-server");
    }

    private String generateCorrelationId() {
        return "rsocket-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void clearMDC() {
        MDC.remove(CORRELATION_ID_KEY);
        MDC.remove(EXTERNAL_ID_KEY);
        MDC.remove(OPERATION_KEY);
        MDC.remove(DURATION_KEY);
        MDC.remove(SUCCESS_KEY);
        MDC.remove(ERROR_TYPE_KEY);
        MDC.remove(SERVICE_KEY);
        MDC.remove(TRANSACTION_ID_KEY);
        MDC.remove("circuit_breaker.event_type");
        MDC.remove("circuit_breaker.state");
        MDC.remove("retry.attempt_number");
        MDC.remove("connection.event_type");
        MDC.remove("connection.host");
        MDC.remove("connection.port");
        MDC.remove("health_check.result");
        MDC.remove("config.event_type");
        MDC.remove("config.key");
    }
}