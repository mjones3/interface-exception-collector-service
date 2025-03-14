package com.arcone.biopro.distribution.order.infrastructure.event;

import com.arcone.biopro.distribution.order.infrastructure.dto.OrderFulfilledDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "OrderFulfilled",
    title = "OrderFulfilled",
    description = "Order Fulfilled Event"
)
public class OrderFulfilledEventDTO extends AbstractEvent<OrderFulfilledDTO> {

    private final static String eventVersion = "1.0";
    private final static String eventType = "OrderFulfilled";

    public OrderFulfilledEventDTO(OrderFulfilledDTO payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventType, eventVersion );
    }

}


