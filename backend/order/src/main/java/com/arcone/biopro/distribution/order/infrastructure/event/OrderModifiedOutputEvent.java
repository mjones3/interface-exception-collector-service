package com.arcone.biopro.distribution.order.infrastructure.event;

import com.arcone.biopro.distribution.order.infrastructure.dto.OrderModifiedDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "OrderModified",
    title = "OrderModified",
    description = "Order Modified Event"
)
public class OrderModifiedOutputEvent extends AbstractEvent<OrderModifiedDTO>{

    private final static String eventVersion = "1.0";
    private final static String eventType = "OrderModified";


    public OrderModifiedOutputEvent(OrderModifiedDTO payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventType, eventVersion );
    }
}
