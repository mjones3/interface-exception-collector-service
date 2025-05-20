package com.arcone.biopro.distribution.inventory.adapter.in.listener.labelinvalidated;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "LabelInvalidatedEvent",
    title = "LabelInvalidatedEvent",
    description = "Message for label invalidation process"
)
public record LabelInvalidatedMessage(
    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode
) {
}
