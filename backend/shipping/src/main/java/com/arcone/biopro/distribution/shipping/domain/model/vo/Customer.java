package com.arcone.biopro.distribution.shipping.domain.model.vo;

import com.arcone.biopro.distribution.shipping.domain.model.Validatable;
import com.arcone.biopro.distribution.shipping.domain.service.CustomerService;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.error.DataNotFoundException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class Customer implements Validatable {

    private String code;
    private String name;
    private final CustomerService customerService;

    public Customer(String code, String name , CustomerService customerService) {
        this.code = code;
        this.name = name;
        this.customerService = customerService;
        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.code == null || this.code.isBlank()) {
            throw new IllegalArgumentException("Code cannot be null or blank");
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
            throw new IllegalArgumentException("Name cannot be null or blank");
        }

    }
}
