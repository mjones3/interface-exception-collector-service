package com.arcone.biopro.distribution.orderservice.application.mapper;

import com.arcone.biopro.distribution.orderservice.adapter.in.web.dto.OrderItemDTO;
import com.arcone.biopro.distribution.orderservice.domain.model.OrderItem;
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
            .build();
    }

    public OrderItem mapToDomain(final OrderItemDTO orderItemDTO) {
        return new OrderItem(
            orderItemDTO.id(),
            orderItemDTO.orderId(),
            orderItemDTO.productFamily(),
            orderItemDTO.bloodType(),
            orderItemDTO.quantity(),
            orderItemDTO.comments(),
            orderItemDTO.createDate(),
            orderItemDTO.modificationDate()
        );
    }

}
