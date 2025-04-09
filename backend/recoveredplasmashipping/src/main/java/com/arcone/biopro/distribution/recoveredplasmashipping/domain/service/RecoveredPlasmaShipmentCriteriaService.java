package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ProductTypeOutput;
import reactor.core.publisher.Flux;

public interface RecoveredPlasmaShipmentCriteriaService {

    Flux<ProductTypeOutput> findAllProductTypeByCustomer(String customerCode);
}
