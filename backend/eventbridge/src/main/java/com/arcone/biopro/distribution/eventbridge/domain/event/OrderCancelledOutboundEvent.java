package com.arcone.biopro.distribution.eventbridge.domain.event;

import com.arcone.biopro.distribution.eventbridge.domain.model.OrderCancelledOutbound;

public class OrderCancelledOutboundEvent extends OrderOutboundEvent<OrderCancelledOutbound> {

    private final static String eventType = "OrderCancelledOutbound";

    public OrderCancelledOutboundEvent(OrderCancelledOutbound payload) {
        super(payload);
    }

    @Override
    public String getEventType() {
        return this.eventType;
    }
}
