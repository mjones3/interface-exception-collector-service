package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductCreatedInput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductsImportedInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryCreatedEvent;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductsImportedUseCase implements UseCase<Mono<InventoryOutput>, ProductsImportedInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryOutputMapper mapper;
    InventoryEventPublisher publisher;



    @Override
    public Mono<InventoryOutput> execute(ProductsImportedInput input) {
        return Flux.fromIterable(input.getProducts())
            .flatMap(productCreatedInput -> inventoryAggregateRepository
                .findByUnitNumberAndProductCode(productCreatedInput.unitNumber(), productCreatedInput.productCode())
                .switchIfEmpty(Mono.defer(() -> buildAggregate(productCreatedInput)))
                .map(inventoryAggregate -> addProperties(inventoryAggregate, productCreatedInput))
                .flatMap(inventoryAggregateRepository::saveInventory)
               // .doOnSuccess(aggregate -> publisher.publish(new InventoryCreatedEvent(aggregate)))
                .doOnSuccess(response -> log.info("Product imported: {}", response))
                .doOnError(e -> log.error("Error occurred during import product. Input: {}, error: {}", productCreatedInput, e.getMessage(), e))
            )
            .collectList()
            .map(List::getLast)
            .map(InventoryAggregate::getInventory)
            .map(mapper::toOutput);


    }

    private Mono<InventoryAggregate> buildAggregate(ProductCreatedInput productCreatedInput) {
        return Mono.just(mapper.toAggregate(productCreatedInput));
    }

    private InventoryAggregate addProperties(InventoryAggregate inventoryAggregate, ProductCreatedInput productCreatedInput) {
        inventoryAggregate.addImportedFlag();

        if (productCreatedInput.quarantines() != null && !productCreatedInput.quarantines().isEmpty()) {
            inventoryAggregate.addQuarantineFlag();
        }

        return inventoryAggregate;

    }




}
