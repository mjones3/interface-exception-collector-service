package com.arcone.biopro.distribution.irradiation.adapter.in.listener.created.wholeblood;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.created.ValueUnit;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.InputProduct;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;
import java.util.List;

@Schema(
    name = "WholeBloodProductCreated",
    title = "WholeBloodProductCreated",
    description = "Whole Blood Product Created Event"
)
public record WholeBloodProductCreated(
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
