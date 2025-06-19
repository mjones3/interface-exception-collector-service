package com.arcone.biopro.distribution.order.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderItemDTO;
import com.arcone.biopro.distribution.order.domain.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemMapper {

    public OrderItemDTO mapToDTO(final OrderItem orderItem) {
        return OrderItemDTO.builder()
            .id(orderItem.getId())
            .orderId(orderItem.getOrderId().getOrderId())
            .productFamily(orderItem.getProductFamily().getProductFamily())
            .bloodType(orderItem.getBloodType().getBloodType())
            .quantity(orderItem.getQuantity())
            .comments(orderItem.getComments())
            .createDate(orderItem.getCreateDate())
            .modificationDate(orderItem.getModificationDate())
            .quantityAvailable(orderItem.getQuantityAvailable())
            .quantityShipped(orderItem.getQuantityShipped())
            .quantityRemaining(orderItem.getQuantityRemaining())
            .build();
    }

}
