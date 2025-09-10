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
 * Simplified input DTO for resolving a single exception via GraphQL.
 * Focuses on core resolution data with streamlined validation rules.
 * Enhanced with better resolution method handling and state transition validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveExceptionInput {

    /**
     * The transaction ID of the exception to resolve.
     * Must be a valid, non-empty string with enhanced format validation.
     */
    @NotBlank(message = "Transaction ID is required")
    @Size(max = 255, message = "Transaction ID must not exceed 255 characters")
    private String transactionId;

    /**
     * The method used to resolve the exception.
     * Must be one of the predefined resolution methods and appropriate for the exception state.
     * Enhanced validation ensures proper state transitions.
     */
    @NotNull(message = "Resolution method is required")
    private ResolutionMethod resolutionMethod;

    /**
     * Optional detailed notes about the resolution.
     * Simplified to focus on essential resolution information only.
     * Can contain technical details, steps taken, or other relevant information.
     */
    @Size(max = 2000, message = "Resolution notes must not exceed 2000 characters")
    private String resolutionNotes;

    /**
     * Validates that the transaction ID has a proper format.
     * Enhanced validation for better error handling.
     *
     * @return true if transaction ID format is valid
     */
    public boolean hasValidTransactionIdFormat() {
        return transactionId != null && 
               !transactionId.trim().isEmpty() && 
               transactionId.length() <= 255 &&
               transactionId.matches("^[A-Za-z0-9\\-_]+$");
    }

    /**
     * Checks if resolution notes are provided.
     *
     * @return true if resolution notes are present and not empty
     */
    public boolean hasResolutionNotes() {
        return resolutionNotes != null && !resolutionNotes.trim().isEmpty();
    }

    /**
     * Gets the trimmed resolution notes or null if empty.
     *
     * @return trimmed resolution notes or null
     */
    public String getTrimmedResolutionNotes() {
        return resolutionNotes != null && !resolutionNotes.trim().isEmpty() 
            ? resolutionNotes.trim() 
            : null;
    }
}