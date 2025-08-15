package com.arcone.biopro.exception.collector.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for retry operation initiation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryResponse {

    private Long retryId;
    private String status;
    private String message;
    private OffsetDateTime estimatedCompletionTime;
    private Integer attemptNumber;
}