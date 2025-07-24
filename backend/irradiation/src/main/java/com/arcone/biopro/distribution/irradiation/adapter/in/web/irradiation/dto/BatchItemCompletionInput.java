package com.arcone.biopro.distribution.irradiation.adapter.in.web.irradiation.dto;

import lombok.Builder;

@Builder
public record BatchItemCompletionInput(
    String unitNumber,
    String productCode,
    boolean isIrradiated
) {
}
