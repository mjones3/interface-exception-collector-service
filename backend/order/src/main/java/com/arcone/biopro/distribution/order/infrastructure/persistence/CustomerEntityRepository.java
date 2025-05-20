package com.arcone.biopro.distribution.order.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerEntityRepository extends ReactiveCrudRepository<CustomerEntity, Long> {

    Flux<CustomerEntity> findAllByActiveIsTrueOrderByNameAsc();
    Mono<CustomerEntity> findByCode(String code);

}
