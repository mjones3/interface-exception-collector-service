package com.arcone.biopro.distribution.order.infrastructure.event;

import com.arcone.biopro.distribution.order.infrastructure.dto.OrderRejectedDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "OrderRejected",
    title = "OrderRejected",
    description = "Order Rejected Event"
)
public class OrderRejectedOutputEvent extends AbstractEvent<OrderRejectedDTO> {

    private final static String eventVersion = "1.0";
    private final static String eventType = "OrderRejected";


    public OrderRejectedOutputEvent(OrderRejectedDTO payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventType, eventVersion );
    }
}
