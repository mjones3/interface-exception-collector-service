package com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository;

import reactor.core.publisher.Mono;

public interface CartonItemRepository {

    Mono<Integer> countByProduct(String unitNumber , String productCode);
}
