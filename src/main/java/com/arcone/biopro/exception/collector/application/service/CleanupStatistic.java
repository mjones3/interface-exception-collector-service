package com.arcone.biopro.exception.collector.application.service;

import lombok.Builder;
import lombok.Data;

/**
 * Statistics about data that can be cleaned up.
 */
@Data
@Builder
public class CleanupStatistic {
    private String metricName;
    private Long metricValue;
    private String recommendation;
}