package com.arcone.biopro.distribution.order.domain.model;

import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class SearchOrderCriteria implements Validatable {

    private List<Lookup> orderStatus;
    private List<Lookup> orderPriorities;
    private List<OrderCustomerReport> customers;

    public SearchOrderCriteria(List<Lookup> orderStatus, List<Lookup> orderPriorities, List<OrderCustomerReport> customers) {
        this.orderStatus = orderStatus;
        this.orderPriorities = orderPriorities;
        this.customers = customers;

        checkValid();
    }


    @Override
    public void checkValid() {
        if (orderStatus == null || orderStatus.isEmpty()) {
            throw new IllegalArgumentException("orderStatus are not valid");
        }
        if (orderPriorities == null || orderPriorities.isEmpty()) {
            throw new IllegalArgumentException("orderPriorities are not valid");
        }
        if (customers == null || customers.isEmpty()) {
            throw new IllegalArgumentException("customers are not valid");
        }
    }
}
