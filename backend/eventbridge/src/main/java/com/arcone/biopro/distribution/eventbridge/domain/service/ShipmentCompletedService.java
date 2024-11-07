package com.arcone.biopro.distribution.eventbridge.domain.service;

import com.arcone.biopro.springwebsocketsrabbitmqpoc.application.dto.ShipmentCompletedPayload;
import reactor.core.publisher.Mono;

public interface ShipmentCompletedService {

    Mono<Void> processCompletedShipmentEvent(ShipmentCompletedPayload shipmentCompletedEventPayloadDTO);
}
