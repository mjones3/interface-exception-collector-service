package com.arcone.biopro.distribution.irradiation.infrastructure.persistence.irradiation;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.ProductDetermination;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.ProductDeterminationRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.ProductCode;
import reactor.core.publisher.Mono;

class ProductDeterminationRepositoryImpl implements ProductDeterminationRepository {

    private final ProductDeterminationEntityRepository entityRepository;
    private final ProductDeterminationEntityMapper mapper;

    public ProductDeterminationRepositoryImpl(ProductDeterminationEntityRepository entityRepository,
                                            ProductDeterminationEntityMapper mapper) {
        this.entityRepository = entityRepository;
        this.mapper = mapper;
    }

    @Override
    public Mono<ProductDetermination> findBySourceProductCode(ProductCode sourceProductCode) {
        return entityRepository.findBySourceProductCode(sourceProductCode.value())
            .map(mapper::toDomain);
    }
}
