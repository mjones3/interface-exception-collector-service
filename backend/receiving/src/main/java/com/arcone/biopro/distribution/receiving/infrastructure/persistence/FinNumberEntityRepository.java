package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface FinNumberEntityRepository extends ReactiveCrudRepository<FinNumberEntity, Integer> {

    Mono<FinNumberEntity> findFirstByFinNumberAndActiveIsTrue(String finNumber);
}
