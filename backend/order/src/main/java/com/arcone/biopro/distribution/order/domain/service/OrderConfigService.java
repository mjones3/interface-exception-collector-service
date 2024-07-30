package com.arcone.biopro.distribution.order.domain.service;

import reactor.core.publisher.Mono;

public interface OrderConfigService {

    Mono<String> findProductFamilyByCategory(String productCategory, String productFamily);
    Mono<String> findBloodTypeByFamilyAndType(String productFamily, String bloodType);
}
