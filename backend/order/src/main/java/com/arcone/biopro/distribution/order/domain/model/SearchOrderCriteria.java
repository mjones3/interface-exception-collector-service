package com.arcone.biopro.distribution.order.domain.model;

import com.arcone.biopro.distribution.order.domain.model.vo.LocationFilter;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import com.arcone.biopro.distribution.order.domain.repository.LocationRepository;
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
    private static final String SHIPMENT_TYPE_CODE = "ORDER_SHIPMENT_TYPE";

    private final LookupService lookupService;
    private final CustomerService customerService;
    private final LocationRepository locationRepository;

    private List<Lookup> orderStatus;
    private List<Lookup> orderPriorities;
    private List<OrderCustomerReport> customers;
    private List<Lookup> shipmentTypes;
    private List<LocationFilter> locations;

    public SearchOrderCriteria(LookupService lookupService, CustomerService customerService , LocationRepository locationRepository) {

        this.lookupService = lookupService;
        this.customerService = customerService;
        this.locationRepository = locationRepository;

        loadData();
        checkValid();
    }


    public void loadData() {
        loadOrderStatus();
        loadOrderPriorities();
        loadCustomers();
        loadShipmentTypes();
        loadLocations();
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

    public void loadLocations() {
        try{
            var locations = this.locationRepository.findAll().collectList().block();

            if(locations == null || locations.isEmpty()) {
                throw new IllegalArgumentException("Locations not found");
            }

            this.locations = locations.stream().map(location ->
                new LocationFilter(location.getCode(), location.getName())).toList();
        }catch (DataNotFoundException ex){
            throw new IllegalArgumentException("Locations not found");
        }
    }

    public void loadShipmentTypes() {
        try{
            var shipmentTypes = lookupService.findAllByType(SHIPMENT_TYPE_CODE).collectList().block();

            if(shipmentTypes == null || shipmentTypes.isEmpty()) {
                throw new IllegalArgumentException("Shipment Type not found");
            }

            this.shipmentTypes = shipmentTypes;
        }catch (DataNotFoundException ex){
            throw new IllegalArgumentException("Shipment Type not found");
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
        if (shipmentTypes == null || shipmentTypes.isEmpty()) {
            throw new IllegalArgumentException("Shipment Types are not valid");
        }
    }
}
