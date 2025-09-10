package com.arcone.biopro.exception.collector.infrastructure.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Component for collecting comprehensive mutation operation performance metrics.
 * Tracks success rates, execution times, error rates, and operation counts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MutationMetrics {

    private final MeterRegistry meterRegistry;
    
    // Counters for tracking mutation operations
    private final Counter retryMutationCounter;
    private final Counter acknowledgeMutationCounter;
    private final Counter resolveMutationCounter;
    private final Counter cancelRetryMutationCounter;
    
    // Counters for tracking errors
    private final Counter mutationErrorCounter;
    private final Counter validationErrorCounter;
    private final Counter businessRuleErrorCounter;
    
    // Timers for tracking execution duration
    private final Timer retryMutationTimer;
    private final Timer acknowledgeMutationTimer;
    private final Timer resolveMutationTimer;
    private final Timer cancelRetryMutationTimer;
    
    // Gauges for tracking active operations
    private final AtomicLong activeRetryOperations = new AtomicLong(0);
    private final AtomicLong activeAcknowledgeOperations = new AtomicLong(0);
    private final AtomicLong activeResolveOperations = new AtomicLong(0);
    private final AtomicLong activeCancelOperations = new AtomicLong(0);
    
    // Success rate tracking
    private final ConcurrentHashMap<String, AtomicLong> successCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> totalCounts = new ConcurrentHashMap<>();

    public MutationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize mutation operation counters
        this.retryMutationCounter = Counter.builder("graphql.mutation.retry.total")
                .description("Total number of retry mutation operations")
                .register(meterRegistry);
                
        this.acknowledgeMutationCounter = Counter.builder("graphql.mutation.acknowledge.total")
                .description("Total number of acknowledge mutation operations")
                .register(meterRegistry);
                
        this.resolveMutationCounter = Counter.builder("graphql.mutation.resolve.total")
                .description("Total number of resolve mutation operations")
                .register(meterRegistry);
                
        this.cancelRetryMutationCounter = Counter.builder("graphql.mutation.cancel_retry.total")
                .description("Total number of cancel retry mutation operations")
                .register(meterRegistry);
        
        // Initialize error counters
        this.mutationErrorCounter = Counter.builder("graphql.mutation.errors.total")
                .description("Total number of mutation errors")
                .register(meterRegistry);
                
        this.validationErrorCounter = Counter.builder("graphql.mutation.validation_errors.total")
                .description("Total number of validation errors")
                .register(meterRegistry);
                
        this.businessRuleErrorCounter = Counter.builder("graphql.mutation.business_rule_errors.total")
                .description("Total number of business rule errors")
                .register(meterRegistry);
        
        // Initialize timers
        this.retryMutationTimer = Timer.builder("graphql.mutation.retry.duration")
                .description("Duration of retry mutation operations")
                .register(meterRegistry);
                
        this.acknowledgeMutationTimer = Timer.builder("graphql.mutation.acknowledge.duration")
                .description("Duration of acknowledge mutation operations")
                .register(meterRegistry);
                
        this.resolveMutationTimer = Timer.builder("graphql.mutation.resolve.duration")
                .description("Duration of resolve mutation operations")
                .register(meterRegistry);
                
        this.cancelRetryMutationTimer = Timer.builder("graphql.mutation.cancel_retry.duration")
                .description("Duration of cancel retry mutation operations")
                .register(meterRegistry);
        
        // Initialize gauges for active operations
        Gauge.builder("graphql.mutation.retry.active", activeRetryOperations, AtomicLong::doubleValue)
                .description("Number of active retry operations")
                .register(meterRegistry);
                
        Gauge.builder("graphql.mutation.acknowledge.active", activeAcknowledgeOperations, AtomicLong::doubleValue)
                .description("Number of active acknowledge operations")
                .register(meterRegistry);
                
        Gauge.builder("graphql.mutation.resolve.active", activeResolveOperations, AtomicLong::doubleValue)
                .description("Number of active resolve operations")
                .register(meterRegistry);
                
        Gauge.builder("graphql.mutation.cancel_retry.active", activeCancelOperations, AtomicLong::doubleValue)
                .description("Number of active cancel retry operations")
                .register(meterRegistry);
        
        // Initialize success rate gauges
        initializeSuccessRateGauges();
    }
    
    private void initializeSuccessRateGauges() {
        String[] operations = {"retry", "acknowledge", "resolve", "cancel_retry"};
        
        for (String operation : operations) {
            successCounts.put(operation, new AtomicLong(0));
            totalCounts.put(operation, new AtomicLong(0));
            
            Gauge.builder("graphql.mutation." + operation + ".success_rate", operation, this::calculateSuccessRate)
                    .description("Success rate for " + operation + " mutations")
                    .register(meterRegistry);
        }
    }
    
    private double calculateSuccessRate(String operation) {
        long total = totalCounts.get(operation).get();
        if (total == 0) {
            return 0.0;
        }
        long success = successCounts.get(operation).get();
        return (double) success / total;
    }

    /**
     * Record the start of a retry mutation operation
     */
    public Timer.Sample startRetryOperation() {
        activeRetryOperations.incrementAndGet();
        return Timer.start(meterRegistry);
    }

    /**
     * Record the completion of a retry mutation operation
     */
    public void recordRetryOperation(Timer.Sample sample, boolean success, String errorType) {
        activeRetryOperations.decrementAndGet();
        retryMutationCounter.increment();
        sample.stop(retryMutationTimer);
        
        recordOperationResult("retry", success, errorType);
    }

    /**
     * Record the start of an acknowledge mutation operation
     */
    public Timer.Sample startAcknowledgeOperation() {
        activeAcknowledgeOperations.incrementAndGet();
        return Timer.start(meterRegistry);
    }

    /**
     * Record the completion of an acknowledge mutation operation
     */
    public void recordAcknowledgeOperation(Timer.Sample sample, boolean success, String errorType) {
        activeAcknowledgeOperations.decrementAndGet();
        acknowledgeMutationCounter.increment();
        sample.stop(acknowledgeMutationTimer);
        
        recordOperationResult("acknowledge", success, errorType);
    }

    /**
     * Record the start of a resolve mutation operation
     */
    public Timer.Sample startResolveOperation() {
        activeResolveOperations.incrementAndGet();
        return Timer.start(meterRegistry);
    }

    /**
     * Record the completion of a resolve mutation operation
     */
    public void recordResolveOperation(Timer.Sample sample, boolean success, String errorType) {
        activeResolveOperations.decrementAndGet();
        resolveMutationCounter.increment();
        sample.stop(resolveMutationTimer);
        
        recordOperationResult("resolve", success, errorType);
    }

    /**
     * Record the start of a cancel retry mutation operation
     */
    public Timer.Sample startCancelRetryOperation() {
        activeCancelOperations.incrementAndGet();
        return Timer.start(meterRegistry);
    }

    /**
     * Record the completion of a cancel retry mutation operation
     */
    public void recordCancelRetryOperation(Timer.Sample sample, boolean success, String errorType) {
        activeCancelOperations.decrementAndGet();
        cancelRetryMutationCounter.increment();
        sample.stop(cancelRetryMutationTimer);
        
        recordOperationResult("cancel_retry", success, errorType);
    }
    
    private void recordOperationResult(String operation, boolean success, String errorType) {
        totalCounts.get(operation).incrementAndGet();
        
        if (success) {
            successCounts.get(operation).incrementAndGet();
        } else {
            mutationErrorCounter.increment();
            
            if ("VALIDATION_ERROR".equals(errorType)) {
                validationErrorCounter.increment();
            } else if ("BUSINESS_RULE_ERROR".equals(errorType)) {
                businessRuleErrorCounter.increment();
            }
        }
    }

    /**
     * Record a validation error
     */
    public void recordValidationError(String operation) {
        validationErrorCounter.increment();
        mutationErrorCounter.increment();
        log.debug("Recorded validation error for operation: {}", operation);
    }

    /**
     * Record a business rule error
     */
    public void recordBusinessRuleError(String operation) {
        businessRuleErrorCounter.increment();
        mutationErrorCounter.increment();
        log.debug("Recorded business rule error for operation: {}", operation);
    }

    /**
     * Get current success rate for an operation
     */
    public double getSuccessRate(String operation) {
        return calculateSuccessRate(operation);
    }

    /**
     * Get total operation count for an operation
     */
    public long getTotalOperations(String operation) {
        return totalCounts.getOrDefault(operation, new AtomicLong(0)).get();
    }

    /**
     * Get successful operation count for an operation
     */
    public long getSuccessfulOperations(String operation) {
        return successCounts.getOrDefault(operation, new AtomicLong(0)).get();
    }
}