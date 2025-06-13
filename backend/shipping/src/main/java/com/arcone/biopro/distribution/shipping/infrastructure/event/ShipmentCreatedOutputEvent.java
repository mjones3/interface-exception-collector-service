package com.arcone.biopro.distribution.shipping.infrastructure.event;

import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShipmentCreatedPayloadDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "ShipmentCreated",
    title = "ShipmentCreated",
    description = "Shipment Created Event"
)
@Getter
public class ShipmentCreatedOutputEvent extends AbstractEvent<ShipmentCreatedPayloadDTO> {

    private final static String eventVersion = "1.0";
    private final static String eventType = "ShipmentCreated";

    public ShipmentCreatedOutputEvent(ShipmentCreatedPayloadDTO payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventVersion, eventType );
    }

}
