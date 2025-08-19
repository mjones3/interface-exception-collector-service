package com.arcone.biopro.exception.collector.infrastructure.monitoring;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import com.dynatrace.oneagent.sdk.api.IncomingWebRequestTracer;
import com.dynatrace.oneagent.sdk.api.OutgoingWebRequestTracer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for capturing business-specific metrics for Dynatrace monitoring.
 * Provides comprehensive instrumentation for interface exception management
 * that goes beyond standard infrastructure monitoring.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "dynatrace.enabled", havingValue = "true", matchIfMissing = false)
public class DynatraceBusinessMetricsService {

    private final MeterRegistry meterRegistry;
    private final OneAgentSDK oneAgentSDK;

    // Business Metrics Counters
    private Counter exceptionsReceivedCounter;
    private Counter exceptionsProcessedCounter;
    private Counter exceptionsResolvedCounter;
    private Counter exceptionsAcknowledgedCounter;
    private Counter retryAttemptsCounter;
    private Counter retrySuccessCounter;
    private Counter retryFailureCounter;
    private Counter payloadRetrievalCounter;
    private Counter payloadRetrievalFailureCounter;

    // Business Metrics Timers
    private Timer exceptionProcessingTimer;
    private Timer retryOperationTimer;
    private Timer payloadRetrievalTimer;
    private Timer resolutionTimeTimer;
    private Timer acknowledgmentTimeTimer;

    // Business State Gauges
    private final AtomicLong pendingExceptionsCount = new AtomicLong(0);
    private final AtomicLong failedExceptionsCount = new AtomicLong(0);
    private final AtomicLong resolvedExceptionsCount = new AtomicLong(0);
    private final AtomicLong criticalExceptionsCount = new AtomicLong(0);
    private final AtomicLong highExceptionsCount = new AtomicLong(0);
    private final AtomicLong mediumExceptionsCount = new AtomicLong(0);
    private final AtomicLong lowExceptionsCount = new AtomicLong(0);

    // Interface-specific metrics
    private final ConcurrentHashMap<InterfaceType, AtomicLong> interfaceExceptionCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> customerExceptionCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> locationExceptionCounts = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeMetrics() {
        log.info("Initializing Dynatrace business metrics for interface exception management");

        // Exception lifecycle counters
        exceptionsReceivedCounter = Counter.builder("biopro.exceptions.received")
                .description("Total number of interface exceptions received")
                .tag("component", "exception-collector")
                .register(meterRegistry);

        exceptionsProcessedCounter = Counter.builder("biopro.exceptions.processed")
                .description("Total number of interface exceptions processed")
                .tag("component", "exception-collector")
                .register(meterRegistry);

        exceptionsResolvedCounter = Counter.builder("biopro.exceptions.resolved")
                .description("Total number of interface exceptions resolved")
                .tag("component", "exception-collector")
                .register(meterRegistry);

        exceptionsAcknowledgedCounter = Counter.builder("biopro.exceptions.acknowledged")
                .description("Total number of interface exceptions acknowledged")
                .tag("component", "exception-collector")
                .register(meterRegistry);

        // Retry operation counters
        retryAttemptsCounter = Counter.builder("biopro.exceptions.retry.attempts")
                .description("Total number of retry attempts")
                .tag("component", "exception-collector")
                .register(meterRegistry);

        retrySuccessCounter = Counter.builder("biopro.exceptions.retry.success")
                .description("Total number of successful retry attempts")
                .tag("component", "exception-collector")
                .register(meterRegistry);

        retryFailureCounter = Counter.builder("biopro.exceptions.retry.failure")
                .description("Total number of failed retry attempts")
                .tag("component", "exception-collector")
                .register(meterRegistry);

        // Payload retrieval counters
        payloadRetrievalCounter = Counter.builder("biopro.exceptions.payload.retrieval")
                .description("Total number of payload retrieval attempts")
                .tag("component", "exception-collector")
                .register(meterRegistry);

        payloadRetrievalFailureCounter = Counter.builder("biopro.exceptions.payload.retrieval.failure")
                .description("Total number of failed payload retrievals")
                .tag("component", "exception-collector")
                .register(meterRegistry);

        // Business process timers
        exceptionProcessingTimer = Timer.builder("biopro.exceptions.processing.duration")
                .description("Time taken to process interface exceptions")
                .tag("component", "exception-collector")
                .register(meterRegistry);

        retryOperationTimer = Timer.builder("biopro.exceptions.retry.duration")
                .description("Time taken for retry operations")
                .tag("component", "exception-collector")
                .register(meterRegistry);

        payloadRetrievalTimer = Timer.builder("biopro.exceptions.payload.retrieval.duration")
                .description("Time taken to retrieve original payloads")
                .tag("component", "exception-collector")
                .register(meterRegistry);

        resolutionTimeTimer = Timer.builder("biopro.exceptions.resolution.duration")
                .description("Time from exception creation to resolution")
                .tag("component", "exception-collector")
                .register(meterRegistry);

        acknowledgmentTimeTimer = Timer.builder("biopro.exceptions.acknowledgment.duration")
                .description("Time from exception creation to acknowledgment")
                .tag("component", "exception-collector")
                .register(meterRegistry);

        // Business state gauges
        Gauge.builder("biopro.exceptions.pending.count")
                .description("Current number of pending exceptions")
                .tag("component", "exception-collector")
                .register(meterRegistry, pendingExceptionsCount, AtomicLong::get);

        Gauge.builder("biopro.exceptions.failed.count")
                .description("Current number of failed exceptions")
                .tag("component", "exception-collector")
                .register(meterRegistry, failedExceptionsCount, AtomicLong::get);

        Gauge.builder("biopro.exceptions.resolved.count")
                .description("Current number of resolved exceptions")
                .tag("component", "exception-collector")
                .register(meterRegistry, resolvedExceptionsCount, AtomicLong::get);

        // Severity-based gauges
        Gauge.builder("biopro.exceptions.severity.critical.count")
                .description("Current number of critical severity exceptions")
                .tag("component", "exception-collector")
                .tag("severity", "critical")
                .register(meterRegistry, criticalExceptionsCount, AtomicLong::get);

        Gauge.builder("biopro.exceptions.severity.high.count")
                .description("Current number of high severity exceptions")
                .tag("component", "exception-collector")
                .tag("severity", "high")
                .register(meterRegistry, highExceptionsCount, AtomicLong::get);

        Gauge.builder("biopro.exceptions.severity.medium.count")
                .description("Current number of medium severity exceptions")
                .tag("component", "exception-collector")
                .tag("severity", "medium")
                .register(meterRegistry, mediumExceptionsCount, AtomicLong::get);

        Gauge.builder("biopro.exceptions.severity.low.count")
                .description("Current number of low severity exceptions")
                .tag("component", "exception-collector")
                .tag("severity", "low")
                .register(meterRegistry, lowExceptionsCount, AtomicLong::get);

        log.info("Dynatrace business metrics initialized successfully");
    }

