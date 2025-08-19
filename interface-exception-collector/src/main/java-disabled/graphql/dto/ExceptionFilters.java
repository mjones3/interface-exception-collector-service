package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.api.graphql.validation.ValidDateRange;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * GraphQL input type for filtering exception queries.
 * Maps to the ExceptionFilters input type defined in the GraphQL schema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionFilters {

    @Size(max = 10, message = "Cannot filter by more than 10 interface types")
    private List<InterfaceType> interfaceTypes;

    @Size(max = 10, message = "Cannot filter by more than 10 statuses")
    private List<ExceptionStatus> statuses;

    @Size(max = 10, message = "Cannot filter by more than 10 severities")
    private List<ExceptionSeverity> severities;

    @Size(max = 10, message = "Cannot filter by more than 10 categories")
    private List<ExceptionCategory> categories;

    @Valid
    @ValidDateRange(maxRangeDays = 90, message = "Date range cannot exceed 90 days")
    private DateRangeInput dateRange;

    @Size(max = 50, message = "Cannot filter by more than 50 customer IDs")
    private List<@Pattern(regexp = "^[A-Za-z0-9\\-_]{1,20}$", message = "Invalid customer ID format") String> customerIds;

    @Size(max = 50, message = "Cannot filter by more than 50 location codes")
    private List<@Pattern(regexp = "^[A-Za-z0-9\\-_]{1,10}$", message = "Invalid location code format") String> locationCodes;

    @Size(max = 100, message = "Search term cannot exceed 100 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\p{P}\\p{Z}]*$", message = "Search term contains invalid characters")
    private String searchTerm;

    private Boolean excludeResolved;
    private Boolean retryable;

    @Size(max = 50, message = "Acknowledged by field cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\-_.@]*$", message = "Invalid acknowledged by format")
    private String acknowledgedBy;

    private Boolean hasRetries;

    /**
     * Date range input for filtering by timestamp.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateRangeInput {
        private OffsetDateTime from;
        private OffsetDateTime to;

        /**
         * Validates that the date range is valid (from <= to).
         */
        public boolean isValid() {
            if (from == null || to == null) {
                return true; // Individual null validation handled elsewhere
            }
            return !from.isAfter(to);
        }

        /**
         * Gets the duration of the date range in days.
         */
        public long getDurationInDays() {
            if (from == null || to == null) {
                return 0;
            }
            return java.time.Duration.between(from, to).toDays();
        }
    }
}