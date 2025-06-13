package com.arcone.biopro.distribution.inventory.adapter.in.listener.storage;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "ProductStored",
    title = "ProductStored",
    description = "Message for stored product"
)
public record ProductStoredMessage(
    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode,

    @Schema(description = "Device where product is stored")
    String deviceStored,

    @Schema(description = "Device usage type")
    String deviceUse,

    @Schema(description = "Storage location identifier")
    String storageLocation,

    @Schema(description = "Location identifier")
    String location,

    @Schema(description = "Type of location")
    String locationType,

    @Schema(description = "Time when product was stored")
    String storageTime,

    @Schema(description = "User who performed the storage")
    String performedBy
) {
}
