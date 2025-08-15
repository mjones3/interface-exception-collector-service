package com.arcone.biopro.exception.collector.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for exception acknowledgment.
 * Returned from PUT /api/v1/exceptions/{transactionId}/acknowledge endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after acknowledging an exception")
public class AcknowledgeResponse {

    @Schema(description = "Current status of the exception", example = "ACKNOWLEDGED")
    private String status;

    @Schema(description = "Timestamp when the exception was acknowledged")
    private OffsetDateTime acknowledgedAt;

    @Schema(description = "User or system that acknowledged the exception", example = "john.doe@company.com")
    private String acknowledgedBy;

    @Schema(description = "Notes provided during acknowledgment", example = "Reviewed and assigned to development team")
    private String notes;

    @Schema(description = "Transaction ID of the acknowledged exception", example = "txn-12345")
    private String transactionId;
}