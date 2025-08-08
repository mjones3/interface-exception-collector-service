package com.arcone.biopro.exception.collector.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for exception summary statistics response.
 * Implements requirements US-010 for aggregated exception statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aggregated exception statistics and trends")
public class ExceptionSummaryResponse {

    @Schema(description = "Total number of exceptions in the specified time range", example = "150")
    private Long totalExceptions;

    @Schema(description = "Exception counts grouped by interface type")
    private Map<String, Long> byInterfaceType;

    @Schema(description = "Exception counts grouped by severity level")
    private Map<String, Long> bySeverity;

    @Schema(description = "Exception counts grouped by status")
    private Map<String, Long> byStatus;

    @Schema(description = "Daily exception counts for trend analysis")
    private List<DailyTrendResponse> trends;

    /**
     * DTO for daily trend data points.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Daily exception count for trend analysis")
    public static class DailyTrendResponse {

        @Schema(description = "Date in YYYY-MM-DD format", example = "2025-08-04")
        private String date;

        @Schema(description = "Number of exceptions on this date", example = "25")
        private Long count;
    }
}