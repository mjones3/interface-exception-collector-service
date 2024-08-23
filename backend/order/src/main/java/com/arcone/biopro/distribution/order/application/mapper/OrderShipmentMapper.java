package com.arcone.biopro.distribution.order.application.mapper;

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
}
