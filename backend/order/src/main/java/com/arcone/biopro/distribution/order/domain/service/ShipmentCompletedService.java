package com.arcone.biopro.distribution.order.domain.service;

import com.arcone.biopro.distribution.order.application.dto.ShipmentCompletedEventPayloadDTO;
import reactor.core.publisher.Mono;

public interface ShipmentCompletedService {

    Mono<Void> processCompletedShipmentEvent(ShipmentCompletedEventPayloadDTO shipmentCompletedEventPayloadDTO);
}
