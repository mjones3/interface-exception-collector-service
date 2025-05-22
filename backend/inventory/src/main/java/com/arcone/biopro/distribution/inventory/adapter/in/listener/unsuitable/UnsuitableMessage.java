package com.arcone.biopro.distribution.inventory.adapter.in.listener.unsuitable;

import io.swagger.v3.oas.annotations.media.Schema;


public record UnsuitableMessage(
    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode,

    @Schema(description = "Key identifying the reason for unsuitability")
    String reasonKey
) {
}

