package com.arcone.biopro.distribution.irradiation.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RecoveredPlasmaCartonUnpackedInput(
    String cartonNumber,
    int cartonSequence,
    String locationCode,
    String productType,
    String status,
    int totalProducts,
    List<PackedProductInput> unpackedProducts
) { }
