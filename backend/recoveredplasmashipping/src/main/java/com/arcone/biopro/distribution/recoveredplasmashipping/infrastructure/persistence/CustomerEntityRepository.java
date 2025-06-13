package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerEntityRepository extends ReactiveCrudRepository<CustomerEntity, Long> {

    Flux<CustomerEntity> findAllByActiveIsTrueOrderByNameAsc();
    Mono<CustomerEntity> findByCode(String code);

}
