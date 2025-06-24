package com.arcone.biopro.distribution.eventbridge.domain.event;

import com.arcone.biopro.distribution.eventbridge.domain.model.OrderRejectedOutbound;

public record OrderRejectedOutboundEvent(OrderRejectedOutbound orderRejectedOutbound) {
}