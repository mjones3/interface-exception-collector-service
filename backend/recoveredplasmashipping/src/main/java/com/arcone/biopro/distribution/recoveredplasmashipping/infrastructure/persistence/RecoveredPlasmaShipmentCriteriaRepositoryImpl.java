package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ProductType;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentCriteria;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.ProductTypeEntityMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaShipmentCriteriaEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class RecoveredPlasmaShipmentCriteriaRepositoryImpl implements RecoveredPlasmaShipmentCriteriaRepository {

    private final RecoveredPlasmaShipmentCriteriaEntityRepository recoveredPlasmaShipmentCriteriaEntityRepository;
    private final RecoveredPlasmaShipmentCriteriaEntityMapper recoveredPlasmaShipmentCriteriaEntityMapper;
    private final RecoveredPlasmaProductTypeEntityRepository recoveredPlasmaProductTypeEntityRepository;
    private final RecoveredPlasmaShipmentCriteriaItemEntityRepository recoveredPlasmaShipmentCriteriaItemEntityRepository;
    private final ProductTypeEntityMapper productTypeEntityMapper;

    @Override
    public Mono<RecoveredPlasmaShipmentCriteria> findProductCriteriaByCustomerCode(String productType, String customerCode) {
        return recoveredPlasmaShipmentCriteriaEntityRepository
            .findByCustomerCodeAndProductTypeAndActiveIsTrue(customerCode,productType)
            .flatMap(shipmentCriteria -> recoveredPlasmaShipmentCriteriaItemEntityRepository
                .findAllByCriteriaId(shipmentCriteria.getId())
                .collectList()
                .map(criteriaItemList -> {
                    return recoveredPlasmaShipmentCriteriaEntityMapper.toModel(shipmentCriteria,criteriaItemList);
                }));
    }

    @Override
    public Flux<ProductType> findAllProductTypeByByCustomerCode(String customerCode) {
        return recoveredPlasmaProductTypeEntityRepository.findAllByCostumer(customerCode)
            .map(productTypeEntityMapper::toModel);
    }

    @Override
    public Mono<ProductType> findProductTypeByProductCode(String productCode) {
        return recoveredPlasmaProductTypeEntityRepository
            .findByProductCode(productCode)
            .map(productTypeEntityMapper::toModel);
    }
}
