package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.domain.model.SearchOrderCriteria;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.domain.service.SearchOrderCriteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class SearchOrderCriteriaUseCase implements SearchOrderCriteriaService {

    private final LookupService lookupService;
    private final CustomerService customerService;


    @Override
    public Mono<SearchOrderCriteria> searchOrderCriteria() {
        return Mono.fromCallable(() -> new SearchOrderCriteria(lookupService, customerService)).publishOn(Schedulers.boundedElastic());
    }

}
