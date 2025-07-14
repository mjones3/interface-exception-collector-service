package com.arcone.biopro.distribution.irradiation.application.dto;

import lombok.Builder;

@Builder
public record IrradiationInventoryOutput(
    String unitNumber,
    String productCode,
    String location,
    String status,
    String productDescription,
    String productFamily,
    String shortDescription,
    boolean isLabeled,
    String statusReason,
    String unsuitableReason,
    Boolean expired) {
}

