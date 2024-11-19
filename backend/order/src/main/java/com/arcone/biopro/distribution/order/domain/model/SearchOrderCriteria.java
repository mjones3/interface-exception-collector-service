package com.arcone.biopro.distribution.order.domain.model;

import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.infrastructure.controller.error.DataNotFoundException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class SearchOrderCriteria implements Validatable {

    private static final String ORDER_STATUS_TYPE_CODE = "ORDER_STATUS";
    private static final String ORDER_PRIORITY_TYPE_CODE = "ORDER_PRIORITY";

    private final LookupService lookupService;
    private final CustomerService customerService;

    private List<Lookup> orderStatus;
    private List<Lookup> orderPriorities;
    private List<OrderCustomerReport> customers;

    public SearchOrderCriteria(LookupService lookupService, CustomerService customerService) {

        this.lookupService = lookupService;
        this.customerService = customerService;

        loadData();
        checkValid();
    }


    public void loadData() {
        loadOrderStatus();
        loadOrderPriorities();
        loadCustomers();
    }

    public void loadOrderPriorities() {
        try{
            var orderPriorities = lookupService.findAllByType(ORDER_PRIORITY_TYPE_CODE).collectList().block();

            if(orderPriorities == null || orderPriorities.isEmpty()) {
                throw new IllegalArgumentException("Order Priorities not found");
            }

            this.orderPriorities = orderPriorities;
        }catch (DataNotFoundException ex){
            throw new IllegalArgumentException("Order Priorities not found");
        }
    }


    public void loadOrderStatus() {
        try{
            var orderStatus = lookupService.findAllByType(ORDER_STATUS_TYPE_CODE).collectList().block();

            if(orderStatus == null || orderStatus.isEmpty()) {
                throw new IllegalArgumentException("Order Status not found");
            }

            this.orderStatus = orderStatus;
        }catch (DataNotFoundException ex){
            throw new IllegalArgumentException("Order Status not found");
        }
    }

    public void loadCustomers() {
        try{
            var customers = customerService.getCustomers().collectList().block();

            if(customers == null || customers.isEmpty()) {
                throw new IllegalArgumentException("Customers not found");
            }

           this.customers = customers.stream().map(customer ->
               new OrderCustomerReport(customer.code(), customer.name())).toList();
        }catch (DataNotFoundException ex){
            throw new IllegalArgumentException("Customers not found");
        }
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
