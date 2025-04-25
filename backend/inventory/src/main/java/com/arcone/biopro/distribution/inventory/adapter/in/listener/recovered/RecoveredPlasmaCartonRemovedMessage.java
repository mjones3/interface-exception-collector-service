package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

import java.util.List;

public record RecoveredPlasmaCartonRemovedMessage(
    String cartonNumber,
    int cartonSequence,
    String locationCode,
    String productType,
    String removeEmployeeId,
    String removeDate,
    String status,
    int totalProducts,
    double totalWeight,
    double totalVolume,
    List<PackedProductMessage> packedProducts
) {}
