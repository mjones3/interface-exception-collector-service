package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.GetInventoryByUnitNumberAndProductInput;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class GetInventoryByUnitNumberAndProductCodeUseCase implements UseCase<Mono<InventoryOutput>, GetInventoryByUnitNumberAndProductInput> {

    InventoryAggregateRepository repository;

    InventoryOutputMapper mapper;

    @Override
    public Mono<InventoryOutput> execute(GetInventoryByUnitNumberAndProductInput input) {
        return repository.findByUnitNumberAndProductCode(input.unitNumber(), input.productCode())
            .map(InventoryAggregate::getInventory)
            .map(mapper::toOutput);
    }


}
