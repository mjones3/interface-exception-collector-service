package com.arcone.biopro.distribution.eventbridge.infrastructure.event;

import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.RecoveredPlasmaShipmentClosedOutboundPayload;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "RecoveredPlasmaShipmentClosedOutbound",
    title = "RecoveredPlasmaShipmentClosedOutbound",
    description = "Recovered Plasma Shipment Closed Outbound Event"
)
@Getter
public class RecoveredPlasmaShipmentClosedOutboundEvent extends AbstractEvent<RecoveredPlasmaShipmentClosedOutboundPayload> {

    private final static String eventVersion = "1.0";
    private final static String eventType = "RecoveredPlasmaShipmentClosedOutbound";


    public RecoveredPlasmaShipmentClosedOutboundEvent(RecoveredPlasmaShipmentClosedOutboundPayload payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventType, eventVersion );
    }

}
