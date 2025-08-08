package com.arcone.biopro.exception.collector.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic DTO for paginated responses.
 * Provides consistent pagination metadata across all list endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response wrapper")
public class PagedResponse<T> {

    @Schema(description = "List of items in the current page")
    private List<T> content;

    @Schema(description = "Current page number (0-based)", example = "0")
    private Integer page;

    @Schema(description = "Number of items per page", example = "20")
    private Integer size;

    @Schema(description = "Total number of items across all pages", example = "150")
    private Long totalElements;

    @Schema(description = "Total number of pages", example = "8")
    private Integer totalPages;

    @Schema(description = "Whether this is the first page", example = "true")
    private Boolean first;

    @Schema(description = "Whether this is the last page", example = "false")
    private Boolean last;

    @Schema(description = "Number of items in the current page", example = "20")
    private Integer numberOfElements;

    @Schema(description = "Whether the page is empty", example = "false")
    private Boolean empty;
}