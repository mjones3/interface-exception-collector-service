package com.arcone.biopro.distribution.order.infrastructure.event;

import com.arcone.biopro.distribution.order.infrastructure.dto.OrderCancelledDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "OrderCancelled",
    title = "OrderCancelled",
    description = "Order Cancelled Event"
)
public class OrderCancelledOutputEvent extends AbstractEvent<OrderCancelledDTO>{

    private final static String eventVersion = "1.0";
    private final static String eventType = "OrderCancelled";


    public OrderCancelledOutputEvent(OrderCancelledDTO payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventType, eventVersion );
    }
}
