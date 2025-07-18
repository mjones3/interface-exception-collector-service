package com.arcone.biopro.distribution.irradiation.infrastructure.persistence.irradiation;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.ProductDetermination;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.ProductDeterminationRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.ProductCode;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
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

    @Override
    public Mono<Boolean> existsBySourceProductCode(ProductCode sourceProductCode) {
        return entityRepository.existsBySourceProductCode(sourceProductCode.value());
    }
}
