package com.arcone.biopro.exception.collector.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for retry attempt information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryAttemptResponse {

    private Long id;
    private Integer attemptNumber;
    private String status;
    private String initiatedBy;
    private OffsetDateTime initiatedAt;
    private OffsetDateTime completedAt;
    private Boolean resultSuccess;
    private String resultMessage;
    private Integer resultResponseCode;
    private String resultErrorDetails;
}