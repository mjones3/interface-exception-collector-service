package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionSummary;
import com.arcone.biopro.exception.collector.api.graphql.dto.TimeRange;
import com.arcone.biopro.exception.collector.api.graphql.service.SummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;

/**
 * GraphQL Query resolver for exception summary statistics.
 * Handles queries for aggregated exception data, trend analysis, and key
 * metrics
 * for dashboard displays.
 * 
 * Implements requirements:
 * - 4.1: Aggregated exception counts by interface type, status, and severity
 * - 4.2: Time-series data for exception patterns
 * - 4.3: Retry success rates and customer impact statistics
 * - 4.4: Dashboard statistics with 200ms response time (95th percentile)
 * - 5.4: Redis caching with 5-minute TTL for summary queries
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@Validated
public class SummaryQueryResolver {

    private final SummaryService summaryService;

    /**
     * Query for retrieving comprehensive exception summary statistics.
     * 
     * Provides:
     * - Total exception counts
     * - Aggregated counts by interface type, status, and severity
     * - Time-series trend data for dashboard charts
     * - Key performance metrics including retry success rates and customer impact
     * 
     * Results are cached in Redis with a 5-minute TTL for optimal performance.
     * Performance target: 200ms response time (95th percentile)
     * 
     * @param timeRange the time range for the summary statistics
     * @param filters   optional filters to apply to the aggregation
     * @return CompletableFuture containing the exception summary
     */
    @QueryMapping
    public CompletableFuture<ExceptionSummary> exceptionSummary(
            @Argument @NotNull(message = "Time range is required") @Valid TimeRange timeRange,
            @Argument @Valid ExceptionFilters filters) {

        log.debug("GraphQL exceptionSummary query - timeRange: {}, filters: {}", timeRange, filters);

        // Validate time range
        validateTimeRange(timeRange);

        return summaryService.generateSummary(timeRange, filters)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Error executing exceptionSummary query", throwable);
                    } else {
                        log.debug("Successfully executed exceptionSummary query, returned {} total exceptions",
                                result.getTotalExceptions());
                    }
                });
    }

    /**
     * Validates the time range input parameters.
     * 
     * @param timeRange the time range to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateTimeRange(TimeRange timeRange) {
        if (timeRange.getPeriod() == TimeRange.TimePeriod.CUSTOM) {
            if (timeRange.getCustomRange() == null) {
                throw new IllegalArgumentException(
                        "Custom date range is required when period is set to CUSTOM");
            }

            if (timeRange.getCustomRange().getFrom() == null || timeRange.getCustomRange().getTo() == null) {
                throw new IllegalArgumentException(
                        "Both 'from' and 'to' dates are required for custom date range");
            }

            if (timeRange.getCustomRange().getFrom().isAfter(timeRange.getCustomRange().getTo())) {
                throw new IllegalArgumentException(
                        "'from' date must be before or equal to 'to' date");
            }

            // Validate maximum date range (e.g., not more than 1 year)
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                    timeRange.getCustomRange().getFrom(),
                    timeRange.getCustomRange().getTo());

            if (daysBetween > 365) {
                throw new IllegalArgumentException(
                        "Date range cannot exceed 365 days");
            }
        }
    }
}