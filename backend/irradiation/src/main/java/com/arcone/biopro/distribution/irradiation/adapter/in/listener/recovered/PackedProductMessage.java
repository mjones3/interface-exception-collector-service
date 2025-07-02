package com.arcone.biopro.distribution.irradiation.adapter.in.listener.recovered;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "PackedProductMessage",
    title = "PackedProductMessage",
    description = "Packed Product Message"
)
public record PackedProductMessage(
        String unitNumber,
        String productCode,
        String status
    ) {}
