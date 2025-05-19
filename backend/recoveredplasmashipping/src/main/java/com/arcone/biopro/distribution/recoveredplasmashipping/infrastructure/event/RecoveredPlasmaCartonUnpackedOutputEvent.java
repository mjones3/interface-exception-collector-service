package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event;

import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonUnpackedOutputDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "RecoveredPlasmaCartonUnpacked",
    title = "RecoveredPlasmaCartonUnpacked",
    description = "Recovered Plasma Carton Unpacked Event"
)
@Getter
public class RecoveredPlasmaCartonUnpackedOutputEvent extends AbstractEvent<RecoveredPlasmaCartonUnpackedOutputDTO> {

    private final static String eventVersion = "1.0";
    private final static String eventType = "RecoveredPlasmaCartonUnpacked";


    public RecoveredPlasmaCartonUnpackedOutputEvent(RecoveredPlasmaCartonUnpackedOutputDTO payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventType, eventVersion );
    }

}
