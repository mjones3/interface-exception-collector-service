package com.arcone.biopro.distribution.irradiation.domain.service;


import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.ProductDetermination;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.ProductDeterminationRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.ProductCode;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class ProductDeterminationServiceImpl implements ProductDeterminationService {

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

    @Override
    public Mono<ProductDetermination> findProductDetermination(ProductCode sourceProductCode) {
        return repository.findBySourceProductCode(sourceProductCode)
            .filter(ProductDetermination::isActive)
            .doOnNext(determination -> log.debug("Found product determination for source: {}", sourceProductCode.value()))
            .switchIfEmpty(Mono.fromRunnable(() ->
                log.error("No product determination found for source product code: {}", sourceProductCode.value())
            ).then(Mono.empty()));
    }
}
