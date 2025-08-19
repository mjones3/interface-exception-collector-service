package com.arcone.biopro.exception.collector.api.graphql.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

/**
 * Input DTO for acknowledging a single exception via GraphQL.
 * Contains validation rules and business constraints for acknowledgment
 * operations.
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
     */
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    /**
     * Optional estimated resolution time for the exception.
     * Helps with planning and tracking resolution progress.
     */
    private OffsetDateTime estimatedResolutionTime;

    /**
     * Optional user ID to assign the exception to for resolution.
     * Can be used for workload distribution and accountability.
     */
    @Size(max = 255, message = "Assigned user ID must not exceed 255 characters")
    private String assignedTo;
}