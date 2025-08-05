package com.arcone.biopro.distribution.eventbridge.infrastructure.mapper;

import com.arcone.biopro.distribution.eventbridge.domain.model.OrderCreatedOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.OrderCreatedOutboundPayload;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderCreatedOutboundMapper {
    OrderCreatedOutboundPayload toDto(OrderCreatedOutbound domain);
}