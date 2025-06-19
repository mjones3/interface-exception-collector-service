package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event;

import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonPackedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaShipmentCreatedOutputDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "RecoveredPlasmaCartonPacked",
    title = "RecoveredPlasmaCartonPacked",
    description = "Recovered Plasma Carton Packed Event"
)
@Getter
public class RecoveredPlasmaCartonPackedOutputEvent extends AbstractEvent<RecoveredPlasmaCartonPackedOutputDTO> {

    private final static String eventVersion = "1.0";
    private final static String eventType = "RecoveredPlasmaCartonPacked";


    public RecoveredPlasmaCartonPackedOutputEvent(RecoveredPlasmaCartonPackedOutputDTO payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventType, eventVersion );
    }

}
