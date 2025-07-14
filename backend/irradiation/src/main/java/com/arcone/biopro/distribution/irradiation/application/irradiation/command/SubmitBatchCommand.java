package com.arcone.biopro.distribution.irradiation.application.irradiation.command;

import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchItemDTO;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Command for submitting a batch for irradiation process.
 */
@Builder
public record SubmitBatchCommand(
    String deviceId,
    LocalDateTime startTime,
    List<BatchItemDTO> batchItems
) {
}
