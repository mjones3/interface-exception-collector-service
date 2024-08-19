package com.arcone.biopro.distribution.order.domain.repository;

import reactor.core.publisher.Mono;

public interface OrderConfigRepository {

    Mono<String> findProductFamilyByCategory(String productCategory, String productFamily);
    Mono<String> findBloodTypeByFamilyAndType(String productFamily, String bloodType);

}
