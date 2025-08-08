package com.arcone.biopro.exception.collector.application.service;

import lombok.Builder;
import lombok.Data;

/**
 * Result object for data cleanup operations.
 */
@Data
@Builder
public class CleanupResult {
    private boolean success;
    private int deletedExceptions;
    private int deletedRetryAttempts;
    private int preservedCritical;
    private String duration;
    private String errorMessage;
}

/**
 * Statistics about data that can be cleaned up.
 */
@Data
@Builder
class CleanupStatistic {
    private String metricName;
    private Long metricValue;
    private String recommendation;
}