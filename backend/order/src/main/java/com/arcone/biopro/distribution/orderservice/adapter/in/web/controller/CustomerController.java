package com.arcone.biopro.distribution.orderservice.adapter.in.web.controller;

import com.arcone.biopro.distribution.orderservice.domain.service.CustomerService;
import com.arcone.biopro.distribution.orderservice.infrastructure.controller.error.ServiceNotAvailableException;
import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @QueryMapping
    public Flux<CustomerDTO> findAllCustomers() {
        return customerService.getCustomers();
    }

    @QueryMapping
    public Mono<CustomerDTO> findCustomerByCode(@Argument("code") String code) {
        return customerService.getCustomerByCode(code);
    }

    @ExceptionHandler
    public Mono<ServiceNotAvailableException> handle(ServiceNotAvailableException e) {
        return Mono.error(e);
    }

}
