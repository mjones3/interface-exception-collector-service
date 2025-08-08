package com.arcone.biopro.exception.collector.application.service;

import lombok.Builder;
import lombok.Data;

/**
 * Statistics about archived vs active data.
 */
@Data
@Builder
public class ArchiveStatistic {
    private String metricName;
    private Long mainTableCount;
    private Long archiveTableCount;
    private Long totalCount;
    private Double archivePercentage;
}