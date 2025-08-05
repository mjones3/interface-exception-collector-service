package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ShipmentHistoryOutput;
import reactor.core.publisher.Flux;

public interface ShipmentHistoryService {

    Flux<ShipmentHistoryOutput> findAllByShipmentId(Long shipmentId);
}
