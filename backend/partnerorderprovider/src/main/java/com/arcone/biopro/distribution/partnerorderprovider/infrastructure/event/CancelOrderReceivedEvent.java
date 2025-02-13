package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event;

import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto.CancelOrderDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "CancelOrderReceived",
    title = "CancelOrderReceived",
    description = "Cancel Order Received Event"
)
@Getter
public class CancelOrderReceivedEvent extends AbstractEvent<CancelOrderDTO> {

    private final static String eventVersion = "1.0";
    private final static String eventType = "CancelOrderReceived";

    public CancelOrderReceivedEvent(CancelOrderDTO payload) {
        super( UUID.randomUUID(),Instant.now(),payload , eventVersion, eventType );
    }
}
