package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import com.arcone.biopro.distribution.orderservice.domain.service.CustomerService;
import com.arcone.biopro.distribution.orderservice.infrastructure.controller.error.DataNotFoundException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class OrderCustomer implements Validatable {

    private String code;
    private String name;
    private final CustomerService customerService;

    public OrderCustomer(String code, CustomerService customerService) {
        this.code = code;
        this.customerService = customerService;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (this.code == null || this.code.isBlank()) {
            throw new IllegalArgumentException("code cannot be null or blank");
        }

        try{
            var customer = customerService.getCustomerByCode(code).block();
            if(customer == null) {
                throw new IllegalArgumentException("Customer not found for code: " + this.code);
            }
            this.name = customer.name();
        }catch (DataNotFoundException ex){
            log.error("Could not find customer with code {}", code, ex);
            throw new IllegalArgumentException("Customer not found for code: " + this.code);
        }

        if (this.name == null || this.name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or blank");
        }
    }

}
