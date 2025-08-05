package com.arcone.biopro.distribution.eventbridge.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Schema(
    name = "RecoveredPlasmaShipmentClosedOutboundPayload",
    title = "RecoveredPlasmaShipmentClosedOutboundPayload",
    description = "Recovered Plasma Shipment Closed Outbound Event Payload"
)
@Builder
public record RecoveredPlasmaShipmentClosedOutboundPayload(
        String shipmentNumber,
        String locationShipmentCode,
        String locationCartonCode,
        String customerCode,
        String bloodCenterName,
        String shipmentDate,
        String shipmentCloseDate,
        String shipmentLocationCode,
        Integer totalShipmentProducts,
        List<RecoveredPlasmaShipmentClosedCartonOutboundPayload> cartonList
) implements Serializable {

}
