package com.arcone.biopro.exception.collector.api.graphql.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO for raw summary statistics data from materialized view.
 * Used internally for processing aggregated data before converting to
 * ExceptionSummary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryStatistics {

    private String interfaceType;
    private String status;
    private String severity;
    private Long totalCount;
    private Long uniqueCustomers;
    private Long criticalCount;
    private Double avgResolutionHours;
    private OffsetDateTime firstOccurrence;
    private OffsetDateTime lastOccurrence;
}