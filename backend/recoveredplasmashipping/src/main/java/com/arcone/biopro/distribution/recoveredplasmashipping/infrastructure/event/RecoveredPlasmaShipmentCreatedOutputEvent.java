package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event;

import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaShipmentCreatedOutputDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "RecoveredPlasmaShipmentCreated",
    title = "RecoveredPlasmaShipmentCreated",
    description = "Recovered Plasma Shipment Created Event"
)
@Getter
public class RecoveredPlasmaShipmentCreatedOutputEvent extends AbstractEvent<RecoveredPlasmaShipmentCreatedOutputDTO> {

    private final static String eventVersion = "1.0";
    private final static String eventType = "RecoveredPlasmaShipmentCreated";


    public RecoveredPlasmaShipmentCreatedOutputEvent(RecoveredPlasmaShipmentCreatedOutputDTO payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventType, eventVersion );
    }

}
