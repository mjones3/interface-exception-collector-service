package com.arcone.biopro.exception.collector.application.service;

import lombok.Builder;
import lombok.Data;

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