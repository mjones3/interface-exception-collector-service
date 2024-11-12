package com.arcone.biopro.distribution.order.domain.repository;

import com.arcone.biopro.distribution.order.domain.model.SearchOrderCriteria;
import reactor.core.publisher.Mono;

public interface SearchOrderCriteriaAggregate {
    Mono<SearchOrderCriteria> searchOrderCriteria();
}
