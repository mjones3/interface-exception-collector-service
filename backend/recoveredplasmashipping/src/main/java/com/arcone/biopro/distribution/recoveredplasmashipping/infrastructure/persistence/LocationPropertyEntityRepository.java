package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface LocationPropertyEntityRepository extends ReactiveCrudRepository<LocationPropertyEntity, Long> {

    Flux<LocationPropertyEntity> findByLocationId(Long locationId);

    @Query("select * from lk_location_property where location_id in ( select id from lk_location where code = :locationCode )")
    Flux<LocationPropertyEntity> findAllByLocationCode(@Param("locationCode") String locationCode);
}