    /**
     * Records metrics when a new interface exception is received.
     *
     * @param exception the received exception
     */
    public void recordExceptionReceived(InterfaceException exception) {
        exceptionsReceivedCounter.increment(
                "interface_type", exception.getInterfaceType().name(),
                "severity", exception.getSeverity().name(),
                "customer_id", exception.getCustomerId() != null ? exception.getCustomerId() : "unknown",
                "location_code", exception.getLocationCode() != null ? exception.getLocationCode() : "unknown");

        // Update interface-specific counters
        interfaceExceptionCounts.computeIfAbsent(exception.getInterfaceType(), k -> new AtomicLong(0))
                .incrementAndGet();

        // Update customer-specific counters
        if (exception.getCustomerId() != null) {
            customerExceptionCounts.computeIfAbsent(exception.getCustomerId(), k -> new AtomicLong(0))
                    .incrementAndGet();
        }

        // Update location-specific counters
        if (exception.getLocationCode() != null) {
            locationExceptionCounts.computeIfAbsent(exception.getLocationCode(), k -> new AtomicLong(0))
                    .incrementAndGet();
        }

        // Update severity counters
        updateSeverityCounters(exception.getSeverity(), 1);

        // Update status counters
        updateStatusCounters(exception.getStatus(), 1);

        // Add custom attributes for Dynatrace
        addCustomAttributes("exception_received", exception);

        log.debug("Recorded exception received metrics for transaction: {}", exception.getTransactionId());
    }

    /**
     * Records metrics when an exception is processed.
     *
     * @param exception        the processed exception
     * @param processingTimeMs processing time in milliseconds
     */
    public void recordExceptionProcessed(InterfaceException exception, long processingTimeMs) {
        exceptionsProcessedCounter.increment(
                "interface_type", exception.getInterfaceType().name(),
                "severity", exception.getSeverity().name(),
                "status", exception.getStatus().name());

        exceptionProcessingTimer.record(Duration.ofMillis(processingTimeMs));

        addCustomAttributes("exception_processed", exception);

        log.debug("Recorded exception processed metrics for transaction: {} ({}ms)",
                exception.getTransactionId(), processingTimeMs);
    }

