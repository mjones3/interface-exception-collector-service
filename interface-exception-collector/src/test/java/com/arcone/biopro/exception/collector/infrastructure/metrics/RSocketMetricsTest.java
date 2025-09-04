package com.arcone.biopro.exception.collector.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RSocketMetricsTest {

    private MeterRegistry meterRegistry;
    private RSocketMetrics rSocketMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        rSocketMetrics = new RSocketMetrics(meterRegistry);
    }

    @Test
    void shouldRecordSuccessfulCall() {
        // Given
        Duration duration = Duration.ofMillis(150);
        String operation = "GET_ORDER_DATA";

        // When
        rSocketMetrics.recordSuccessfulCall(duration, operation);

        // Then
        Counter totalCalls = meterRegistry.get("rsocket.calls.total").counter();
        Counter successCalls = meterRegistry.get("rsocket.calls.success").counter();
        Timer callDuration = meterRegistry.get("rsocket.call.duration").timer();

        assertThat(totalCalls.count()).isEqualTo(1.0);
        assertThat(successCalls.count()).isEqualTo(1.0);
        assertThat(callDuration.count()).isEqualTo(1);
        assertThat(callDuration.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isEqualTo(150.0);
    }

    @Test
    void shouldRecordFailedCall() {
        // Given
        Duration duration = Duration.ofMillis(200);
        String operation = "GET_ORDER_DATA";
        String errorType = "TimeoutException";

        // When
        rSocketMetrics.recordFailedCall(duration, operation, errorType);

        // Then
        Counter totalCalls = meterRegistry.get("rsocket.calls.total").counter();
        Counter failureCalls = meterRegistry.get("rsocket.calls.failure").counter();
        Counter errors = meterRegistry.get("rsocket.errors.total").counter();
        Timer callDuration = meterRegistry.get("rsocket.call.duration").timer();

        assertThat(totalCalls.count()).isEqualTo(1.0);
        assertThat(failureCalls.count()).isEqualTo(1.0);
        assertThat(errors.count()).isEqualTo(1.0);
        assertThat(callDuration.count()).isEqualTo(1);
        assertThat(callDuration.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isEqualTo(200.0);
    }

    @Test
    void shouldRecordTimeout() {
        // Given
        String operation = "GET_ORDER_DATA";

        // When
        rSocketMetrics.recordTimeout(operation);

        // Then
        Counter timeouts = meterRegistry.get("rsocket.timeouts.total").counter();
        assertThat(timeouts.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordCircuitBreakerEvent() {
        // Given
        String eventType = "FALLBACK_TRIGGERED";
        String operation = "GET_ORDER_DATA";

        // When
        rSocketMetrics.recordCircuitBreakerEvent(eventType, operation);

        // Then
        Counter circuitBreakerEvents = meterRegistry.get("rsocket.circuit_breaker.events").counter();
        assertThat(circuitBreakerEvents.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordError() {
        // Given
        String errorType = "ConnectionException";
        String operation = "GET_ORDER_DATA";
        String externalId = "TEST-ORDER-1";

        // When
        rSocketMetrics.recordError(errorType, operation, externalId);

        // Then
        Counter errors = meterRegistry.get("rsocket.errors.total").counter();
        assertThat(errors.count()).isEqualTo(1.0);
    }

    @Test
    void shouldCalculateSuccessRate() {
        // Given - Record some successful and failed calls
        rSocketMetrics.recordSuccessfulCall(Duration.ofMillis(100), "GET_ORDER_DATA");
        rSocketMetrics.recordSuccessfulCall(Duration.ofMillis(150), "GET_ORDER_DATA");
        rSocketMetrics.recordFailedCall(Duration.ofMillis(200), "GET_ORDER_DATA", "TimeoutException");

        // When
        double successRate = rSocketMetrics.getSuccessRate();

        // Then
        assertThat(successRate).isEqualTo(66.66666666666666); // 2 out of 3 calls successful
    }

    @Test
    void shouldReturnHundredPercentSuccessRateWhenNoCalls() {
        // When
        double successRate = rSocketMetrics.getSuccessRate();

        // Then
        assertThat(successRate).isEqualTo(100.0);
    }

    @Test
    void shouldGetTotalCalls() {
        // Given
        rSocketMetrics.recordSuccessfulCall(Duration.ofMillis(100), "GET_ORDER_DATA");
        rSocketMetrics.recordFailedCall(Duration.ofMillis(200), "GET_ORDER_DATA", "Error");

        // When
        double totalCalls = rSocketMetrics.getTotalCalls();

        // Then
        assertThat(totalCalls).isEqualTo(2.0);
    }

    @Test
    void shouldGetTotalErrors() {
        // Given
        rSocketMetrics.recordError("Error1", "GET_ORDER_DATA", "ORDER-1");
        rSocketMetrics.recordError("Error2", "GET_ORDER_DATA", "ORDER-2");

        // When
        double totalErrors = rSocketMetrics.getTotalErrors();

        // Then
        assertThat(totalErrors).isEqualTo(2.0);
    }
}