package com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShipmentHistory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecoveredPlasmaShippingRepository {

    Mono<Long> getNextShipmentId();
    Mono<RecoveredPlasmaShipment> create(RecoveredPlasmaShipment recoveredPlasmaShipment);
    Mono<RecoveredPlasmaShipment> findOneById(Long id);
    Mono<RecoveredPlasmaShipment> update(RecoveredPlasmaShipment recoveredPlasmaShipment);
    Mono<ShipmentHistory> createShipmentHistory(ShipmentHistory shipmentHistory);
    Flux<ShipmentHistory> findAllByShipmentId(Long shipmentId);

}
