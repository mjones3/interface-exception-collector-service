package com.arcone.biopro.distribution.orderservice.infrastructure.mapper;

import com.arcone.biopro.distribution.orderservice.domain.model.OrderItem;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.OrderItemOrderId;
import com.arcone.biopro.distribution.orderservice.infrastructure.persistence.OrderItemEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class OrderItemEntityMapper {

    public OrderItemEntity mapToEntity(final OrderItem orderItem) {
        return OrderItemEntity.builder()
            .id(orderItem.getId())
            .orderId(ofNullable(orderItem.getOrderId()).map(OrderItemOrderId::getOrderId).orElse(null))
            .productFamily(orderItem.getProductFamily().getProductFamily())
            .bloodType(orderItem.getBloodType().getBloodType())
            .quantity(orderItem.getQuantity())
            .comments(orderItem.getComments())
            .createDate(orderItem.getCreateDate())
            .modificationDate(orderItem.getModificationDate())
            .build();
    }

    public OrderItem mapToDomain(final OrderItemEntity orderItemEntity) {
        return new OrderItem(
            orderItemEntity.getId(),
            orderItemEntity.getOrderId(),
            orderItemEntity.getProductFamily(),
            orderItemEntity.getBloodType(),
            orderItemEntity.getQuantity(),
            orderItemEntity.getComments(),
            orderItemEntity.getCreateDate(),
            orderItemEntity.getModificationDate()
        );
    }

}
