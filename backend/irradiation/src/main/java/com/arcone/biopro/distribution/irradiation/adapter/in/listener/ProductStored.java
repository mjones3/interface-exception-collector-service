package com.arcone.biopro.distribution.irradiation.adapter.in.listener;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "ProductStored",
    title = "ProductStored",
    description = "Message for product stored process"
)
public record ProductStored(
    @Schema(
        title = "Unit Number",
        description = "Unit Number of the product",
        example = "W036825008001",
        requiredMode = REQUIRED
    )
    String unitNumber,

    @Schema(
        title = "Product Code",
        description = "Product Code",
        example = "E000001",
        requiredMode = REQUIRED
    )
    String productCode,

    @Schema(
        title = "Device Stored",
        description = "Device Stored",
        example = "Amicus",
        requiredMode = REQUIRED
    )
    String deviceStored,

    @Schema(
        title = "Device Used",
        description = "Device Used",
        requiredMode = REQUIRED
    )
    String deviceUse,

    @Schema(
        title = "Storage Location",
        description = "location when product was stored",
        example = "1FS",
        requiredMode = REQUIRED
    )
    String storageLocation,

    @Schema(
        title = "Location",
        description = "Location when product was created",
        example = "1FS",
        requiredMode = REQUIRED
    )
    String location,

    @Schema(
        title = "Location Type",
        description = "location type",
        example = "1FS",
        requiredMode = REQUIRED
    )
    String locationType,

    @Schema(
        title = "Storage Time",
        description = "Storage Time",
        example = "10:30",
        requiredMode = REQUIRED
    )
    ZonedDateTime storageTime,

    @Schema(
        name = "performedBy",
        title = "Performed By",
        description = "User that triggered the event",
        example = "trm-user",
        requiredMode = REQUIRED
    )
    String performedBy
) {}
