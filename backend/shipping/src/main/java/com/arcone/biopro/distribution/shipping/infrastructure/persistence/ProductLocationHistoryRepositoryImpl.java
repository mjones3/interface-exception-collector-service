package com.arcone.biopro.distribution.shipping.infrastructure.persistence;

import com.arcone.biopro.distribution.shipping.domain.model.ProductLocationHistory;
import com.arcone.biopro.distribution.shipping.domain.model.vo.Product;
import com.arcone.biopro.distribution.shipping.domain.repository.ProductLocationHistoryRepository;
import com.arcone.biopro.distribution.shipping.infrastructure.mapper.ProductLocationHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ProductLocationHistoryRepositoryImpl implements ProductLocationHistoryRepository {

    private final ProductLocationHistoryEntityRepository productLocationHistoryEntityRepository;
    private final ProductLocationHistoryMapper productLocationHistoryMapper;

    @Override
    public Mono<ProductLocationHistory> findCurrentLocation(Product product) {
        return productLocationHistoryEntityRepository.findLastHistoryByProduct(product.getUnitNumber(), product.getProductCode())
            .map(productLocationHistoryMapper::toDomain);
    }
}
