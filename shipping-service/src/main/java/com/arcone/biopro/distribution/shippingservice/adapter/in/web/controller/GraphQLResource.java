package com.arcone.biopro.distribution.shippingservice.adapter.in.web.controller;

import com.arcone.biopro.distribution.shippingservice.domain.model.ShippingService;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShippingServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class GraphQLResource {

    private final ShippingServiceRepository repository;

    @QueryMapping
    public Flux<ShippingService> shippingServiceList() {
        return repository.findAll();
    }

}
