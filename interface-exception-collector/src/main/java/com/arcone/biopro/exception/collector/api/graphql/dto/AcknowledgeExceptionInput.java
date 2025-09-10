package com.arcone.biopro.exception.collector.api.graphql.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Simplified input DTO for acknowledging a single exception via GraphQL.
 * Focuses on essential fields only to reduce complexity and improve usability.
 * Aligns with REST API AcknowledgeRequest structure for consistency.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcknowledgeExceptionInput {

    /**
     * The transaction ID of the exception to acknowledge.
     * Must be a valid, non-empty string.
     */
    @NotBlank(message = "Transaction ID is required")
    @Size(max = 255, message = "Transaction ID must not exceed 255 characters")
    private String transactionId;

    /**
     * The reason for acknowledging the exception.
     * Must be a meaningful explanation of why the exception is being acknowledged.
     */
    @NotBlank(message = "Acknowledgment reason is required")
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;

    /**
     * Optional additional notes about the acknowledgment.
     * Can contain detailed information about the acknowledgment context.
     * Simplified from previous version - removed estimatedResolutionTime and assignedTo
     * to focus on core acknowledgment functionality.
     */
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;
}