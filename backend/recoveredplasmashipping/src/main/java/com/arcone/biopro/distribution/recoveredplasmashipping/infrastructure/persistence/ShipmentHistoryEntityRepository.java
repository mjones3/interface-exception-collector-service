package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ShipmentHistoryEntityRepository extends ReactiveCrudRepository<ShipmentHistoryEntity, Long> {

    Flux<ShipmentHistoryEntity> findAllByShipmentIdOrderByCreateDateDesc(Long shipmentId);
}
