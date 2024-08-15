package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.GetAllAvailableInventoriesInput;
import com.arcone.biopro.distribution.inventory.application.dto.GetAllAvailableInventoriesOutput;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryCriteria;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryFamily;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
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
class GetAllAvailableInventoriesUseCase implements UseCase<Mono<GetAllAvailableInventoriesOutput>, GetAllAvailableInventoriesInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryOutputMapper mapper;

    @Override
    public Mono<GetAllAvailableInventoriesOutput> execute(GetAllAvailableInventoriesInput input) {

        return Flux.fromIterable(input.inventoryCriteria())
            .flatMap(criteria -> getAllAvailableByCriteria(input.location(), criteria))
            .collectList()
            .map(inventoryFamilies -> mapper.toOutput(input.location(), inventoryFamilies));
    }

    private Mono<InventoryFamily> getAllAvailableByCriteria(String location, InventoryCriteria inventoryCriteria) {
        Mono<List<InventoryAggregate>> shortDateProducts = this.inventoryAggregateRepository.findAllAvailableShortDate(location, inventoryCriteria.productFamily(), inventoryCriteria.aboRh())
            .collectList();

        Mono<Long> countAvailable = this.inventoryAggregateRepository.countAllAvailable(location, inventoryCriteria.productFamily(), inventoryCriteria.aboRh());

        return Mono.zip(shortDateProducts, countAvailable)
            .map(tuple -> mapper.toOutput(inventoryCriteria.productFamily(), inventoryCriteria.aboRh(), tuple.getT2(), tuple.getT1()));
    }
}
