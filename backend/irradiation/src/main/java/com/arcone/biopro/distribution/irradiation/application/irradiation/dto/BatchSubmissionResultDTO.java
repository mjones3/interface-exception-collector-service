package com.arcone.biopro.distribution.irradiation.application.irradiation.dto;

import lombok.Builder;

/**
 * DTO representing the result of batch submission for irradiation.
 */
@Builder
public record BatchSubmissionResultDTO(
    Long batchId,
    String message,
    boolean success
) {
}