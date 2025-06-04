package com.arcone.biopro.distribution.receiving.domain.repository;

import com.arcone.biopro.distribution.receiving.domain.model.Location;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LocationRepository {

    Flux<Location> findAll();

    Mono<Location> findOneById(Long id);

    Mono<Location> findOneByCode(String code);
}
