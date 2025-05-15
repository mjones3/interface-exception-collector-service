package com.arcone.biopro.distribution.inventory.adapter.in.listener.created;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.vo.InputProduct;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;
import java.util.List;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ProductCreatedMessage.class, name = "ApheresisPlasmaProductCreated"),
    @JsonSubTypes.Type(value = ProductCreatedMessage.class, name = "ApheresisRBCProductCreated"),
    @JsonSubTypes.Type(value = ProductCreatedMessage.class, name = "ApheresisPlateletProductCreated"),
    @JsonSubTypes.Type(value = ProductCreatedMessage.class, name = "WholeBloodProductCreated")
})
public record ProductCreatedMessage(
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
    List<InputProduct> inputProduct) {
}
