package com.arcone.biopro.distribution.order.domain.service;

import com.arcone.biopro.distribution.order.application.dto.ShipmentCreatedEvenPayloadDTO;
import com.arcone.biopro.distribution.order.domain.model.OrderShipment;
import reactor.core.publisher.Mono;

public interface OrderShipmentService {

    Mono<OrderShipment> processShipmentCreatedEvent(ShipmentCreatedEvenPayloadDTO eventPayload);

    Mono<OrderShipment> findOneByOrderId(Long orderId);
}
