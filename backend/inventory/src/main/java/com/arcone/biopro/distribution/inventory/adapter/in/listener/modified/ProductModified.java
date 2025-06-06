package com.arcone.biopro.distribution.inventory.adapter.in.listener.modified;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.common.Volume;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.created.ValueUnit;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

@Schema(
    name = "ProductModified",
    title = "ProductModified",
    description = "Message for modified product process"
)
public record ProductModified(
    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode,

    @Schema(description = "Product description")
    String productDescription,

    @Schema(description = "Parent product code")
    String parentProductCode,

    @Schema(description = "Product family")
    String productFamily,

    @Schema(description = "Expiration date")
    String expirationDate,

    @Schema(description = "Expiration time")
    String expirationTime,

    @Schema(description = "Location where modification was performed")
    String modificationLocation,

    @Schema(description = "Date and time of modification")
    ZonedDateTime modificationDate,

    @Schema(description = "Volume of the product")
    Volume volume,

    @Schema(description = "Weight of the product")
    ValueUnit weight
) {
}
