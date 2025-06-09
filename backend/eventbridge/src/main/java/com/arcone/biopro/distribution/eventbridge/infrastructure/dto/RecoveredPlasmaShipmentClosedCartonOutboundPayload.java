package com.arcone.biopro.distribution.eventbridge.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Schema(
    name = "RecoveredPlasmaShipmentClosedCartonOutboundPayload",
    title = "RecoveredPlasmaShipmentClosedCartonOutboundPayload"
)
@Builder
public record RecoveredPlasmaShipmentClosedCartonOutboundPayload(
        String cartonNumber,
        List<RecoveredPlasmaShipmentClosedCartonItemOutboundPayload> packedProducts

) implements Serializable {

}
