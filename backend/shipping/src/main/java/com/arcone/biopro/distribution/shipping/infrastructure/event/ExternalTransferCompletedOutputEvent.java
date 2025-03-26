package com.arcone.biopro.distribution.shipping.infrastructure.event;

import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ExternalTransferCompletedOutputPayload;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "ExternalTransferCompleted",
    title = "ExternalTransferCompleted",
    description = "External Transfer Completed Event"
)
@Getter
public class ExternalTransferCompletedOutputEvent extends AbstractEvent<ExternalTransferCompletedOutputPayload> {


    private final static String eventVersion = "1.0";
    private final static String eventType = "ExternalTransferCompleted";

    public ExternalTransferCompletedOutputEvent(ExternalTransferCompletedOutputPayload payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventVersion, eventType );
    }


}
