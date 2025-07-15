package com.arcone.biopro.distribution.irradiation.adapter.in.web.irradiation.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GraphQL input DTO for batch submission.
 */
@Builder
public record SubmitBatchInput(
    String deviceId,
    LocalDateTime startTime,
    List<BatchItemInput> batchItems
) {
}