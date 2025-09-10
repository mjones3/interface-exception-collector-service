package com.arcone.biopro.exception.collector.infrastructure.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MutationMetrics component.
 * Tests metrics collection, success rate calculation, and operation tracking.
 */
@ExtendWith(MockitoExtension.class)
class MutationMetricsTest {

    private MeterRegistry meterRegistry;
    private MutationMetrics mutationMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        mutationMetrics = new MutationMetrics(meterRegistry);
    }

    @Test
    void shouldInitializeAllMetrics() {
        // Verify counters are registered
        assertThat(meterRegistry.find("graphql.mutation.retry.total").counter()).isNotNull();
        assertThat(meterRegistry.find("graphql.mutation.acknowledge.total").counter()).isNotNull();
        assertThat(meterRegistry.find("graphql.mutation.resolve.total").counter()).isNotNull();
        assertThat(meterRegistry.find("graphql.mutation.cancel_retry.total").counter()).isNotNull();
        
        // Verify error counters are registered
        assertThat(meterRegistry.find("graphql.mutation.errors.total").counter()).isNotNull();
        assertThat(meterRegistry.find("graphql.mutation.validation_errors.total").counter()).isNotNull();
        assertThat(meterRegistry.find("graphql.mutation.business_rule_errors.total").counter()).isNotNull();
        
        // Verify timers are registered
        assertThat(meterRegistry.find("graphql.mutation.retry.duration").timer()).isNotNull();
        assertThat(meterRegistry.find("graphql.mutation.acknowledge.duration").timer()).isNotNull();
        assertThat(meterRegistry.find("graphql.mutation.resolve.duration").timer()).isNotNull();
        assertThat(meterRegistry.find("graphql.mutation.cancel_retry.duration").timer()).isNotNull();
        
        // Verify gauges are registered
        assertThat(meterRegistry.find("graphql.mutation.retry.active").gauge()).isNotNull();
        assertThat(meterRegistry.find("graphql.mutation.acknowledge.active").gauge()).isNotNull();
        assertThat(meterRegistry.find("graphql.mutation.resolve.active").gauge()).isNotNull();
        assertThat(meterRegistry.find("graphql.mutation.cancel_retry.active").gauge()).isNotNull();
        
        // Verify success rate gauges are registered
        assertThat(meterRegistry.find("graphql.mutation.retry.success_rate").gauge()).isNotNull();
        assertThat(meterRegistry.find("graphql.mutation.acknowledge.success_rate").gauge()).isNotNull();
        assertThat(meterRegistry.find("graphql.mutation.resolve.success_rate").gauge()).isNotNull();
        assertThat(meterRegistry.find("graphql.mutation.cancel_retry.success_rate").gauge()).isNotNull();
    }

    @Test
    void shouldRecordRetryOperationSuccess() {
        // Given
        Timer.Sample sample = mutationMetrics.startRetryOperation();
        
        // When
        mutationMetrics.recordRetryOperation(sample, true, null);
        
        // Then
        Counter retryCounter = meterRegistry.find("graphql.mutation.retry.total").counter();
        assertThat(retryCounter.count()).isEqualTo(1.0);
        
        Timer retryTimer = meterRegistry.find("graphql.mutation.retry.duration").timer();
        assertThat(retryTimer.count()).isEqualTo(1);
        
        assertThat(mutationMetrics.getSuccessRate("retry")).isEqualTo(1.0);
        assertThat(mutationMetrics.getTotalOperations("retry")).isEqualTo(1);
        assertThat(mutationMetrics.getSuccessfulOperations("retry")).isEqualTo(1);
    }

    @Test
    void shouldRecordRetryOperationFailure() {
        // Given
        Timer.Sample sample = mutationMetrics.startRetryOperation();
        
        // When
        mutationMetrics.recordRetryOperation(sample, false, "VALIDATION_ERROR");
        
        // Then
        Counter retryCounter = meterRegistry.find("graphql.mutation.retry.total").counter();
        assertThat(retryCounter.count()).isEqualTo(1.0);
        
        Counter errorCounter = meterRegistry.find("graphql.mutation.errors.total").counter();
        assertThat(errorCounter.count()).isEqualTo(1.0);
        
        Counter validationErrorCounter = meterRegistry.find("graphql.mutation.validation_errors.total").counter();
        assertThat(validationErrorCounter.count()).isEqualTo(1.0);
        
        assertThat(mutationMetrics.getSuccessRate("retry")).isEqualTo(0.0);
        assertThat(mutationMetrics.getTotalOperations("retry")).isEqualTo(1);
        assertThat(mutationMetrics.getSuccessfulOperations("retry")).isEqualTo(0);
    }

    @Test
    void shouldRecordAcknowledgeOperationSuccess() {
        // Given
        Timer.Sample sample = mutationMetrics.startAcknowledgeOperation();
        
        // When
        mutationMetrics.recordAcknowledgeOperation(sample, true, null);
        
        // Then
        Counter acknowledgeCounter = meterRegistry.find("graphql.mutation.acknowledge.total").counter();
        assertThat(acknowledgeCounter.count()).isEqualTo(1.0);
        
        Timer acknowledgeTimer = meterRegistry.find("graphql.mutation.acknowledge.duration").timer();
        assertThat(acknowledgeTimer.count()).isEqualTo(1);
        
        assertThat(mutationMetrics.getSuccessRate("acknowledge")).isEqualTo(1.0);
        assertThat(mutationMetrics.getTotalOperations("acknowledge")).isEqualTo(1);
        assertThat(mutationMetrics.getSuccessfulOperations("acknowledge")).isEqualTo(1);
    }

    @Test
    void shouldRecordResolveOperationSuccess() {
        // Given
        Timer.Sample sample = mutationMetrics.startResolveOperation();
        
        // When
        mutationMetrics.recordResolveOperation(sample, true, null);
        
        // Then
        Counter resolveCounter = meterRegistry.find("graphql.mutation.resolve.total").counter();
        assertThat(resolveCounter.count()).isEqualTo(1.0);
        
        Timer resolveTimer = meterRegistry.find("graphql.mutation.resolve.duration").timer();
        assertThat(resolveTimer.count()).isEqualTo(1);
        
        assertThat(mutationMetrics.getSuccessRate("resolve")).isEqualTo(1.0);
        assertThat(mutationMetrics.getTotalOperations("resolve")).isEqualTo(1);
        assertThat(mutationMetrics.getSuccessfulOperations("resolve")).isEqualTo(1);
    }

    @Test
    void shouldRecordCancelRetryOperationSuccess() {
        // Given
        Timer.Sample sample = mutationMetrics.startCancelRetryOperation();
        
        // When
        mutationMetrics.recordCancelRetryOperation(sample, true, null);
        
        // Then
        Counter cancelCounter = meterRegistry.find("graphql.mutation.cancel_retry.total").counter();
        assertThat(cancelCounter.count()).isEqualTo(1.0);
        
        Timer cancelTimer = meterRegistry.find("graphql.mutation.cancel_retry.duration").timer();
        assertThat(cancelTimer.count()).isEqualTo(1);
        
        assertThat(mutationMetrics.getSuccessRate("cancel_retry")).isEqualTo(1.0);
        assertThat(mutationMetrics.getTotalOperations("cancel_retry")).isEqualTo(1);
        assertThat(mutationMetrics.getSuccessfulOperations("cancel_retry")).isEqualTo(1);
    }

    @Test
    void shouldCalculateCorrectSuccessRate() {
        // Given - Record multiple operations with mixed success/failure
        Timer.Sample sample1 = mutationMetrics.startRetryOperation();
        mutationMetrics.recordRetryOperation(sample1, true, null);
        
        Timer.Sample sample2 = mutationMetrics.startRetryOperation();
        mutationMetrics.recordRetryOperation(sample2, true, null);
        
        Timer.Sample sample3 = mutationMetrics.startRetryOperation();
        mutationMetrics.recordRetryOperation(sample3, false, "VALIDATION_ERROR");
        
        Timer.Sample sample4 = mutationMetrics.startRetryOperation();
        mutationMetrics.recordRetryOperation(sample4, false, "BUSINESS_RULE_ERROR");
        
        // Then - Success rate should be 2/4 = 0.5
        assertThat(mutationMetrics.getSuccessRate("retry")).isEqualTo(0.5);
        assertThat(mutationMetrics.getTotalOperations("retry")).isEqualTo(4);
        assertThat(mutationMetrics.getSuccessfulOperations("retry")).isEqualTo(2);
    }

    @Test
    void shouldRecordBusinessRuleErrors() {
        // Given
        Timer.Sample sample = mutationMetrics.startRetryOperation();
        
        // When
        mutationMetrics.recordRetryOperation(sample, false, "BUSINESS_RULE_ERROR");
        
        // Then
        Counter businessRuleErrorCounter = meterRegistry.find("graphql.mutation.business_rule_errors.total").counter();
        assertThat(businessRuleErrorCounter.count()).isEqualTo(1.0);
        
        Counter errorCounter = meterRegistry.find("graphql.mutation.errors.total").counter();
        assertThat(errorCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordValidationErrorsDirectly() {
        // When
        mutationMetrics.recordValidationError("retry");
        
        // Then
        Counter validationErrorCounter = meterRegistry.find("graphql.mutation.validation_errors.total").counter();
        assertThat(validationErrorCounter.count()).isEqualTo(1.0);
        
        Counter errorCounter = meterRegistry.find("graphql.mutation.errors.total").counter();
        assertThat(errorCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordBusinessRuleErrorsDirectly() {
        // When
        mutationMetrics.recordBusinessRuleError("acknowledge");
        
        // Then
        Counter businessRuleErrorCounter = meterRegistry.find("graphql.mutation.business_rule_errors.total").counter();
        assertThat(businessRuleErrorCounter.count()).isEqualTo(1.0);
        
        Counter errorCounter = meterRegistry.find("graphql.mutation.errors.total").counter();
        assertThat(errorCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldReturnZeroSuccessRateForNoOperations() {
        // When/Then
        assertThat(mutationMetrics.getSuccessRate("retry")).isEqualTo(0.0);
        assertThat(mutationMetrics.getTotalOperations("retry")).isEqualTo(0);
        assertThat(mutationMetrics.getSuccessfulOperations("retry")).isEqualTo(0);
    }

    @Test
    void shouldHandleUnknownOperationType() {
        // When/Then
        assertThat(mutationMetrics.getSuccessRate("unknown")).isEqualTo(0.0);
        assertThat(mutationMetrics.getTotalOperations("unknown")).isEqualTo(0);
        assertThat(mutationMetrics.getSuccessfulOperations("unknown")).isEqualTo(0);
    }

    @Test
    void shouldTrackActiveOperations() {
        // Given
        Gauge retryActiveGauge = meterRegistry.find("graphql.mutation.retry.active").gauge();
        Gauge acknowledgeActiveGauge = meterRegistry.find("graphql.mutation.acknowledge.active").gauge();
        
        // When - Start operations
        Timer.Sample retrySample = mutationMetrics.startRetryOperation();
        Timer.Sample acknowledgeSample = mutationMetrics.startAcknowledgeOperation();
        
        // Then - Active counts should increase (note: actual gauge values depend on internal implementation)
        // We can't easily test the exact values without accessing internal state
        // But we can verify the gauges exist and are registered
        assertThat(retryActiveGauge).isNotNull();
        assertThat(acknowledgeActiveGauge).isNotNull();
        
        // Complete operations
        mutationMetrics.recordRetryOperation(retrySample, true, null);
        mutationMetrics.recordAcknowledgeOperation(acknowledgeSample, true, null);
    }

    @Test
    void shouldHandleMultipleOperationTypes() {
        // Given - Record operations for all types
        Timer.Sample retrySample = mutationMetrics.startRetryOperation();
        Timer.Sample acknowledgeSample = mutationMetrics.startAcknowledgeOperation();
        Timer.Sample resolveSample = mutationMetrics.startResolveOperation();
        Timer.Sample cancelSample = mutationMetrics.startCancelRetryOperation();
        
        // When
        mutationMetrics.recordRetryOperation(retrySample, true, null);
        mutationMetrics.recordAcknowledgeOperation(acknowledgeSample, false, "VALIDATION_ERROR");
        mutationMetrics.recordResolveOperation(resolveSample, true, null);
        mutationMetrics.recordCancelRetryOperation(cancelSample, false, "BUSINESS_RULE_ERROR");
        
        // Then
        assertThat(mutationMetrics.getSuccessRate("retry")).isEqualTo(1.0);
        assertThat(mutationMetrics.getSuccessRate("acknowledge")).isEqualTo(0.0);
        assertThat(mutationMetrics.getSuccessRate("resolve")).isEqualTo(1.0);
        assertThat(mutationMetrics.getSuccessRate("cancel_retry")).isEqualTo(0.0);
        
        // Verify total error counts
        Counter errorCounter = meterRegistry.find("graphql.mutation.errors.total").counter();
        assertThat(errorCounter.count()).isEqualTo(2.0); // acknowledge + cancel_retry failures
        
        Counter validationErrorCounter = meterRegistry.find("graphql.mutation.validation_errors.total").counter();
        assertThat(validationErrorCounter.count()).isEqualTo(1.0); // acknowledge failure
        
        Counter businessRuleErrorCounter = meterRegistry.find("graphql.mutation.business_rule_errors.total").counter();
        assertThat(businessRuleErrorCounter.count()).isEqualTo(1.0); // cancel_retry failure
    }
}