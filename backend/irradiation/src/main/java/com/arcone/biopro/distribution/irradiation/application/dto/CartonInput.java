package com.arcone.biopro.distribution.irradiation.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CartonInput(
    String cartonNumber,
    List<ShipmentPackedProductInput> packedProducts
) {}
