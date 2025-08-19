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
 * Input type for bulk retry exceptions GraphQL mutation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkRetryInput {

    @NotEmpty(message = "Transaction IDs list cannot be empty")
    @Size(max = 100, message = "Cannot retry more than 100 exceptions at once")
    private List<String> transactionIds;

    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    private RetryExceptionInput.RetryPriority priority;
}