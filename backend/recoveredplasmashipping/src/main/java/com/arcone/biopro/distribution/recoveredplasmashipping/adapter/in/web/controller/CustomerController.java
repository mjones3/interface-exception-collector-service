package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CustomerDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.CustomerDtoMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerDtoMapper customerDtoMapper;


    @QueryMapping("findAllCustomers")
    public Flux<CustomerDTO> findAll() {
        return customerService.findAll()
            .map(customerDtoMapper::toCustomerDto)
            .flatMap(Mono::just);
    }

}
