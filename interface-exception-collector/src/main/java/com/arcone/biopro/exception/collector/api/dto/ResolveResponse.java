package com.arcone.biopro.exception.collector.api.dto;

import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for exception resolution.
 * Returned from PUT /api/v1/exceptions/{transactionId}/resolve endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after resolving an exception")
public class ResolveResponse {

    @Schema(description = "Current status of the exception", example = "RESOLVED")
    private String status;

    @Schema(description = "Timestamp when the exception was resolved")
    private OffsetDateTime resolvedAt;

    @Schema(description = "User or system that resolved the exception", example = "jane.smith@company.com")
    private String resolvedBy;

    @Schema(description = "Method used to resolve the exception")
    private ResolutionMethod resolutionMethod;

    @Schema(description = "Notes about how the exception was resolved", example = "Fixed data validation issue in source system")
    private String resolutionNotes;

    @Schema(description = "Transaction ID of the resolved exception", example = "txn-12345")
    private String transactionId;

    @Schema(description = "Total number of retry attempts made", example = "2")
    private Integer totalRetryAttempts;
}