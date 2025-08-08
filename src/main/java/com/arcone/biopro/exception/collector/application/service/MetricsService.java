package com.arcone.biopro.exception.collector.application.service;

import org.springframework.stereotype.Service;

/**
 * Stub MetricsService for compilation
 */
@Service
public class MetricsService {

    public void recordExceptionProcessed(String interfaceType) {
        // Stub implementation
    }

    public void recordRetryAttempt(String interfaceType, boolean success) {
        // Stub implementation
    }

    public void recordAlertGenerated(String alertLevel) {
        // Stub implementation
    }
}