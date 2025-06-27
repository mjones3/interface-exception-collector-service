package com.arcone.biopro.distribution.eventbridge.domain.event;

import com.arcone.biopro.distribution.eventbridge.domain.model.OrderCreatedOutbound;

public class OrderCreatedOutboundEvent extends OrderOutboundEvent<OrderCreatedOutbound> {

    private final static String eventType = "OrderCreatedOutbound";

    public OrderCreatedOutboundEvent(OrderCreatedOutbound payload) {
        super(payload);
    }

    @Override
    public String getEventType() {
        return this.eventType;
    }
}
