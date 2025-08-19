package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.api.graphql.validation.ValidPagination;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * GraphQL input type for cursor-based pagination.
 * Maps to the PaginationInput input type defined in the GraphQL schema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidPagination(maxPageSize = 100, defaultPageSize = 20)
public class PaginationInput {

    @Min(value = 1, message = "First must be at least 1")
    @Max(value = 100, message = "First cannot exceed 100")
    private Integer first;

    private String after;

    @Min(value = 1, message = "Last must be at least 1")
    @Max(value = 100, message = "Last cannot exceed 100")
    private Integer last;

    private String before;

    /**
     * Gets the effective page size for the pagination request.
     * Defaults to 20 if neither first nor last is specified.
     * 
     * @return the page size to use
     */
    public int getEffectivePageSize() {
        if (first != null && first > 0) {
            return Math.min(first, 100); // Cap at 100 for performance
        }
        if (last != null && last > 0) {
            return Math.min(last, 100); // Cap at 100 for performance
        }
        return 20; // Default page size
    }

    /**
     * Determines if this is a forward pagination request.
     * 
     * @return true if paginating forward (using first/after)
     */
    public boolean isForwardPagination() {
        return first != null || after != null;
    }

    /**
     * Determines if this is a backward pagination request.
     * 
     * @return true if paginating backward (using last/before)
     */
    public boolean isBackwardPagination() {
        return last != null || before != null;
    }
}