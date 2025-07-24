package com.arcone.biopro.distribution.irradiation.adapter.in.web.irradiation.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CompleteBatchInput(
    String batchId,
    LocalDateTime endTime,
    List<BatchItemCompletionInput> batchItems
) {
}
