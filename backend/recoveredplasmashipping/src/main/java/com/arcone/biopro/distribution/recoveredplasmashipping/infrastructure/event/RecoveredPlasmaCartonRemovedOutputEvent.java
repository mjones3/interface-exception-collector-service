package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event;

import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonRemovedOutputDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "RecoveredPlasmaCartonRemoved",
    title = "RecoveredPlasmaCartonRemoved",
    description = "Recovered Plasma Carton Removed Event"
)
@Getter
public class RecoveredPlasmaCartonRemovedOutputEvent extends AbstractEvent<RecoveredPlasmaCartonRemovedOutputDTO> {

    private final static String eventVersion = "1.0";
    private final static String eventType = "RecoveredPlasmaCartonRemoved";


    public RecoveredPlasmaCartonRemovedOutputEvent(RecoveredPlasmaCartonRemovedOutputDTO payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventType, eventVersion );
    }

}
