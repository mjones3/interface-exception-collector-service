package com.arcone.biopro.exception.collector.api.graphql.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Input DTO for bulk acknowledgment of multiple exceptions via GraphQL.
 * Contains validation rules for batch acknowledgment operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAcknowledgeInput {

    /**
     * List of transaction IDs for exceptions to acknowledge.
     * Must contain at least one valid transaction ID.
     */
    @NotEmpty(message = "Transaction IDs list cannot be empty")
    @Size(max = 100, message = "Cannot acknowledge more than 100 exceptions at once")
    private List<String> transactionIds;

    /**
     * The reason for acknowledging all the exceptions.
     * Must be a meaningful explanation applicable to all exceptions.
     */
    @NotBlank(message = "Acknowledgment reason is required")
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;

    /**
     * Optional additional notes about the bulk acknowledgment.
     * Applied to all exceptions in the batch.
     */
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    /**
     * Optional user ID to assign all exceptions to for resolution.
     * Can be used for workload distribution in bulk operations.
     */
    @Size(max = 255, message = "Assigned user ID must not exceed 255 characters")
    private String assignedTo;
}