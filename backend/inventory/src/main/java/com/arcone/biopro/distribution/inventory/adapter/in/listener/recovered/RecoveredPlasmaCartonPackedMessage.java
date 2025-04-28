package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

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
    List<PackedProductMessage> packedProducts
) {}
