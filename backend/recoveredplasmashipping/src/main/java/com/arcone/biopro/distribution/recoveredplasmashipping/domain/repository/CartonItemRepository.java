package com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import reactor.core.publisher.Mono;

public interface CartonItemRepository {

    Mono<Integer> countByProduct(String unitNumber , String productCode);
    Mono<CartonItem> save(CartonItem cartonItem);
    Mono<CartonItem> findByCartonAndProduct(Long cartonId, String unitNumber , String productCode);
    Mono<Void> deleteAllByCartonId(Long cartonId);
}
