package com.arcone.biopro.distribution.order.infrastructure.persistence;

import com.arcone.biopro.distribution.order.domain.model.SearchOrderCriteria;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import com.arcone.biopro.distribution.order.domain.repository.SearchOrderCriteriaAggregate;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.infrastructure.mapper.SearchOrderCriteriaEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

@Repository
@RequiredArgsConstructor
public class SearchOrderCriteriaAggregateImpl implements SearchOrderCriteriaAggregate {

    private static final String ORDER_STATUS_TYPE_CODE = "ORDER_STATUS";
    private static final String ORDER_PRIORITY_TYPE_CODE = "ORDER_PRIORITY";

    private final SearchOrderCriteriaEntityMapper searchCriteriaValuesEntityMapper;
    private final LookupService lookupService;
    private final CustomerService customerService;


    @Override
    public Mono<SearchOrderCriteria> searchOrderCriteria() {
        var orderStatus = lookupService.findAllByType(ORDER_STATUS_TYPE_CODE);
        var orderPriorities = lookupService.findAllByType(ORDER_PRIORITY_TYPE_CODE);
        var orderCustomers = customerService.getCustomers().map(customer -> new OrderCustomerReport(customer.code(), customer.name()));

        return Flux.zip(orderStatus, orderPriorities, orderCustomers).collectList().map(list -> {
            var status = list.stream().map(Tuple2::getT1).toList();
            var priorities = list.stream().map(Tuple2::getT2).toList();
            var customers = list.stream().map(Tuple3::getT3).toList();
            return searchCriteriaValuesEntityMapper.mapToDomain(status, priorities, customers);
        });
    }
}
