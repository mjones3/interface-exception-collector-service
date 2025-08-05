package com.arcone.biopro.distribution.receiving.infrastructure.dto;

import com.arcone.biopro.distribution.receiving.infrastructure.event.AbstractEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(
    name = "ShipmentCompleted",
    title = "ShipmentCompleted",
    description = "Shipment Completed Event"
)
@Builder
public class ShipmentCompletedMessage extends AbstractEvent<ShipmentCompletedPayload> {

}