    /**
     * Records metrics when an exception is resolved.
     *
     * @param exception the resolved exception
     */
    public void recordExceptionResolved(InterfaceException exception) {
        exceptionsResolvedCounter.increment(
                "interface_type", exception.getInterfaceType().name(),
                "severity", exception.getSeverity().name(),
                "resolution_method", determineResolutionMethod(exception));

        // Calculate resolution time
        if (exception.getResolvedAt() != null && exception.getCreatedAt() != null) {
            Duration resolutionDuration = Duration.between(exception.getCreatedAt(), exception.getResolvedAt());
            resolutionTimeTimer.record(resolutionDuration);
        }

        // Update status counters
        updateStatusCounters(ExceptionStatus.PENDING, -1);
        updateStatusCounters(ExceptionStatus.RESOLVED, 1);

        addCustomAttributes("exception_resolved", exception);

        log.debug("Recorded exception resolved metrics for transaction: {}", exception.getTransactionId());
    }

    /**
     * Records metrics when an exception is acknowledged.
     *
     * @param exception the acknowledged exception
     */
    public void recordExceptionAcknowledged(InterfaceException exception) {
        exceptionsAcknowledgedCounter.increment(
                "interface_type", exception.getInterfaceType().name(),
                "severity", exception.getSeverity().name(),
                "acknowledged_by", exception.getAcknowledgedBy() != null ? exception.getAcknowledgedBy() : "system");

        // Calculate acknowledgment time
        if (exception.getAcknowledgedAt() != null && exception.getCreatedAt() != null) {
            Duration acknowledgmentDuration = Duration.between(exception.getCreatedAt(), exception.getAcknowledgedAt());
            acknowledgmentTimeTimer.record(acknowledgmentDuration);
        }

        addCustomAttributes("exception_acknowledged", exception);

        log.debug("Recorded exception acknowledged metrics for transaction: {}", exception.getTransactionId());
    }

    /**
     * Records metrics for retry attempts.
     *
     * @param retryAttempt the retry attempt
     * @param success      whether the retry was successful
     * @param durationMs   retry duration in milliseconds
     */
    public void recordRetryAttempt(RetryAttempt retryAttempt, boolean success, long durationMs) {
        retryAttemptsCounter.increment(
                "attempt_number", String.valueOf(retryAttempt.getAttemptNumber()),
                "success", String.valueOf(success));

        if (success) {
            retrySuccessCounter.increment();
        } else {
            retryFailureCounter.increment();
        }

        retryOperationTimer.record(Duration.ofMillis(durationMs));

        // Add custom attributes for retry tracking
        if (oneAgentSDK.getCurrentState() == OneAgentSDK.State.ACTIVE) {
            CustomRequestAttributes customAttributes = oneAgentSDK.createCustomRequestAttributes();
            customAttributes.addCustomAttribute("retry_attempt_number",
                    String.valueOf(retryAttempt.getAttemptNumber()));
            customAttributes.addCustomAttribute("retry_success", String.valueOf(success));
            customAttributes.addCustomAttribute("retry_duration_ms", String.valueOf(durationMs));
        }

        log.debug("Recorded retry attempt metrics - Attempt: {}, Success: {}, Duration: {}ms",
                retryAttempt.getAttemptNumber(), success, durationMs);
    }

    /**
     * Records metrics for payload retrieval operations.
     *
     * @param transactionId the transaction ID
     * @param interfaceType the interface type
     * @param success       whether retrieval was successful
     * @param durationMs    retrieval duration in milliseconds
     */
    public void recordPayloadRetrieval(String transactionId, InterfaceType interfaceType, boolean success,
            long durationMs) {
        payloadRetrievalCounter.increment(
                "interface_type", interfaceType.name(),
                "success", String.valueOf(success));

        if (!success) {
            payloadRetrievalFailureCounter.increment("interface_type", interfaceType.name());
        }

        payloadRetrievalTimer.record(Duration.ofMillis(durationMs));

        // Add custom attributes for payload retrieval tracking
        if (oneAgentSDK.getCurrentState() == OneAgentSDK.State.ACTIVE) {
            CustomRequestAttributes customAttributes = oneAgentSDK.createCustomRequestAttributes();
            customAttributes.addCustomAttribute("payload_transaction_id", transactionId);
            customAttributes.addCustomAttribute("payload_interface_type", interfaceType.name());
            customAttributes.addCustomAttribute("payload_retrieval_success", String.valueOf(success));
            customAttributes.addCustomAttribute("payload_retrieval_duration_ms", String.valueOf(durationMs));
        }

        log.debug("Recorded payload retrieval metrics - Transaction: {}, Interface: {}, Success: {}, Duration: {}ms",
                transactionId, interfaceType, success, durationMs);
    }

    /**
     * Creates a Dynatrace web request tracer for incoming requests.
     *
     * @param url    the request URL
     * @param method the HTTP method
     * @return the tracer instance
     */
    public IncomingWebRequestTracer createIncomingWebRequestTracer(String url, String method) {
        if (oneAgentSDK.getCurrentState() != OneAgentSDK.State.ACTIVE) {
            return null;
        }

        return oneAgentSDK.traceIncomingWebRequest()
                .withUrl(url)
                .withMethod(method)
                .withHeader("User-Agent", "Interface-Exception-Collector")
                .start();
    }

