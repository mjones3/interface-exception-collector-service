package com.arcone.biopro.exception.collector.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for acknowledging an exception.
 * Used in PUT /api/v1/exceptions/{transactionId}/acknowledge endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to acknowledge an exception")
public class AcknowledgeRequest {

    @NotBlank(message = "Acknowledged by is required")
    @Size(max = 255, message = "Acknowledged by must not exceed 255 characters")
    @Schema(description = "User or system that acknowledged the exception", example = "john.doe@company.com")
    private String acknowledgedBy;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Optional notes about the acknowledgment", example = "Reviewed and assigned to development team")
    private String notes;
}