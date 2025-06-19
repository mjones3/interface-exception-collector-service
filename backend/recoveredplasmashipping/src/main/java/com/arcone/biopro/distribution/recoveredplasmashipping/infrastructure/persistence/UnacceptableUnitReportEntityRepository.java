package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UnacceptableUnitReportEntityRepository extends ReactiveCrudRepository<UnacceptableUnitReportEntity, Long> {
    Flux<UnacceptableUnitReportEntity> findAllByShipmentId(final Long shipmentId);
    Mono<Void> deleteAllByShipmentId(final Long shipmentId);
}
