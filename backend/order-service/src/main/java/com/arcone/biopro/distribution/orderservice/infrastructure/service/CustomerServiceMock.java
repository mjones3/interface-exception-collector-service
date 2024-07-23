package com.arcone.biopro.distribution.orderservice.infrastructure.service;

import com.arcone.biopro.distribution.orderservice.domain.service.CustomerService;
import com.arcone.biopro.distribution.orderservice.infrastructure.controller.error.DataNotFoundException;
import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomerServiceMock implements CustomerService {

    private static final List<CustomerDTO> CUSTOMERS = new ArrayList<>();

    private final ObjectMapper objectMapper;

    @PostConstruct
    private void postConstruct() throws IOException {
        CUSTOMERS.clear();
        try (var inputStream = new ClassPathResource("mock/customer/customer-mock-data.json").getInputStream()) {
            var data = objectMapper.readValue(inputStream, CustomerDTO[].class);
            CUSTOMERS.addAll(Arrays.asList(data));
        }
    }

    public Flux<CustomerDTO> getCustomers() {
        return Flux.fromIterable(CUSTOMERS);
    }

    public Mono<CustomerDTO> getCustomerByCode(final String code) {
        return CUSTOMERS.stream()
            .filter(customer -> Objects.equals(customer.code(), code))
            .findAny()
            .map(Mono::just)
            .orElseGet(() -> Mono.error(new DataNotFoundException(code)));
    }

}
