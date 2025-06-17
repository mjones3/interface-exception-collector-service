package com.arcone.biopro.distribution.inventory.adapter.in.listener.labelinvalidated;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "LabelInvalidated",
    title = "LabelInvalidated",
    description = "Message for label invalidation process"
)
public record LabelInvalidated(
    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode
) {
}
