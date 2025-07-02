package com.arcone.biopro.distribution.irradiation.adapter.in.listener.recovered;

import java.util.List;

public record CartonMessage(
        String cartonNumber,
        List<PackedProductMessage> packedProducts
    ) {}
