package com.arcone.biopro.distribution.irradiation.application.irradiation.command;

import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchItemCompletionDTO;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CompleteBatchCommand(
    String deviceId,
    LocalDateTime endTime,
    List<BatchItemCompletionDTO> batchItems
) {
}