package com.arcone.biopro.distribution.eventbridge.application.mapper;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderRejectedPayload;
import com.arcone.biopro.distribution.eventbridge.domain.model.OrderRejectedOutbound;
import org.springframework.stereotype.Component;

@Component
public class OrderRejectedMapper {

    public OrderRejectedOutbound toDomain(OrderRejectedPayload payload) {
        return OrderRejectedOutbound.builder()
            .externalId(payload.externalId())
            .rejectedReason(payload.rejectedReason())
            .operation(payload.operation())
            .transactionId(payload.transactionId())
            .build();
    }
}