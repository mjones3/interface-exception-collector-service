package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductsReceivedInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryUpdatedApplicationEvent;
import com.arcone.biopro.distribution.inventory.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryUpdateType;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductsReceivedUseCase implements UseCase<Mono<InventoryOutput>, ProductsReceivedInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryOutputMapper mapper;
    InventoryEventPublisher inventoryEventPublisher;

    @Override
    public Mono<InventoryOutput> execute(ProductsReceivedInput input) {
        return Flux.fromIterable(input.products())
            .flatMap(this::processProduct)
            .map(InventoryAggregate::getInventory)
            .map(mapper::toOutput)
            .last();
    }

    private Mono<InventoryAggregate> processProduct(com.arcone.biopro.distribution.inventory.application.dto.ProductReceivedInput product) {
        return inventoryAggregateRepository.findByUnitNumberAndProductCode(product.unitNumber(), product.productCode())
            .map(inventoryAggregate -> inventoryAggregate.productReceived(product.inventoryLocation(), !product.quarantines().isEmpty()))
            .flatMap(inventoryAggregateRepository::saveInventory);
    }
}
