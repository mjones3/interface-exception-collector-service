package com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentCriteria;
import reactor.core.publisher.Mono;

public interface RecoveredPlasmaShipmentCriteriaRepository {
    Mono<RecoveredPlasmaShipmentCriteria> findProductCriteriaByCustomerCode(String productType, String customerCode);
}
