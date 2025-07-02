package com.arcone.biopro.distribution.irradiation.adapter.in.listener.completed.apheresis;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.common.Volume;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.AboRhType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "ApheresisPlateletProductCompleted",
    title = "ApheresisPlateletProductCompleted",
    description = "Message for completed ApheresisPlateletProductCompleted process"
)
public record ApheresisPlateletProductCompleted(
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
