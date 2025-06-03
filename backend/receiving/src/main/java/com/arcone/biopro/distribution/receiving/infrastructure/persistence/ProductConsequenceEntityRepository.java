package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProductConsequenceEntityRepository  extends ReactiveCrudRepository<ProductConsequenceEntity, Long> {

    Flux<ProductConsequenceEntity> findAllByProductCategoryAndActiveIsTrueOrderByOrderNumberAsc(final String productCategory);
    Flux<ProductConsequenceEntity> findAllByProductCategoryAndResultPropertyAndActiveIsTrueOrderByOrderNumberAsc(final String productCategory , final String resultProperty);

}
