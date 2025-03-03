package com.arcone.biopro.distribution.shipping.domain.repository;

import com.arcone.biopro.distribution.shipping.domain.model.ProductLocationHistory;
import com.arcone.biopro.distribution.shipping.domain.model.vo.Product;
import reactor.core.publisher.Mono;

public interface ProductLocationHistoryRepository {
    Mono<ProductLocationHistory> findCurrentLocation(Product product);
}
