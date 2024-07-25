package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import com.arcone.biopro.distribution.orderservice.domain.service.CustomerService;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
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


    public OrderCustomer(String code , String name) {
        this.code = code;
        this.name = name;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (this.code == null || this.code.isBlank()) {
            throw new IllegalArgumentException("code cannot be null or blank");
        }

        if (this.name == null || this.name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or blank");
        }
    }



}
