package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Input DTO for resolving a single exception via GraphQL.
 * Contains validation rules and business constraints for resolution operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveExceptionInput {

    /**
     * The transaction ID of the exception to resolve.
     * Must be a valid, non-empty string.
     */
    @NotBlank(message = "Transaction ID is required")
    @Size(max = 255, message = "Transaction ID must not exceed 255 characters")
    private String transactionId;

    /**
     * The method used to resolve the exception.
     * Must be one of the predefined resolution methods.
     */
    @NotNull(message = "Resolution method is required")
    private ResolutionMethod resolutionMethod;

    /**
     * Optional detailed notes about the resolution.
     * Can contain technical details, steps taken, or other relevant information.
     */
    @Size(max = 2000, message = "Resolution notes must not exceed 2000 characters")
    private String resolutionNotes;
}