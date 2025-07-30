package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetInventoryByUnitNumberUseCase implements UseCase<Flux<InventoryOutput>, String>  {

    InventoryAggregateRepository inventoryRepository;
    InventoryOutputMapper mapper;

    @Override
    public Flux<InventoryOutput> execute(String unitNumber) {
        return inventoryRepository.findByUnitNumber(unitNumber)
            .filter(inventoryAggregate -> !List.of(InventoryStatus.MODIFIED, InventoryStatus.CONVERTED).contains(inventoryAggregate.getInventory().getInventoryStatus()))
            .map(ia -> mapper.toOutput(ia.getInventory(), ia.isExpired(), ia.getProperties()));
    }
}
