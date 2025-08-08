package com.arcone.biopro.exception.collector.application.service;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result object for data archival operations.
 */
@Data
@Builder
public class ArchiveResult {
    private boolean success;
    private int archivedExceptions;
    private int archivedRetryAttempts;
    private int preservedCritical;
    private String duration;
    private String errorMessage;
}

/**
 * Result object for data restoration operations.
 */
@Data
@Builder
class RestoreResult {
    private boolean success;
    private int restoredExceptions;
    private int restoredRetryAttempts;
    private List<String> failedRestorations;
    private String errorMessage;
}

/**
 * Statistics about archived vs active data.
 */
@Data
@Builder
class ArchiveStatistic {
    private String metricName;
    private Long mainTableCount;
    private Long archiveTableCount;
    private Long totalCount;
    private Double archivePercentage;
}