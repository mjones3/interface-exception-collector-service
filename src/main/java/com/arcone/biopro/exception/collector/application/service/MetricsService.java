package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Stub MetricsService for compilation
 */
@Service
public class MetricsService {

    public void recordExceptionProcessed(InterfaceType interfaceType, ExceptionSeverity severity) {
        // Stub implementation
    }

    public void recordExceptionProcessingTime(Duration duration, InterfaceType interfaceType) {
        // Stub implementation
    }

    public void recordRetryAttempt(String interfaceType, boolean success) {
        // Stub implementation
    }

    public void recordRetryOperation(InterfaceType interfaceType, boolean success) {
        // Stub implementation
    }

    public void recordRetryOperationTime(Duration duration, InterfaceType interfaceType, boolean success) {
        // Stub implementation
    }

    public void recordApiResponseTime(Duration duration, String endpoint, String method, int statusCode) {
        // Stub implementation
    }

    public void recordDatabaseOperation(Duration duration, String operation, boolean success) {
        // Stub implementation
    }

    public void recordKafkaMessageConsumed(String topic, boolean success) {
        // Stub implementation
    }

    public void recordKafkaMessageProduced(String topic, boolean success) {
        // Stub implementation
    }

    public void recordExternalServiceCall(Duration duration, String service, boolean success) {
        // Stub implementation
    }

    public void recordDataValidationMetrics(ValidationResult result) {
        // Stub implementation
    }

    public void recordCriticalAlert(String alertType, InterfaceType interfaceType) {
        // Stub implementation
    }

    public void initializeGaugeMetrics() {
        // Stub implementation
    }

    public void resetDailyCounters() {
        // Stub implementation
    }

    public long getActiveExceptionsCount() {
        return 0; // Stub implementation
    }

    public long getTotalExceptionsToday() {
        return 0; // Stub implementation
    }

    public long getCriticalExceptionsToday() {
        return 0; // Stub implementation
    }

    public double getAverageResolutionTimeHours() {
        return 0.0; // Stub implementation
    }

    public void recordAlertGenerated(String alertLevel) {
        // Stub implementation
    }
}