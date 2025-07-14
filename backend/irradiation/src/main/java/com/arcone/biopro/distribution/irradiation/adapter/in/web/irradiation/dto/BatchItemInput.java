package com.arcone.biopro.distribution.irradiation.adapter.in.web.irradiation.dto;

import lombok.Builder;

/**
 * GraphQL input DTO for batch item.
 */
@Builder
public record BatchItemInput(
    String unitNumber,
    String productCode,
    String lotNumber
) {
}