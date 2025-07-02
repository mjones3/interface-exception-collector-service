package com.arcone.biopro.distribution.irradiation.adapter.in.listener.unsuitable;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "ProductUnsuitable",
    title = "ProductUnsuitable",
    description = "Product Unsuitable Event"
)
public record ProductUnsuitable(
    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode,

    @Schema(description = "Key identifying the reason for unsuitability")
    String reasonKey
) {
}

