package com.arcone.biopro.distribution.irradiation.domain.service;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.ProductDetermination;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.ProductDeterminationRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.ProductCode;
import reactor.core.publisher.Mono;

/**
 * Domain service for product determination logic.
 */
public interface ProductDeterminationService {
    
    Mono<ProductCode> determineTargetProduct(ProductCode sourceProductCode);
}

class ProductDeterminationServiceImpl implements ProductDeterminationService {
    
    private final ProductDeterminationRepository repository;
    
    public ProductDeterminationServiceImpl(ProductDeterminationRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public Mono<ProductCode> determineTargetProduct(ProductCode sourceProductCode) {
        return repository.findBySourceProductCode(sourceProductCode)
            .filter(ProductDetermination::isActive)
            .map(ProductDetermination::getTargetProductCode)
            .switchIfEmpty(Mono.just(sourceProductCode));
    }
}