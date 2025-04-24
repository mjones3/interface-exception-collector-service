package com.arcone.biopro.distribution.inventory.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RecoveredPlasmaCartonPackedInput(
    String cartonNumber,
    int cartonSequence,
    String locationCode,
    String productType,
    String status,
    int totalProducts,
    double totalWeight,
    double totalVolume,
    List<PackedProduct> packedProducts
) {
    @Builder
    public record PackedProduct(
        String unitNumber,
        String productCode,
        String status
    ) {}
}
