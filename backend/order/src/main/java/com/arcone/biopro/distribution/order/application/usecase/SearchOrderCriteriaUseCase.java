package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.domain.model.SearchOrderCriteria;
import com.arcone.biopro.distribution.order.domain.repository.SearchOrderCriteriaAggregate;
import com.arcone.biopro.distribution.order.domain.service.SearchOrderCriteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SearchOrderCriteriaUseCase implements SearchOrderCriteriaService {

    private final SearchOrderCriteriaAggregate searchOrderCriteriaAggregate;


    @Override
    public Mono<SearchOrderCriteria> searchOrderCriteria() {
        return searchOrderCriteriaAggregate.searchOrderCriteria();
    }

}
