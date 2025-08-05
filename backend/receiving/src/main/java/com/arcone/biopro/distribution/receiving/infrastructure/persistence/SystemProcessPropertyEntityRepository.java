package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SystemProcessPropertyEntityRepository extends ReactiveCrudRepository<SystemProcessPropertyEntity, Long> {
    Flux<SystemProcessPropertyEntity> findAllBySystemProcessType(String processType);
    Mono<SystemProcessPropertyEntity> findFirstBySystemProcessTypeAndPropertyKey(String processType,String propertyKey);
}
