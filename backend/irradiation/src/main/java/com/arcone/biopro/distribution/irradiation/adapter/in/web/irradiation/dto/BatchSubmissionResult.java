package com.arcone.biopro.distribution.irradiation.adapter.in.web.irradiation.dto;

import lombok.Builder;

/**
 * GraphQL result DTO for batch submission response.
 */
@Builder
public record BatchSubmissionResult(
    Long batchId,
    String message,
    boolean success
) {
}