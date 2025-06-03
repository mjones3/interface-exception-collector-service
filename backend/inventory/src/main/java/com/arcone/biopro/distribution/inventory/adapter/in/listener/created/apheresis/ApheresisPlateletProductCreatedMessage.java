package com.arcone.biopro.distribution.inventory.adapter.in.listener.created.apheresis;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.created.ValueUnit;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.vo.InputProduct;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;
import java.util.List;

@Schema(
    name = "ApheresisPlateletProductCreated",
    title = "ApheresisPlateletProductCreated",
    description = "Apheresis Platelet Product Created Event"
)
public record ApheresisPlateletProductCreatedMessage(
    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode,

    @Schema(description = "Product description")
    String productDescription,

    @Schema(description = "Expiration date")
    String expirationDate,

    @Schema(description = "Expiration time")
    String expirationTime,

    @Schema(description = "Weight of the product")
    ValueUnit weight,

    @Schema(description = "Draw time of the product")
    ZonedDateTime drawTime,

    @Schema(description = "Manufacturing location")
    String manufacturingLocation,

    @Schema(description = "Collection location")
    String collectionLocation,

    @Schema(description = "Collection time zone")
    String collectionTimeZone,

    @Schema(description = "Product family")
    String productFamily,

    @Schema(description = "Blood type and RH factor")
    AboRhType aboRh,

    @Schema(description = "List of input products used")
    List<InputProduct> inputProducts) {
}
