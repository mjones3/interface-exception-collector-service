package com.arcone.biopro.distribution.inventory.adapter.in.web.controller;

import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class GraphQLResource {

    private final InventoryRepository repository;

    @QueryMapping
    public Flux<Inventory> inventoryList() {
        return repository.findAll();
    }

}
