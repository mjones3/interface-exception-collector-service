package com.arcone.biopro.distribution.shipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.shipping.application.dto.CustomerDTO;
import com.arcone.biopro.distribution.shipping.application.exception.ServiceNotAvailableException;
import com.arcone.biopro.distribution.shipping.domain.service.CustomerService;
import lombok.RequiredArgsConstructor;
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
        return customerService.getCustomers()
            .flatMap(customerDTO -> Mono.just(CustomerDTO
                .builder()
                .name(customerDTO.name())
                .code(customerDTO.code())
                .build()));

    }

    @ExceptionHandler
    public Mono<ServiceNotAvailableException> handle(ServiceNotAvailableException e) {
        return Mono.error(e);
    }

}
