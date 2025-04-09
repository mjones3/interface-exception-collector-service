package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CartonItemEntityRepository extends ReactiveCrudRepository<CartonItemEntity,Long> {

    Flux<CartonItemEntity> findAllByCartonIdOrderByCreateDateAsc(Long cartonId);
    Mono<Integer> countByUnitNumberAndProductCode(String unitNumber, String productCode);

}
