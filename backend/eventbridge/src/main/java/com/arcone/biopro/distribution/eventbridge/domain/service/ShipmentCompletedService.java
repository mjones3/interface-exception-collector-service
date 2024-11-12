package com.arcone.biopro.distribution.eventbridge.domain.service;

import com.arcone.biopro.distribution.eventbridge.application.dto.ShipmentCompletedPayload;
import reactor.core.publisher.Mono;

public interface ShipmentCompletedService {

    Mono<Void> processCompletedShipmentEvent(ShipmentCompletedPayload shipmentCompletedEventPayloadDTO);
}
