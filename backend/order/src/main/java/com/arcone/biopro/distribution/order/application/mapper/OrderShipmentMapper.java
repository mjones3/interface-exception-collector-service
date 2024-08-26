package com.arcone.biopro.distribution.order.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderShipmentDTO;
import com.arcone.biopro.distribution.order.application.dto.ShipmentCreatedEvenPayloadDTO;
import com.arcone.biopro.distribution.order.domain.model.OrderShipment;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class OrderShipmentMapper {

    public OrderShipment mapToDomain(ShipmentCreatedEvenPayloadDTO shipmentCreatedEvenPayloadDTO) {
        return new OrderShipment(null, shipmentCreatedEvenPayloadDTO.orderNumber()
            , shipmentCreatedEvenPayloadDTO.shipmentId(), shipmentCreatedEvenPayloadDTO.shipmentStatus(), ZonedDateTime.now());

    }

    public OrderShipmentDTO mapToDto(OrderShipment orderShipment) {
        return OrderShipmentDTO
            .builder()
            .id(orderShipment.getId())
            .orderId(orderShipment.getOrderId())
            .shipmentId(orderShipment.getShipmentId())
            .shipmentStatus(orderShipment.getShipmentStatus())
            .createDate(orderShipment.getCreateDate())
            .build();
    }
}
