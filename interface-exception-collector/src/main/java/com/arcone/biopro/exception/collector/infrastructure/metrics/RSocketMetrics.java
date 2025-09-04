package com.arcone.biopro.exception.collector.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Metrics collection component for RSocket interactions with mock server.
 * Provides Prometheus metrics for monitoring RSocket call performance and reliability.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "biopro.rsocket.mock-server.enabled", havingValue = "true")
public class RSocketMetrics {

    private final Counter rSocketCallsTotal;
    private final Counter rSocketCallsSuccess;
    private final Counter rSocketCallsFailure;
    private final Timer rSocketCallDuration;
    private final Counter rSocketErrors;
    private final Counter rSocketTimeouts;
    private final Counter rSocketCircuitBreakerEvents;

    public RSocketMetrics(MeterRegistry meterRegistry) {
        this.rSocketCallsTotal = Counter.builder("rsocket.calls.total")
            .description("Total number of RSocket calls made to mock server")
            .tag("service", "mock-rsocket-server")
            .tag("client", "interface-exception-collector")
            .register(meterRegistry);

        this.rSocketCallsSuccess = Counter.builder("rsocket.calls.success")
            .description("Number of successful RSocket calls")
            .tag("service", "mock-rsocket-server")
            .register(meterRegistry);

        this.rSocketCallsFailure = Counter.builder("rsocket.calls.failure")
            .description("Number of failed RSocket calls")
            .tag("service", "mock-rsocket-server")
            .register(meterRegistry);

        this.rSocketCallDuration = Timer.builder("rsocket.call.duration")
            .description("Duration of RSocket calls to mock server")
            .tag("service", "mock-rsocket-server")
            .register(meterRegistry);

        this.rSocketErrors = Counter.builder("rsocket.errors.total")
            .description("Total number of RSocket errors by type")
            .tag("service", "mock-rsocket-server")
            .register(meterRegistry);

        this.rSocketTimeouts = Counter.builder("rsocket.timeouts.total")
            .description("Number of RSocket call timeouts")
            .tag("service", "mock-rsocket-server")
            .register(meterRegistry);

        this.rSocketCircuitBreakerEvents = Counter.builder("rsocket.circuit_breaker.events")
            .description("Circuit breaker events for RSocket calls")
            .tag("service", "mock-rsocket-server")
            .register(meterRegistry);
    }

    /**
     * Record a successful RSocket call with duration.
     */
    public void recordSuccessfulCall(Duration duration, String operation) {
        rSocketCallsTotal.increment();
        rSocketCallsSuccess.increment();
        rSocketCallDuration.record(duration.toMillis(), TimeUnit.MILLISECONDS);
        
        log.debug("Recorded successful RSocket call: operation={}, duration={}ms", 
                 operation, duration.toMillis());
    }

    /**
     * Record a failed RSocket call with duration and error type.
     */
    public void recordFailedCall(Duration duration, String operation, String errorType) {
        rSocketCallsTotal.increment();
        rSocketCallsFailure.increment();
        rSocketCallDuration.record(duration.toMillis(), TimeUnit.MILLISECONDS);
        
        rSocketErrors.increment();
        
        log.debug("Recorded failed RSocket call: operation={}, duration={}ms, error={}", 
                 operation, duration.toMillis(), errorType);
    }

    /**
     * Record a timeout event.
     */
    public void recordTimeout(String operation) {
        rSocketTimeouts.increment();
        
        log.debug("Recorded RSocket timeout: operation={}", operation);
    }

    /**
     * Record circuit breaker events.
     */
    public void recordCircuitBreakerEvent(String eventType, String operation) {
        rSocketCircuitBreakerEvents.increment();
        
        log.debug("Recorded circuit breaker event: type={}, operation={}", eventType, operation);
    }

    /**
     * Record a generic RSocket error with custom tags.
     */
    public void recordError(String errorType, String operation, String externalId) {
        rSocketErrors.increment();
        
        log.debug("Recorded RSocket error: type={}, operation={}, externalId={}", 
                 errorType, operation, externalId);
    }

    /**
     * Get current success rate as a percentage.
     */
    public double getSuccessRate() {
        double totalCalls = rSocketCallsTotal.count();
        if (totalCalls == 0) {
            return 100.0; // No calls made yet, assume healthy
        }
        
        double successfulCalls = rSocketCallsSuccess.count();
        return (successfulCalls / totalCalls) * 100.0;
    }

    /**
     * Get total number of calls made.
     */
    public double getTotalCalls() {
        return rSocketCallsTotal.count();
    }

    /**
     * Get total number of errors.
     */
    public double getTotalErrors() {
        return rSocketErrors.count();
    }

    /**
     * Record a fallback response being used.
     */
    public void recordFallbackResponse(String operation) {
        log.debug("Recorded fallback response usage: operation={}", operation);
        // Could add specific fallback metrics here if needed
    }

    /**
     * Record a successful connection establishment.
     */
    public void recordConnectionSuccess() {
        log.debug("Recorded successful RSocket connection");
        // Could add connection-specific metrics here if needed
    }

    /**
     * Record a failed connection attempt.
     */
    public void recordConnectionFailure() {
        log.debug("Recorded failed RSocket connection");
        // Could add connection failure metrics here if needed
    }

    /**
     * Record a connection retry attempt.
     */
    public void recordConnectionRetry() {
        log.debug("Recorded RSocket connection retry");
        // Could add retry-specific metrics here if needed
    }

    /**
     * Record that fallback mode has been enabled.
     */
    public void recordFallbackModeEnabled() {
        log.debug("Recorded fallback mode enabled");
        // Could add fallback mode metrics here if needed
    }
}