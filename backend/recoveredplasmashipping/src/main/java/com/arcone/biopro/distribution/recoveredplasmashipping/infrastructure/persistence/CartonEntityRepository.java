package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CartonEntityRepository extends ReactiveCrudRepository<CartonEntity, Long> {

    @Query("select nextval('recoveredplasmashipping.bld_recovered_plasma_shipment_carton_number_seq')")
    Mono<Long> getNextCartonId();

    Mono<Integer> countByShipmentIdAndDeleteDateIsNull(Long shipmentId);

    Flux<CartonEntity> findAllByShipmentIdAndDeleteDateIsNullOrderByCartonSequenceNumberAsc(Long shipmentId);

}
