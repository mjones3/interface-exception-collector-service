package com.arcone.biopro.distribution.order.domain.service;

import com.arcone.biopro.distribution.order.application.dto.ShipmentCompletedPayload;
import reactor.core.publisher.Mono;

public interface ShipmentCompletedService {

    Mono<Void> processCompletedShipmentEvent(ShipmentCompletedPayload shipmentCompletedEventPayloadDTO);
}
