package com.arcone.biopro.distribution.eventbridge.domain.event;

import com.arcone.biopro.distribution.eventbridge.domain.model.OrderModifiedOutbound;

public class OrderModifiedOutboundEvent extends OrderOutboundEvent<OrderModifiedOutbound> {

    private final static String eventType = "OrderModifiedOutbound";

    public OrderModifiedOutboundEvent(OrderModifiedOutbound payload) {
        super(payload);
    }

    @Override
    public String getEventType() {
        return this.eventType;
    }
}
