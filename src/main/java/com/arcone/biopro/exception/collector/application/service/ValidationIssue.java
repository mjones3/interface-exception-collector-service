package com.arcone.biopro.exception.collector.application.service;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Represents a specific validation issue found during data integrity checks.
 */
@Data
@Builder
public class ValidationIssue {
    private String type;
    private int count;
    private List<String> sampleIssues;
}