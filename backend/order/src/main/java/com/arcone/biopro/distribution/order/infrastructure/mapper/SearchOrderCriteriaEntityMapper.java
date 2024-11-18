package com.arcone.biopro.distribution.order.infrastructure.mapper;

import com.arcone.biopro.distribution.order.domain.model.Lookup;
import com.arcone.biopro.distribution.order.domain.model.SearchOrderCriteria;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchOrderCriteriaEntityMapper {

    public SearchOrderCriteria mapToDomain(List<Lookup> status, List<Lookup> deliveryTypes, List<OrderCustomerReport> customers) {
        return new SearchOrderCriteria(status, deliveryTypes, customers);

    }
}
