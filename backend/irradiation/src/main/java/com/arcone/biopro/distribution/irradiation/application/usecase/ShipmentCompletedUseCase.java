package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.dto.ShipmentCompletedInput;
import com.arcone.biopro.distribution.irradiation.application.dto.ShipmentCompletedOutput;
import com.arcone.biopro.distribution.irradiation.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.irradiation.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.irradiation.domain.event.InventoryUpdatedApplicationEvent;
import com.arcone.biopro.distribution.irradiation.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.InventoryUpdateType;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.ShipmentType;
import com.arcone.biopro.distribution.irradiation.domain.repository.InventoryAggregateRepository;
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
public class ShipmentCompletedUseCase implements UseCase<Mono<ShipmentCompletedOutput>, ShipmentCompletedInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryOutputMapper mapper;
    InventoryEventPublisher inventoryEventPublisher;

    @Override
    public Mono<ShipmentCompletedOutput> execute(ShipmentCompletedInput input) {
        return Flux.fromIterable(input.lineItems())
            .flatMap(lineItem -> Flux.fromIterable(lineItem.products()))
            .flatMap(product -> processProduct(product, input.shipmentType()))
            .collectList()
            .map(inventoryOutputs -> createShipmentCompletedOutput(input, inventoryOutputs));
    }

    private Mono<InventoryOutput> processProduct(ShipmentCompletedInput.LineItem.Product product, ShipmentType shipmentType) {
        return inventoryAggregateRepository.findByUnitNumberAndProductCode(product.unitNumber(), product.productCode())
            .switchIfEmpty(Mono.error(InventoryNotFoundException::new))
            .flatMap(inventoryAggregate -> inventoryAggregateRepository.saveInventory(
                inventoryAggregate.completeShipment(shipmentType)
            ))
            .map(InventoryAggregate::getInventory)
            .doOnSuccess(inventory -> inventoryEventPublisher.publish(new InventoryUpdatedApplicationEvent(inventory, InventoryUpdateType.SHIPPED)))
            .map(mapper::toOutput);
    }

    private ShipmentCompletedOutput createShipmentCompletedOutput(ShipmentCompletedInput input, List<InventoryOutput> inventoryOutputs) {
        return new ShipmentCompletedOutput(
            input.shipmentId(),
            input.orderNumber(),
            input.performedBy(),
            inventoryOutputs
        );
    }
}
