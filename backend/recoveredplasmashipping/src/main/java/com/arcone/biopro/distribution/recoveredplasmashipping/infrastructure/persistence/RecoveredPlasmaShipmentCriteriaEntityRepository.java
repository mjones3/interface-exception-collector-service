package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RecoveredPlasmaShipmentCriteriaEntityRepository extends ReactiveCrudRepository<RecoveredPlasmaShipmentCriteriaEntity, Integer> {

    Mono<RecoveredPlasmaShipmentCriteriaEntity> findByCustomerCodeAndProductTypeAndActiveIsTrue(String customerCode, String productType);
}
