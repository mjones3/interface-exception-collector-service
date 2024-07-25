package com.arcone.biopro.distribution.orderservice.application.mapper;

import com.arcone.biopro.distribution.orderservice.application.dto.OrderItemEventDTO;
import com.arcone.biopro.distribution.orderservice.domain.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemReceivedEventMapper {

    public OrderItem mapToDomain(final OrderItemEventDTO orderItemEventDTO) {
        return new OrderItem(
            null,
            null,
            orderItemEventDTO.productFamily(),
            orderItemEventDTO.bloodType(),
            orderItemEventDTO.quantity(),
            orderItemEventDTO.comments(),
            null,
            null
        );
    }

}
