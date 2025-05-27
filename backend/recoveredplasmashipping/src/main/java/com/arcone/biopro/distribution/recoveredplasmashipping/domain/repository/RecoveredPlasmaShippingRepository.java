package com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import reactor.core.publisher.Mono;

public interface RecoveredPlasmaShippingRepository {

    Mono<Long> getNextShipmentId();
    Mono<RecoveredPlasmaShipment> create(RecoveredPlasmaShipment recoveredPlasmaShipment);
    Mono<RecoveredPlasmaShipment> findOneById(Long id);
    Mono<RecoveredPlasmaShipment> update(RecoveredPlasmaShipment recoveredPlasmaShipment);

}
