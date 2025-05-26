package com.arcone.biopro.distribution.receiving.domain.repository;

import com.arcone.biopro.distribution.receiving.domain.model.ProductConsequence;
import reactor.core.publisher.Flux;

public interface ProductConsequenceRepository {

    Flux<ProductConsequence> findAllByProductCategoryAndResultProperty(final String productCategory, final String resultProperty);
}
