package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import com.arcone.biopro.distribution.receiving.domain.model.ProductConsequence;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.ProductConsequenceEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ProductConsequenceRepositoryImpl implements ProductConsequenceRepository {
    private final ProductConsequenceEntityRepository productConsequenceEntityRepository;
    private final ProductConsequenceEntityMapper productConsequenceEntityMapper;


    @Override
    public Flux<ProductConsequence> findAllByProductCategoryAndResultProperty(String productCategory, String resultProperty) {
        return productConsequenceEntityRepository.findAllByProductCategoryAndResultPropertyAndActiveIsTrueOrderByOrderNumberAsc(productCategory,resultProperty)
            .map(productConsequenceEntityMapper::mapToDomain);
    }

    @Override
    public Flux<ProductConsequence> findAllByProductCategory(String productCategory) {
        return productConsequenceEntityRepository.findAllByProductCategoryAndActiveIsTrueOrderByOrderNumberAsc(productCategory)
            .map(productConsequenceEntityMapper::mapToDomain);
    }
}
