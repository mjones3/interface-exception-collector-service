package com.arcone.biopro.exception.collector.api.dto;

import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for resolving an exception.
 * Used in PUT /api/v1/exceptions/{transactionId}/resolve endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to resolve an exception")
public class ResolveRequest {

    @NotBlank(message = "Resolved by is required")
    @Size(max = 255, message = "Resolved by must not exceed 255 characters")
    @Schema(description = "User or system that resolved the exception", example = "jane.smith@company.com")
    private String resolvedBy;

    @NotNull(message = "Resolution method is required")
    @Schema(description = "Method used to resolve the exception")
    private ResolutionMethod resolutionMethod;

    @Size(max = 1000, message = "Resolution notes must not exceed 1000 characters")
    @Schema(description = "Notes about how the exception was resolved", example = "Fixed data validation issue in source system")
    private String resolutionNotes;
}