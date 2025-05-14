package com.arcone.biopro.distribution.inventory.adapter.in.listener.completed;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.common.Volume;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ProductCompletedMessage.class, name = "ApheresisPlasmaProductCompleted"),
    @JsonSubTypes.Type(value = ProductCompletedMessage.class, name = "ApheresisRBCProductCompleted"),
    @JsonSubTypes.Type(value = ProductCompletedMessage.class, name = "ApheresisPlateletProductCompleted"),
    @JsonSubTypes.Type(value = ProductCompletedMessage.class, name = "WholeBloodProductCompleted")
})
public record ProductCompletedMessage(
    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode,

    @Schema(description = "Volume of the product")
    Volume volume,

    @Schema(description = "Anticoagulant volume of the product")
    Volume anticoagulantVolume
    ,
    @Schema(description = "Abo RH Type of the product")
    AboRhType aboRh
) {
}
