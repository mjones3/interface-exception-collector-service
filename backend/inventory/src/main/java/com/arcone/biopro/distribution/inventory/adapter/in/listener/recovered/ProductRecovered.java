package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

@Schema(
    name = "ProductRecovered",
    title = "ProductRecovered",
    description = "Message for recovered product process"
)
public record ProductRecovered(
    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode,

    @Schema(description = "User who performed the recovery")
    String performedBy,

    @Schema(description = "Date and time when recovery was created")
    ZonedDateTime createDate
) {
}
