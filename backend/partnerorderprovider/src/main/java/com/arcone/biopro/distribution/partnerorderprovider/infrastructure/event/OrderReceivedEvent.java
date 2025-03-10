package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event;

import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto.OrderDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "OrderReceived",
    title = "OrderReceived",
    description = "Order Received Event"
)
@Getter
public class OrderReceivedEvent extends AbstractEvent<OrderDTO> implements Serializable {

    private final static String eventVersion = "1.0";
    private final static String eventType = "OrderReceived";

    public OrderReceivedEvent(OrderDTO payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventVersion, eventType );
    }

}