    /**
     * Creates a Dynatrace web request tracer for outgoing requests.
     *
     * @param url    the request URL
     * @param method the HTTP method
     * @return the tracer instance
     */
    public OutgoingWebRequestTracer createOutgoingWebRequestTracer(String url, String method) {
        if (oneAgentSDK.getCurrentState() != OneAgentSDK.State.ACTIVE) {
            return null;
        }

        return oneAgentSDK.traceOutgoingWebRequest()
                .withUrl(url)
                .withMethod(method)
                .start();
    }

    /**
     * Updates severity-based counters.
     */
    private void updateSeverityCounters(ExceptionSeverity severity, int delta) {
        switch (severity) {
            case CRITICAL -> criticalExceptionsCount.addAndGet(delta);
            case HIGH -> highExceptionsCount.addAndGet(delta);
            case MEDIUM -> mediumExceptionsCount.addAndGet(delta);
            case LOW -> lowExceptionsCount.addAndGet(delta);
        }
    }

    /**
     * Updates status-based counters.
     */
    private void updateStatusCounters(ExceptionStatus status, int delta) {
        switch (status) {
            case PENDING -> pendingExceptionsCount.addAndGet(delta);
            case FAILED -> failedExceptionsCount.addAndGet(delta);
            case RESOLVED -> resolvedExceptionsCount.addAndGet(delta);
        }
    }

    /**
     * Adds custom attributes to Dynatrace for enhanced monitoring.
     */
    private void addCustomAttributes(String operation, InterfaceException exception) {
        if (oneAgentSDK.getCurrentState() != OneAgentSDK.State.ACTIVE) {
            return;
        }

        CustomRequestAttributes customAttributes = oneAgentSDK.createCustomRequestAttributes();
        customAttributes.addCustomAttribute("operation", operation);
        customAttributes.addCustomAttribute("transaction_id", exception.getTransactionId());
        customAttributes.addCustomAttribute("interface_type", exception.getInterfaceType().name());
        customAttributes.addCustomAttribute("severity", exception.getSeverity().name());
        customAttributes.addCustomAttribute("status", exception.getStatus().name());

        if (exception.getCustomerId() != null) {
            customAttributes.addCustomAttribute("customer_id", exception.getCustomerId());
        }

        if (exception.getLocationCode() != null) {
            customAttributes.addCustomAttribute("location_code", exception.getLocationCode());
        }

        if (exception.getExternalId() != null) {
            customAttributes.addCustomAttribute("external_id", exception.getExternalId());
        }
    }

    /**
     * Determines the resolution method based on exception properties.
     */
    private String determineResolutionMethod(InterfaceException exception) {
        if (exception.getRetryCount() > 0) {
            return "retry_success";
        } else if (exception.getAcknowledgedAt() != null) {
            return "manual_acknowledgment";
        } else {
            return "automatic_resolution";
        }
    }

    /**
     * Gets current business metrics summary for health checks.
     */
    public BusinessMetricsSummary getBusinessMetricsSummary() {
        return BusinessMetricsSummary.builder()
                .pendingExceptions(pendingExceptionsCount.get())
                .failedExceptions(failedExceptionsCount.get())
                .resolvedExceptions(resolvedExceptionsCount.get())
                .criticalExceptions(criticalExceptionsCount.get())
                .highSeverityExceptions(highExceptionsCount.get())
                .mediumSeverityExceptions(mediumExceptionsCount.get())
                .lowSeverityExceptions(lowExceptionsCount.get())
                .totalExceptionsReceived(exceptionsReceivedCounter.count())
                .totalExceptionsProcessed(exceptionsProcessedCounter.count())
                .totalRetryAttempts(retryAttemptsCounter.count())
                .retrySuccessRate(calculateRetrySuccessRate())
                .build();
    }

    private double calculateRetrySuccessRate() {
        double totalRetries = retryAttemptsCounter.count();
        if (totalRetries == 0) {
            return 0.0;
        }
        return (retrySuccessCounter.count() / totalRetries) * 100.0;
    }

    /**
     * Business metrics summary data structure.
     */
    @lombok.Builder
    @lombok.Data
    public static class BusinessMetricsSummary {
        private long pendingExceptions;
        private long failedExceptions;
        private long resolvedExceptions;
        private long criticalExceptions;
        private long highSeverityExceptions;
        private long mediumSeverityExceptions;
        private long lowSeverityExceptions;
        private double totalExceptionsReceived;
        private double totalExceptionsProcessed;
        private double totalRetryAttempts;
        private double retrySuccessRate;
    }
}