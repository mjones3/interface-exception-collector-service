package com.arcone.biopro.distribution.inventory.adapter.in.listener.completed.apheresis;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.common.Volume;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "ApheresisRBCProductCompleted",
    title = "ApheresisRBCProductCompleted",
    description = "Message for completed ApheresisRBCProductCompleted process"
)
public record ApheresisRBCProductCompleted(
    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode,

    @Schema(description = "Volume of the product")
    Volume volume,

    @Schema(description = "Anticoagulant volume of the product")
    Volume anticoagulantVolume,

    @Schema(description = "Abo RH Type of the product")
    AboRhType aboRh
){}
