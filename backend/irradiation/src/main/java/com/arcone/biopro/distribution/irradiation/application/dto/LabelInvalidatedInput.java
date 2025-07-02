package com.arcone.biopro.distribution.irradiation.application.dto;

import lombok.Builder;

@Builder
public record LabelInvalidatedInput(
    String unitNumber,
    String productCode) {
}
