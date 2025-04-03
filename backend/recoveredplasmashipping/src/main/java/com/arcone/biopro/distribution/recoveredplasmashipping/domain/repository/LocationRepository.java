package com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LocationRepository {

    Flux<Location> findAll();

    Mono<Location> findOneById(Long id);

    Mono<Location> findOneByCode(String code);
}
