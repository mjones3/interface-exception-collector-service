package com.arcone.biopro.distribution.irradiation.domain.service;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.ProductDetermination;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.ProductCode;
import reactor.core.publisher.Mono;

/**
 * Domain service for product determination logic.
 */
public interface ProductDeterminationService {

    Mono<ProductCode> determineTargetProduct(ProductCode sourceProductCode);
    Mono<ProductDetermination> findProductDetermination(ProductCode sourceProductCode);
}
