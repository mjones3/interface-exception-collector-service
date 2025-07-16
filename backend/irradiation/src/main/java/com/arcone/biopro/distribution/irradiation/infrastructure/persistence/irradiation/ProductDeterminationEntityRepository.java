package com.arcone.biopro.distribution.irradiation.infrastructure.persistence.irradiation;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
interface ProductDeterminationEntityRepository extends ReactiveCrudRepository<ProductDeterminationEntity, Integer> {

    Mono<ProductDeterminationEntity> findBySourceProductCode(String sourceProductCode);

}
