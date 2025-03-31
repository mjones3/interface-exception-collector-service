package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RecoveredPlasmaProductTypeEntityRepository extends ReactiveCrudRepository<RecoveredPlasmaProductTypeEntity, Integer> {

    // Find by product type (utilizing the unique constraint)
    Mono<RecoveredPlasmaProductTypeEntity> findByProductType(String productType);

}
