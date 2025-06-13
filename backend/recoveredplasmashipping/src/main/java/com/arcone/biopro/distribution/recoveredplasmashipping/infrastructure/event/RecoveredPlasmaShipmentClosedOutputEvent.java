package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event;

import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaShipmentClosedOutputDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "RecoveredPlasmaShipmentClosed",
    title = "RecoveredPlasmaShipmentClosed",
    description = "Recovered Plasma Shipment Closed Event"
)
@Getter
public class RecoveredPlasmaShipmentClosedOutputEvent extends AbstractEvent<RecoveredPlasmaShipmentClosedOutputDTO> {

    private final static String eventVersion = "1.0";
    private final static String eventType = "RecoveredPlasmaShipmentClosed";


    public RecoveredPlasmaShipmentClosedOutputEvent(RecoveredPlasmaShipmentClosedOutputDTO payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventType, eventVersion );
    }

}
