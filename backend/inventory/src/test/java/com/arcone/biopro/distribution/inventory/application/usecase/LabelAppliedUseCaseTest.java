package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryInput;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.exception.InventoryAlreadyExistsException;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.Location;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LabelAppliedUseCaseTest {

    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    @InjectMocks
    private LabelAppliedUseCase labelAppliedUseCase;

    @Spy
    private InventoryOutputMapper mapper = Mappers.getMapper(InventoryOutputMapper.class);

    @Test
    @DisplayName("should create an inventory")
    void createInventorySuccess() {
        var uuid = UUID.randomUUID();
        Inventory inventory = new Inventory(uuid,
            new UnitNumber("W123456789012"),
            new ProductCode("E1234V12"),
            InventoryStatus.AVAILABLE,
            "2025-01-08T02:05:45.231Z",
            Location.MIAMI,
            ZonedDateTime.now(),
            ZonedDateTime.now());
        InventoryInput input = new InventoryInput("W123456789012", "E1234V12", "2025-01-08T02:05:45.231Z", "MIAMI");
        InventoryAggregate inventoryAggregate = InventoryAggregate.builder()
            .inventory(inventory)
            .build();
        InventoryOutput expectedOutput = new InventoryOutput(uuid, "W123456789012", "E1234V12", InventoryStatus.AVAILABLE, "2025-01-08T02:05:45.231Z", "MIAMI");

        when(inventoryAggregateRepository.existsByUnitNumberAndProductCode(input.unitNumber(), input.productCode())).thenReturn(Mono.just(false));
        when(inventoryAggregateRepository.saveInventory(any())).thenReturn(Mono.just(inventoryAggregate));
        when(mapper.toOutput(any())).thenReturn(expectedOutput);

        StepVerifier.create(labelAppliedUseCase.execute(input))
            .expectNextMatches(output -> output.equals(expectedOutput))
            .verifyComplete();

        ArgumentCaptor<InventoryAggregate> captor = ArgumentCaptor.forClass(InventoryAggregate.class);
        verify(inventoryAggregateRepository).saveInventory(captor.capture());

        InventoryAggregate savedAggregate = captor.getValue();
        assertEquals("W123456789012", savedAggregate.getInventory().getUnitNumber().value());
        assertEquals("E1234V12", savedAggregate.getInventory().getProductCode().value());
        assertEquals("2025-01-08T02:05:45.231Z", savedAggregate.getInventory().getExpirationDate());
        assertEquals(Location.MIAMI, savedAggregate.getInventory().getLocation());
    }

    @Test
    @DisplayName("should Throw InventoryAlreadyExistsException when inventory already exists")
    void createInventoryAlreadyExists() {
        InventoryInput input = new InventoryInput("W123456789012", "E1234V12", "2025-01-08T02:05:45.231Z", "MIAMI");

        when(inventoryAggregateRepository.existsByUnitNumberAndProductCode(input.unitNumber(), input.productCode())).thenReturn(Mono.just(true));

        StepVerifier.create(labelAppliedUseCase.execute(input))
            .expectError(InventoryAlreadyExistsException.class)
            .verify();

        verify(inventoryAggregateRepository).existsByUnitNumberAndProductCode(input.unitNumber(), input.productCode());
    }
}
