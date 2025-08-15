package com.arcone.biopro.exception.collector.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for initiating a retry operation on an exception.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryRequest {

    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    @Size(max = 50, message = "Priority must not exceed 50 characters")
    @Builder.Default
    private String priority = "NORMAL";

    @Builder.Default
    private Boolean notifyOnCompletion = false;

    @Size(max = 255, message = "Initiated by must not exceed 255 characters")
    private String initiatedBy;
}