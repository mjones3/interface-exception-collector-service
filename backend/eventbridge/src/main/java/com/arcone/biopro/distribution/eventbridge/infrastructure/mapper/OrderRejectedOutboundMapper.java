package com.arcone.biopro.distribution.eventbridge.infrastructure.mapper;

import com.arcone.biopro.distribution.eventbridge.domain.model.OrderRejectedOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.OrderRejectedOutboundPayload;
import org.springframework.stereotype.Component;

@Component
public class OrderRejectedOutboundMapper {

    public OrderRejectedOutboundPayload toDto(OrderRejectedOutbound orderRejectedOutbound) {
        return OrderRejectedOutboundPayload.builder()
            .externalId(orderRejectedOutbound.externalId())
            .rejectedReason(orderRejectedOutbound.rejectedReason())
            .operation(orderRejectedOutbound.operation())
            .transactionId(orderRejectedOutbound.transactionId())
            .build();
    }
}