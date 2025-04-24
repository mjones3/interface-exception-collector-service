package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

import java.time.ZonedDateTime;
import java.util.List;

public record RecoveredPlasmaCartonPackedMessage(
    String cartonNumber,
    int cartonSequence,
    String locationCode,
    String productType,
    String closeEmployeeId,
    String closeDate,
    String status,
    int totalProducts,
    double totalWeight,
    double totalVolume,
    List<PackedProduct> packedProducts
) {
    public record PackedProduct(
        String unitNumber,
        String productCode,
        String packedByEmployeeId,
        String status
    ) {}
}