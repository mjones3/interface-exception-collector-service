package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface RecoveredPlasmaShipmentCriteriaItemEntityRepository extends ReactiveCrudRepository<RecoveredPlasmaShipmentCriteriaItemEntity, Integer>{

    Flux<RecoveredPlasmaShipmentCriteriaItemEntity> findAllByCriteriaId(Integer criteriaId);
}
