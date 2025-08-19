package com.arcone.biopro.exception.collector.api.graphql.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GraphQL input type for sorting configuration.
 * Maps to the SortingInput input type defined in the GraphQL schema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortingInput {

    private String field;
    private SortDirection direction;

    /**
     * Sort direction enumeration.
     */
    public enum SortDirection {
        ASC,
        DESC
    }

    /**
     * Gets the effective sort field, defaulting to timestamp if not specified.
     * 
     * @return the field to sort by
     */
    public String getEffectiveField() {
        if (field == null || field.trim().isEmpty()) {
            return "timestamp";
        }
        return field;
    }

    /**
     * Gets the effective sort direction, defaulting to DESC if not specified.
     * 
     * @return the sort direction
     */
    public SortDirection getEffectiveDirection() {
        return direction != null ? direction : SortDirection.DESC;
    }
}