package com.arcone.biopro.exception.collector.api.graphql.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * GraphQL input type for search operations.
 * Maps to the SearchInput input type defined in the GraphQL schema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchInput {

    @NotBlank(message = "Search query is required")
    @Size(max = 200, message = "Search query cannot exceed 200 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\p{P}\\p{Z}]*$", message = "Search query contains invalid characters")
    private String query;

    @Size(max = 10, message = "Cannot search in more than 10 fields")
    private List<SearchField> fields;

    private Boolean fuzzy;

    /**
     * Enum representing the fields that can be searched.
     */
    public enum SearchField {
        EXCEPTION_REASON,
        TRANSACTION_ID,
        EXTERNAL_ID,
        CUSTOMER_ID,
        LOCATION_CODE,
        OPERATION
    }

    /**
     * Gets the effective fuzzy search setting.
     * Defaults to false if not specified.
     */
    public boolean isEffectiveFuzzy() {
        return fuzzy != null && fuzzy;
    }

    /**
     * Gets the effective search fields.
     * Defaults to searching in exception reason if no fields specified.
     */
    public List<SearchField> getEffectiveFields() {
        if (fields == null || fields.isEmpty()) {
            return List.of(SearchField.EXCEPTION_REASON);
        }
        return fields;
    }
}