package com.arcone.biopro.distribution.order.infrastructure.mapper;

import com.arcone.biopro.distribution.order.domain.model.OrderShipment;
import com.arcone.biopro.distribution.order.infrastructure.persistence.OrderShipmentEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderShipmentEntityMapper {

    public OrderShipmentEntity mapToEntity(OrderShipment orderShipment) {
        return OrderShipmentEntity
            .builder()
            .orderId(orderShipment.getOrderId())
            .shipmentId(orderShipment.getShipmentId())
            .shipmentStatus(orderShipment.getShipmentStatus())
            .build();
    }

    public OrderShipment mapToDomain(OrderShipmentEntity orderShipmentEntity) {
        return new OrderShipment(orderShipmentEntity.getId(), orderShipmentEntity.getOrderId()
            , orderShipmentEntity.getShipmentId(), orderShipmentEntity.getShipmentStatus()
            ,orderShipmentEntity.getCreateDate());
    }
}
