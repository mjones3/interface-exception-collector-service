package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentCriteria;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaShipmentCriteriaEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class RecoveredPlasmaShipmentCriteriaRepositoryImpl implements RecoveredPlasmaShipmentCriteriaRepository {

    private final RecoveredPlasmaShipmentCriteriaEntityRepository recoveredPlasmaShipmentCriteriaEntityRepository;
    private final RecoveredPlasmaShipmentCriteriaEntityMapper recoveredPlasmaShipmentCriteriaEntityMapper;

    @Override
    public Mono<RecoveredPlasmaShipmentCriteria> findProductCriteriaByCustomerCode(String productType, String customerCode) {
        return recoveredPlasmaShipmentCriteriaEntityRepository
            .findByCustomerCodeAndProductTypeAndActiveIsTrue(customerCode,productType)
            .map(recoveredPlasmaShipmentCriteriaEntityMapper::toModel);
    }
}
