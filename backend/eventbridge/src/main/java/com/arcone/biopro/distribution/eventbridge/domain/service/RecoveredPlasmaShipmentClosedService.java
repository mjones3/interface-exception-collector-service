package com.arcone.biopro.distribution.eventbridge.domain.service;

import com.arcone.biopro.distribution.eventbridge.application.dto.RecoveredPlasmaShipmentClosedPayload;
import reactor.core.publisher.Mono;

public interface RecoveredPlasmaShipmentClosedService {
    Mono<Void> processClosedShipmentEvent(RecoveredPlasmaShipmentClosedPayload recoveredPlasmaShipmentClosedPayload);
}
