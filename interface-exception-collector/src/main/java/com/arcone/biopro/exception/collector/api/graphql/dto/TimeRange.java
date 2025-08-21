package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.api.graphql.validation.ValidDateRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * GraphQL input type for specifying time ranges in summary queries.
 * Supports both predefined periods and custom date ranges.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeRange {

    /**
     * Predefined time period for the query
     */
    @NotNull(message = "Time period is required")
    private TimePeriod period;

    /**
     * Custom date range when period is set to CUSTOM
     */
    @Valid
    @ValidDateRange(maxRangeDays = 365, allowFuture = false, message = "Custom date range is invalid")
    private ExceptionFilters.DateRangeInput customRange;

    /**
     * Predefined time periods for summary queries
     */
    public enum TimePeriod {
        LAST_HOUR,
        LAST_24_HOURS,
        LAST_7_DAYS,
        LAST_30_DAYS,
        CUSTOM
    }

    /**
     * Gets the effective start date based on the period or custom range
     */
    public OffsetDateTime getEffectiveStartDate() {
        if (period == TimePeriod.CUSTOM && customRange != null) {
            return customRange.getFrom();
        }

        OffsetDateTime now = OffsetDateTime.now();
        switch (period) {
            case LAST_HOUR:
                return now.minusHours(1);
            case LAST_24_HOURS:
                return now.minusDays(1);
            case LAST_7_DAYS:
                return now.minusDays(7);
            case LAST_30_DAYS:
                return now.minusDays(30);
            default:
                return now.minusDays(1); // Default to last 24 hours
        }
    }

    /**
     * Gets the effective end date based on the period or custom range
     */
    public OffsetDateTime getEffectiveEndDate() {
        if (period == TimePeriod.CUSTOM && customRange != null) {
            return customRange.getTo();
        }

        return OffsetDateTime.now();
    }
}