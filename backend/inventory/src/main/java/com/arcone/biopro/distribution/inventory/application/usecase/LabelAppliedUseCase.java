package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryInput;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service class for Inventory.
 */
@Service
@RequiredArgsConstructor
@Slf4j
class LabelAppliedUseCase implements UseCase<Mono<InventoryOutput>, InventoryInput> {

    private final InventoryAggregateRepository inventoryAggregateRepository;

    @Override
    public Mono<InventoryOutput> execute(InventoryInput input) {
        return null;
    }
}
