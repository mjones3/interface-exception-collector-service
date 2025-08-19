package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.api.graphql.validation.ValidRetryOperation;
import com.arcone.biopro.exception.collector.api.graphql.validation.ValidTransactionId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Input type for retry exception GraphQL mutation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidRetryOperation(maxReasonLength = 500)
public class RetryExceptionInput {

    @NotBlank(message = "Transaction ID is required")
    @ValidTransactionId(message = "Invalid transaction ID format")
    private String transactionId;

    @NotBlank(message = "Reason is required")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    private String reason;

    @NotNull(message = "Priority is required")
    private RetryPriority priority;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    public enum RetryPriority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}