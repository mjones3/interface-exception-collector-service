package com.arcone.biopro.distribution.eventbridge.application.mapper;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderCreatedEventDTO;
import com.arcone.biopro.distribution.eventbridge.domain.model.OrderCreatedOutbound;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderCreatedMapper {
    OrderCreatedOutbound toDomain(OrderCreatedEventDTO event);
}