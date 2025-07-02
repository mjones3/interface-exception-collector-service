package com.arcone.biopro.distribution.irradiation.adapter.in.listener.label;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

@Schema(
    name = "LabelApplied",
    title = "LabelApplied",
    description = "Message for label application process"
)
public record LabelApplied(
    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode,

    @Schema(description = "Product description")
    String productDescription,

    @Schema(description = "Expiration date")
    String expirationDate,

    @Schema(description = "Indicates if the product is licensed")
    Boolean isLicensed,

    @Schema(description = "Weight of the product")
    Integer weight,

    @Schema(description = "Collection date and time")
    ZonedDateTime collectionDate,

    @Schema(description = "Location")
    String location,

    @Schema(description = "Product family")
    String productFamily,

    @Schema(description = "Blood type ABO/Rh")
    String aboRh
) {
}
