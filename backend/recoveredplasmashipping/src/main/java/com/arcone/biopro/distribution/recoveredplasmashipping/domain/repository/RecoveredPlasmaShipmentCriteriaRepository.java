package com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ProductType;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentCriteria;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaProductTypeEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecoveredPlasmaShipmentCriteriaRepository {
    Mono<RecoveredPlasmaShipmentCriteria> findProductCriteriaByCustomerCode(String productType, String customerCode);
    Flux<ProductType> findAllProductTypeByByCustomerCode(String customerCode);
}
