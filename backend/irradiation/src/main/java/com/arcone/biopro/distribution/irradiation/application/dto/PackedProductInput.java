package com.arcone.biopro.distribution.irradiation.application.dto;

import lombok.Builder;

@Builder
public record PackedProductInput(
        String unitNumber,
        String productCode,
        String status
    ) {}
