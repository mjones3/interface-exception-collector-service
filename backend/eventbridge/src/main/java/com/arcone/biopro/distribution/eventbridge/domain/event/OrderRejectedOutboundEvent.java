package com.arcone.biopro.distribution.eventbridge.domain.event;

import com.arcone.biopro.distribution.eventbridge.domain.model.OrderRejectedOutbound;

public class OrderRejectedOutboundEvent extends OrderOutboundEvent<OrderRejectedOutbound> {

    private final static String eventType = "OrderRejectedOutbound";

    public OrderRejectedOutboundEvent(OrderRejectedOutbound payload) {
        super(payload);
    }

    @Override
    public String getEventType() {
        return this.eventType;
    }
}
