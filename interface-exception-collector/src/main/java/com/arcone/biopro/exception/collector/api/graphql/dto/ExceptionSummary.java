package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * GraphQL DTO for exception summary statistics.
 * Provides aggregated data for dashboard displays including counts by various
 * dimensions,
 * trend data, and key performance metrics.
 * 
 * Implements requirements:
 * - 4.1: Aggregated exception counts by interface type, status, and severity
 * - 4.2: Time-series data for exception patterns
 * - 4.3: Retry success rates and customer impact statistics
 * - 4.4: Dashboard statistics with 200ms response time
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionSummary {

    /**
     * Total number of exceptions in the queried time range
     */
    private Integer totalExceptions;

    /**
     * Exception counts grouped by interface type
     */
    private List<InterfaceTypeSummary> byInterfaceType;

    /**
     * Exception counts grouped by severity level
     */
    private List<SeveritySummary> bySeverity;

    /**
     * Exception counts grouped by status
     */
    private List<StatusSummary> byStatus;

    /**
     * Time-series trend data for dashboard charts
     */
    private List<TrendDataPoint> trends;

    /**
     * Key performance metrics and KPIs
     */
    private KeyMetrics keyMetrics;

    /**
     * Summary data grouped by interface type
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterfaceTypeSummary {
        private InterfaceType interfaceType;
        private Integer count;
        private Float percentage;
    }

    /**
     * Summary data grouped by severity level
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeveritySummary {
        private ExceptionSeverity severity;
        private Integer count;
        private Float percentage;
    }

    /**
     * Summary data grouped by status
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusSummary {
        private ExceptionStatus status;
        private Integer count;
        private Float percentage;
    }

    /**
     * Time-series data point for trend analysis
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendDataPoint {
        private OffsetDateTime timestamp;
        private Integer count;
        private InterfaceType interfaceType;
    }

    /**
     * Key performance metrics and KPIs
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyMetrics {
        /**
         * Overall retry success rate as a percentage (0.0 to 100.0)
         */
        private Float retrySuccessRate;

        /**
         * Average time to resolve exceptions in hours
         */
        private Float averageResolutionTime;

        /**
         * Number of unique customers impacted by exceptions
         */
        private Integer customerImpactCount;

        /**
         * Number of critical severity exceptions
         */
        private Integer criticalExceptionCount;
    }
}