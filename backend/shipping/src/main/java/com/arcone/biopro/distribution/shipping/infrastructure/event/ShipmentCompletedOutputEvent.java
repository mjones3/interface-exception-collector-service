package com.arcone.biopro.distribution.shipping.infrastructure.event;

import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShipmentCompletedPayload;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "ShipmentCompleted",
    title = "ShipmentCompleted",
    description = "Shipment Completed Event"
)
@Getter
public class ShipmentCompletedOutputEvent extends AbstractEvent<ShipmentCompletedPayload>{


    private final static String eventVersion = "1.0";
    private final static String eventType = "ShipmentCompleted";

    public ShipmentCompletedOutputEvent(ShipmentCompletedPayload payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventVersion, eventType );
    }

}
