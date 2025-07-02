package com.arcone.biopro.distribution.irradiation.adapter.in.listener.recovered;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
    name = "RecoveredPlasmaShipmentClosed",
    title = "RecoveredPlasmaShipmentClosed",
    description = "Message for closed recovered plasma shipment"
)
public record RecoveredPlasmaShipmentClosed(
    @Schema(description = "Shipment number identifier")
    String shipmentNumber,

    @Schema(description = "List of cartons in the shipment")
    List<CartonMessage> cartonList
) {
}

