package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event;

import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto.ModifyOrderDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "ModifyOrderReceived",
    title = "ModifyOrderReceived",
    description = "Modify Order Received Event"
)
@Getter
public class ModifyOrderReceivedEvent extends AbstractEvent<ModifyOrderDTO> {

    private final static String eventVersion = "1.0";
    private final static String eventType = "ModifyOrderReceived";

    public ModifyOrderReceivedEvent(ModifyOrderDTO payload) {
        super( UUID.randomUUID(),Instant.now(),payload , eventVersion, eventType );
    }
}
