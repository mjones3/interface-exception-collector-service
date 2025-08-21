package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionSummary;
import com.arcone.biopro.exception.collector.api.graphql.dto.TimeRange;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service for generating exception summary statistics and aggregations.
 * Provides data for dashboard displays including counts by various dimensions,
 * trend analysis, and key performance metrics.
 * 
 * Implements requirements:
 * - 4.1: Aggregated exception counts by interface type, status, and severity
 * - 4.2: Time-series data for exception patterns
 * - 4.3: Retry success rates and customer impact statistics
 * - 4.4: Dashboard statistics with 200ms response time
 * - 5.4: Redis caching with appropriate TTL values
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SummaryService {

    private final InterfaceExceptionRepository exceptionRepository;
    private final RetryAttemptRepository retryAttemptRepository;

    /**
     * Generates comprehensive exception summary statistics for the specified time
     * range and filters.
     * Results are cached in Redis with a 5-minute TTL for optimal performance.
     * 
     * @param timeRange the time range for the summary
     * @param filters   optional filters to apply to the data
     * @return CompletableFuture containing the exception summary
     */
    @Cacheable(value = "exception-summary", key = "#timeRange.hashCode() + '_' + (#filters != null ? #filters.hashCode() : 'null')")
    public CompletableFuture<ExceptionSummary> generateSummary(TimeRange timeRange, ExceptionFilters filters) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Generating exception summary for time range: {}, filters: {}", timeRange, filters);

                OffsetDateTime startDate = timeRange.getEffectiveStartDate();
                OffsetDateTime endDate = timeRange.getEffectiveEndDate();

                // Generate all summary components in parallel
                CompletableFuture<Integer> totalCountFuture = getTotalExceptionCount(startDate, endDate, filters);
                CompletableFuture<List<ExceptionSummary.InterfaceTypeSummary>> interfaceTypeFuture = getInterfaceTypeSummary(
                        startDate, endDate, filters);
                CompletableFuture<List<ExceptionSummary.SeveritySummary>> severityFuture = getSeveritySummary(startDate,
                        endDate, filters);
                CompletableFuture<List<ExceptionSummary.StatusSummary>> statusFuture = getStatusSummary(startDate,
                        endDate, filters);
                CompletableFuture<List<ExceptionSummary.TrendDataPoint>> trendsFuture = getTrendData(startDate, endDate,
                        filters);
                CompletableFuture<ExceptionSummary.KeyMetrics> keyMetricsFuture = getKeyMetrics(startDate, endDate,
                        filters);

                // Wait for all futures to complete and build the summary
                return ExceptionSummary.builder()
                        .totalExceptions(totalCountFuture.join())
                        .byInterfaceType(interfaceTypeFuture.join())
                        .bySeverity(severityFuture.join())
                        .byStatus(statusFuture.join())
                        .trends(trendsFuture.join())
                        .keyMetrics(keyMetricsFuture.join())
                        .build();

            } catch (Exception e) {
                log.error("Error generating exception summary", e);
                throw new RuntimeException("Failed to generate exception summary: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Gets the total count of exceptions for the specified time range and filters.
     */
    private CompletableFuture<Integer> getTotalExceptionCount(OffsetDateTime startDate, OffsetDateTime endDate,
            ExceptionFilters filters) {
        return CompletableFuture.supplyAsync(() -> {
            long count = exceptionRepository.countByTimestampBetween(startDate, endDate);
            return Math.toIntExact(count);
        });
    }

    /**
     * Generates summary statistics grouped by interface type.
     * Uses materialized view for optimal performance when available.
     */
    @Cacheable(value = "interface-summary", key = "#startDate.toString() + '_' + #endDate.toString() + '_' + (#filters != null ? #filters.hashCode() : 'null')")
    private CompletableFuture<List<ExceptionSummary.InterfaceTypeSummary>> getInterfaceTypeSummary(
            OffsetDateTime startDate, OffsetDateTime endDate, ExceptionFilters filters) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Try to use materialized view first
                List<Object[]> mvData = exceptionRepository.getSummaryStatisticsFromMaterializedView(startDate,
                        endDate);

                if (!mvData.isEmpty()) {
                    return processInterfaceTypeSummaryFromMV(mvData);
                }
            } catch (Exception e) {
                log.warn("Error using materialized view for interface type summary, falling back to direct query", e);
            }

            // Fallback to direct queries
            return getInterfaceTypeSummaryDirect(startDate, endDate);
        });
    }

    /**
     * Process interface type summary from materialized view data.
     */
    private List<ExceptionSummary.InterfaceTypeSummary> processInterfaceTypeSummaryFromMV(List<Object[]> mvData) {
        // Group by interface type and sum counts
        var groupedData = mvData.stream()
                .collect(Collectors.groupingBy(
                        row -> (String) row[0], // interface_type
                        Collectors.summingLong(row -> ((Number) row[3]).longValue()) // total_count
                ));

        long totalCount = groupedData.values().stream().mapToLong(Long::longValue).sum();

        return groupedData.entrySet().stream()
                .map(entry -> {
                    String interfaceTypeStr = entry.getKey();
                    Long count = entry.getValue();
                    float percentage = totalCount > 0 ? (float) count / totalCount * 100 : 0;

                    return ExceptionSummary.InterfaceTypeSummary.builder()
                            .interfaceType(InterfaceType.valueOf(interfaceTypeStr))
                            .count(Math.toIntExact(count))
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Direct query fallback for interface type summary.
     */
    private List<ExceptionSummary.InterfaceTypeSummary> getInterfaceTypeSummaryDirect(
            OffsetDateTime startDate, OffsetDateTime endDate) {

        List<ExceptionSummary.InterfaceTypeSummary> summaries = new ArrayList<>();
        long totalCount = exceptionRepository.countByTimestampBetween(startDate, endDate);

        for (InterfaceType interfaceType : InterfaceType.values()) {
            long count = getCountByInterfaceTypeAndDateRange(interfaceType, startDate, endDate);
            float percentage = totalCount > 0 ? (float) count / totalCount * 100 : 0;

            summaries.add(ExceptionSummary.InterfaceTypeSummary.builder()
                    .interfaceType(interfaceType)
                    .count(Math.toIntExact(count))
                    .percentage(percentage)
                    .build());
        }

        return summaries;
    }

    /**
     * Generates summary statistics grouped by severity level.
     * Uses materialized view for optimal performance when available.
     */
    @Cacheable(value = "severity-summary", key = "#startDate.toString() + '_' + #endDate.toString() + '_' + (#filters != null ? #filters.hashCode() : 'null')")
    private CompletableFuture<List<ExceptionSummary.SeveritySummary>> getSeveritySummary(
            OffsetDateTime startDate, OffsetDateTime endDate, ExceptionFilters filters) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Try to use materialized view first
                List<Object[]> mvData = exceptionRepository.getSummaryStatisticsFromMaterializedView(startDate,
                        endDate);

                if (!mvData.isEmpty()) {
                    return processSeveritySummaryFromMV(mvData);
                }
            } catch (Exception e) {
                log.warn("Error using materialized view for severity summary, falling back to direct query", e);
            }

            // Fallback to direct queries
            return getSeveritySummaryDirect(startDate, endDate);
        });
    }

    /**
     * Process severity summary from materialized view data.
     */
    private List<ExceptionSummary.SeveritySummary> processSeveritySummaryFromMV(List<Object[]> mvData) {
        // Group by severity and sum counts
        var groupedData = mvData.stream()
                .collect(Collectors.groupingBy(
                        row -> (String) row[2], // severity
                        Collectors.summingLong(row -> ((Number) row[3]).longValue()) // total_count
                ));

        long totalCount = groupedData.values().stream().mapToLong(Long::longValue).sum();

        return groupedData.entrySet().stream()
                .map(entry -> {
                    String severityStr = entry.getKey();
                    Long count = entry.getValue();
                    float percentage = totalCount > 0 ? (float) count / totalCount * 100 : 0;

                    return ExceptionSummary.SeveritySummary.builder()
                            .severity(ExceptionSeverity.valueOf(severityStr))
                            .count(Math.toIntExact(count))
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Direct query fallback for severity summary.
     */
    private List<ExceptionSummary.SeveritySummary> getSeveritySummaryDirect(
            OffsetDateTime startDate, OffsetDateTime endDate) {

        List<ExceptionSummary.SeveritySummary> summaries = new ArrayList<>();
        long totalCount = exceptionRepository.countByTimestampBetween(startDate, endDate);

        for (ExceptionSeverity severity : ExceptionSeverity.values()) {
            long count = getCountBySeverityAndDateRange(severity, startDate, endDate);
            float percentage = totalCount > 0 ? (float) count / totalCount * 100 : 0;

            summaries.add(ExceptionSummary.SeveritySummary.builder()
                    .severity(severity)
                    .count(Math.toIntExact(count))
                    .percentage(percentage)
                    .build());
        }

        return summaries;
    }

    /**
     * Generates summary statistics grouped by status.
     * Uses materialized view for optimal performance when available.
     */
    @Cacheable(value = "status-summary", key = "#startDate.toString() + '_' + #endDate.toString() + '_' + (#filters != null ? #filters.hashCode() : 'null')")
    private CompletableFuture<List<ExceptionSummary.StatusSummary>> getStatusSummary(
            OffsetDateTime startDate, OffsetDateTime endDate, ExceptionFilters filters) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Try to use materialized view first
                List<Object[]> mvData = exceptionRepository.getSummaryStatisticsFromMaterializedView(startDate,
                        endDate);

                if (!mvData.isEmpty()) {
                    return processStatusSummaryFromMV(mvData);
                }
            } catch (Exception e) {
                log.warn("Error using materialized view for status summary, falling back to direct query", e);
            }

            // Fallback to direct queries
            return getStatusSummaryDirect(startDate, endDate);
        });
    }

    /**
     * Process status summary from materialized view data.
     */
    private List<ExceptionSummary.StatusSummary> processStatusSummaryFromMV(List<Object[]> mvData) {
        // Group by status and sum counts
        var groupedData = mvData.stream()
                .collect(Collectors.groupingBy(
                        row -> (String) row[1], // status
                        Collectors.summingLong(row -> ((Number) row[3]).longValue()) // total_count
                ));

        long totalCount = groupedData.values().stream().mapToLong(Long::longValue).sum();

        return groupedData.entrySet().stream()
                .map(entry -> {
                    String statusStr = entry.getKey();
                    Long count = entry.getValue();
                    float percentage = totalCount > 0 ? (float) count / totalCount * 100 : 0;

                    return ExceptionSummary.StatusSummary.builder()
                            .status(ExceptionStatus.valueOf(statusStr))
                            .count(Math.toIntExact(count))
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Direct query fallback for status summary.
     */
    private List<ExceptionSummary.StatusSummary> getStatusSummaryDirect(
            OffsetDateTime startDate, OffsetDateTime endDate) {

        List<ExceptionSummary.StatusSummary> summaries = new ArrayList<>();
        long totalCount = exceptionRepository.countByTimestampBetween(startDate, endDate);

        for (ExceptionStatus status : ExceptionStatus.values()) {
            long count = getCountByStatusAndDateRange(status, startDate, endDate);
            float percentage = totalCount > 0 ? (float) count / totalCount * 100 : 0;

            summaries.add(ExceptionSummary.StatusSummary.builder()
                    .status(status)
                    .count(Math.toIntExact(count))
                    .percentage(percentage)
                    .build());
        }

        return summaries;
    }

    /**
     * Generates time-series trend data for dashboard charts.
     * Uses materialized view for optimal performance.
     */
    @Cacheable(value = "trend-data", key = "#startDate.toString() + '_' + #endDate.toString() + '_' + (#filters != null ? #filters.hashCode() : 'null')")
    private CompletableFuture<List<ExceptionSummary.TrendDataPoint>> getTrendData(
            OffsetDateTime startDate, OffsetDateTime endDate, ExceptionFilters filters) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Determine the appropriate time interval based on the date range
                String interval = determineTimeIntervalString(startDate, endDate);

                // Get trend data from materialized view
                List<Object[]> trendData = exceptionRepository.getTrendDataFromMaterializedView(
                        startDate, endDate, interval);

                return trendData.stream()
                        .map(row -> ExceptionSummary.TrendDataPoint.builder()
                                .timestamp((OffsetDateTime) row[0])
                                .count(((Number) row[1]).intValue())
                                .interfaceType(null) // Overall trend, not specific to interface type
                                .build())
                        .collect(Collectors.toList());

            } catch (Exception e) {
                log.warn("Error getting trend data from materialized view, falling back to direct query", e);
                return getFallbackTrendData(startDate, endDate);
            }
        });
    }

    /**
     * Fallback method for trend data when materialized view is not available.
     */
    private List<ExceptionSummary.TrendDataPoint> getFallbackTrendData(OffsetDateTime startDate,
            OffsetDateTime endDate) {
        List<ExceptionSummary.TrendDataPoint> trendPoints = new ArrayList<>();

        // Determine the appropriate time interval based on the date range
        ChronoUnit interval = determineTimeInterval(startDate, endDate);

        // Generate trend data points
        OffsetDateTime current = startDate;
        while (current.isBefore(endDate)) {
            OffsetDateTime nextInterval = current.plus(1, interval);
            if (nextInterval.isAfter(endDate)) {
                nextInterval = endDate;
            }

            long count = exceptionRepository.countByTimestampBetween(current, nextInterval);

            trendPoints.add(ExceptionSummary.TrendDataPoint.builder()
                    .timestamp(current)
                    .count(Math.toIntExact(count))
                    .interfaceType(null) // Overall trend, not specific to interface type
                    .build());

            current = nextInterval;
        }

        return trendPoints;
    }

    /**
     * Generates key performance metrics and KPIs.
     */
    @Cacheable(value = "key-metrics", key = "#startDate.toString() + '_' + #endDate.toString() + '_' + (#filters != null ? #filters.hashCode() : 'null')")
    private CompletableFuture<ExceptionSummary.KeyMetrics> getKeyMetrics(
            OffsetDateTime startDate, OffsetDateTime endDate, ExceptionFilters filters) {

        return CompletableFuture.supplyAsync(() -> {
            // Calculate retry success rate
            float retrySuccessRate = calculateRetrySuccessRate(startDate, endDate);

            // Calculate average resolution time
            float averageResolutionTime = calculateAverageResolutionTime(startDate, endDate);

            // Count unique customers impacted
            int customerImpactCount = countUniqueCustomersImpacted(startDate, endDate);

            // Count critical exceptions
            int criticalExceptionCount = countCriticalExceptions(startDate, endDate);

            return ExceptionSummary.KeyMetrics.builder()
                    .retrySuccessRate(retrySuccessRate)
                    .averageResolutionTime(averageResolutionTime)
                    .customerImpactCount(customerImpactCount)
                    .criticalExceptionCount(criticalExceptionCount)
                    .build();
        });
    }

    /**
     * Helper method to get count by interface type and date range.
     */
    private long getCountByInterfaceTypeAndDateRange(InterfaceType interfaceType, OffsetDateTime startDate,
            OffsetDateTime endDate) {
        // Use a custom query to count by interface type and date range
        return exceptionRepository.findWithFilters(interfaceType, null, null, null, startDate, endDate, null)
                .getTotalElements();
    }

    /**
     * Helper method to get count by severity and date range.
     */
    private long getCountBySeverityAndDateRange(ExceptionSeverity severity, OffsetDateTime startDate,
            OffsetDateTime endDate) {
        // Use a custom query to count by severity and date range
        return exceptionRepository.findWithFilters(null, null, severity, null, startDate, endDate, null)
                .getTotalElements();
    }

    /**
     * Helper method to get count by status and date range.
     */
    private long getCountByStatusAndDateRange(ExceptionStatus status, OffsetDateTime startDate,
            OffsetDateTime endDate) {
        // Use a custom query to count by status and date range
        return exceptionRepository.findWithFilters(null, status, null, null, startDate, endDate, null)
                .getTotalElements();
    }

    /**
     * Determines the appropriate time interval for trend data based on the date
     * range.
     */
    private ChronoUnit determineTimeInterval(OffsetDateTime startDate, OffsetDateTime endDate) {
        long hours = ChronoUnit.HOURS.between(startDate, endDate);

        if (hours <= 24) {
            return ChronoUnit.HOURS;
        } else if (hours <= 168) { // 7 days
            return ChronoUnit.HOURS;
        } else {
            return ChronoUnit.DAYS;
        }
    }

    /**
     * Determines the appropriate time interval string for materialized view
     * queries.
     */
    private String determineTimeIntervalString(OffsetDateTime startDate, OffsetDateTime endDate) {
        long hours = ChronoUnit.HOURS.between(startDate, endDate);

        if (hours <= 24) {
            return "hour";
        } else if (hours <= 168) { // 7 days
            return "day";
        } else if (hours <= 720) { // 30 days
            return "day";
        } else {
            return "week";
        }
    }

    /**
     * Calculates the retry success rate as a percentage.
     */
    private float calculateRetrySuccessRate(OffsetDateTime startDate, OffsetDateTime endDate) {
        try {
            // Get total retry attempts in the time range
            long totalRetries = retryAttemptRepository.countByInitiatedAtBetween(startDate, endDate);

            if (totalRetries == 0) {
                return 0.0f;
            }

            // Get successful retry attempts
            long successfulRetries = retryAttemptRepository.countByInitiatedAtBetweenAndResultSuccess(startDate,
                    endDate, true);

            return (float) successfulRetries / totalRetries * 100;
        } catch (Exception e) {
            log.warn("Error calculating retry success rate, returning 0", e);
            return 0.0f;
        }
    }

    /**
     * Calculates the average resolution time in hours.
     */
    private float calculateAverageResolutionTime(OffsetDateTime startDate, OffsetDateTime endDate) {
        try {
            // Get resolved exceptions within the date range
            Double averageResolutionTime = exceptionRepository.getAverageResolutionTime(startDate, endDate);

            if (averageResolutionTime == null) {
                return 0.0f;
            }

            // Return the average resolution time
            return averageResolutionTime.floatValue();

        } catch (Exception e) {
            log.warn("Error calculating average resolution time, returning default", e);
            return 0.0f;
        }
    }

    /**
     * Counts the number of unique customers impacted by exceptions.
     */
    private int countUniqueCustomersImpacted(OffsetDateTime startDate, OffsetDateTime endDate) {
        try {
            long count = exceptionRepository.countUniqueCustomersImpacted(startDate, endDate);
            return Math.toIntExact(count);
        } catch (Exception e) {
            log.warn("Error counting unique customers impacted, returning 0", e);
            return 0;
        }
    }

    /**
     * Counts the number of critical severity exceptions.
     */
    private int countCriticalExceptions(OffsetDateTime startDate, OffsetDateTime endDate) {
        try {
            long count = getCountBySeverityAndDateRange(ExceptionSeverity.CRITICAL, startDate, endDate);
            return Math.toIntExact(count);
        } catch (Exception e) {
            log.warn("Error counting critical exceptions, returning 0", e);
            return 0;
        }
    }
}