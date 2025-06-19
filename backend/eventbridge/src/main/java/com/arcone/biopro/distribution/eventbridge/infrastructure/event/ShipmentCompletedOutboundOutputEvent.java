package com.arcone.biopro.distribution.eventbridge.infrastructure.event;

import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.ShipmentCompletedOutboundPayload;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "ShipmentCompletedOutbound",
    title = "ShipmentCompletedOutbound",
    description = "Shipment Completed Outbound Event"
)
@Getter
public class ShipmentCompletedOutboundOutputEvent extends AbstractEvent<ShipmentCompletedOutboundPayload>{

    private final static String eventVersion = "1.0";
    private final static String eventType = "ShipmentCompletedOutbound";

    public ShipmentCompletedOutboundOutputEvent(ShipmentCompletedOutboundPayload payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventType, eventVersion );
    }
}
