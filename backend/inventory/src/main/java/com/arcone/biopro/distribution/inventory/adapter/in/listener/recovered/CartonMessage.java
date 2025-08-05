package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

import java.util.List;

public record CartonMessage(
        String cartonNumber,
        List<PackedProductMessage> packedProducts
    ) {}
