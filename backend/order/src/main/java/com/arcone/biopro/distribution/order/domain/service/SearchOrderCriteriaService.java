package com.arcone.biopro.distribution.order.domain.service;

import com.arcone.biopro.distribution.order.domain.model.SearchOrderCriteria;
import reactor.core.publisher.Mono;

public interface SearchOrderCriteriaService {
    Mono<SearchOrderCriteria> searchOrderCriteria();
}
