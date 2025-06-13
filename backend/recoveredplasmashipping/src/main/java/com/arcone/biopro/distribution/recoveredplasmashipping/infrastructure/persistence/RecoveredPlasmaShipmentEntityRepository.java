package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RecoveredPlasmaShipmentEntityRepository extends ReactiveCrudRepository<RecoveredPlasmaShipmentEntity , Long>  {

    @Query("select nextval('recoveredplasmashipping.bld_recovered_plasma_shipment_number_seq')")
    Mono<Long> getNextShippingId();

}
