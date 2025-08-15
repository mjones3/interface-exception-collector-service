package com.arcone.biopro.exception.collector.application.service;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 * Result object for data validation operations.
 */
@Data
@Builder
public class ValidationResult {
    private boolean success;
    @Singular
    private List<ValidationIssue> issues;
    private String errorMessage;
}