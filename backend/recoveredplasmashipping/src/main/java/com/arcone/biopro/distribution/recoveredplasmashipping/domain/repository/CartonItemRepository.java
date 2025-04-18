package com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import reactor.core.publisher.Mono;

public interface CartonItemRepository {

    Mono<Integer> countByProduct(String unitNumber , String productCode);
    Mono<CartonItem> save(CartonItem cartonItem);
}
