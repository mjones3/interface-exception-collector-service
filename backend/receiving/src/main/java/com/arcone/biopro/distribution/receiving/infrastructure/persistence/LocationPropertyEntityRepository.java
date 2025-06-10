package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface LocationPropertyEntityRepository extends ReactiveCrudRepository<LocationPropertyEntity, Long> {

    Flux<LocationPropertyEntity> findByLocationId(Long locationId);
}
