package com.arcone.biopro.distribution.irradiation.domain.irradiation.port;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.ProductDetermination;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.ProductCode;
import reactor.core.publisher.Mono;

/**
 * Repository interface for ProductDetermination domain entity.
 */
public interface ProductDeterminationRepository {

    Mono<ProductDetermination> findBySourceProductCode(ProductCode sourceProductCode);
    Mono<Boolean> existsBySourceProductCode(ProductCode sourceProductCode);

}
