package com.arcone.biopro.exception.collector.application.service;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result object for data migration operations.
 */
@Data
@Builder
public class MigrationResult {
    private boolean success;
    private int migratedCount;
    private int errorCount;
    private List<String> validationErrors;
    private String errorMessage;
}