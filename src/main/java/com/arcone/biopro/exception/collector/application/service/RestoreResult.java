package com.arcone.biopro.exception.collector.application.service;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result object for data restoration operations.
 */
@Data
@Builder
public class RestoreResult {
    private boolean success;
    private int restoredExceptions;
    private int restoredRetryAttempts;
    private List<String> failedRestorations;
    private String errorMessage;
}