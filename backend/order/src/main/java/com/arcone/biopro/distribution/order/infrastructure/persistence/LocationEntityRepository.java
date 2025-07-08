package com.arcone.biopro.distribution.order.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LocationEntityRepository extends ReactiveCrudRepository<LocationEntity, Long> {

    Flux<LocationEntity> findAllByActiveIsTrueOrderByNameAsc();
    Mono<LocationEntity> findById(Long id);
    Mono<LocationEntity> findByCode(String code);
}
