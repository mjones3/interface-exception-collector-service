package com.arcone.biopro.distribution.eventbridge.infrastructure.mapper;

import com.arcone.biopro.distribution.eventbridge.domain.model.OrderCancelledOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.OrderCancelledOutboundPayload;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderCancelledOutboundMapper {
    OrderCancelledOutboundPayload toDto(OrderCancelledOutbound domain);
}