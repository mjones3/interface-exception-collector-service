package com.arcone.biopro.distribution.eventbridge.infrastructure.mapper;

import com.arcone.biopro.distribution.eventbridge.domain.model.OrderModifiedOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.OrderModifiedOutboundPayload;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderModifiedOutboundMapper {
    OrderModifiedOutboundPayload toDto(OrderModifiedOutbound domain);
}