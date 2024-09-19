package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryInput;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.exception.InventoryAlreadyExistsException;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
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

import java.time.LocalDateTime;
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
        Inventory inventory = Inventory.builder()
            .id(uuid)
            .unitNumber(new UnitNumber("W123456789012"))
            .productCode(new ProductCode("E1234V12"))
            .shortDescription("APH PLASMA 24H")
            .inventoryStatus(InventoryStatus.AVAILABLE)
            .expirationDate(LocalDateTime.parse("2025-01-08T02:05:45.231"))
            .collectionDate(ZonedDateTime.now())
            .location("LOCATION_1")
            .productFamily(ProductFamily.PLASMA_TRANSFUSABLE)
            .aboRh(AboRhType.ABN)
            .build();

        InventoryInput input = InventoryInput.builder()
            .unitNumber("W123456789012")
            .productCode("E1234V12")
            .shortDescription("APH PLASMA 24H")
            .expirationDate(LocalDateTime.parse("2025-01-08T02:05:45.231"))
            .collectionDate(ZonedDateTime.now())
            .location("LOCATION_1")
            .productFamily(ProductFamily.PLASMA_TRANSFUSABLE)
            .aboRh(AboRhType.ABN)
            .build();


        InventoryAggregate inventoryAggregate = InventoryAggregate.builder()
            .inventory(inventory)
            .build();
        InventoryOutput expectedOutput = InventoryOutput.builder()
            .unitNumber("W123456789012")
            .productCode("E1234V12")
            .inventoryStatus(InventoryStatus.AVAILABLE)
            .expirationDate(LocalDateTime.parse("2025-01-08T02:05:45.231"))
            .location("LOCATION_1")
            .build();

        when(inventoryAggregateRepository.existsByLocationAndUnitNumberAndProductCode(input.location(), input.unitNumber(), input.productCode())).thenReturn(Mono.just(false));
        when(inventoryAggregateRepository.saveInventory(any())).thenReturn(Mono.just(inventoryAggregate));
        when(mapper.toOutput(any(Inventory.class))).thenReturn(expectedOutput);

        StepVerifier.create(labelAppliedUseCase.execute(input))
            .expectNextMatches(output -> output.equals(expectedOutput))
            .verifyComplete();

        ArgumentCaptor<InventoryAggregate> captor = ArgumentCaptor.forClass(InventoryAggregate.class);
        verify(inventoryAggregateRepository).saveInventory(captor.capture());

        InventoryAggregate savedAggregate = captor.getValue();
        assertEquals("W123456789012", savedAggregate.getInventory().getUnitNumber().value());
        assertEquals("E1234V12", savedAggregate.getInventory().getProductCode().value());
        assertEquals("2025-01-08T02:05:45.231", savedAggregate.getInventory().getExpirationDate().toString());
        assertEquals("LOCATION_1", savedAggregate.getInventory().getLocation());
    }

    @Test
    @DisplayName("should Throw InventoryAlreadyExistsException when inventory already exists")
    void createInventoryAlreadyExists() {
        InventoryInput input = new InventoryInput(
            "W123456789012",
            "E1234V12",
            "APH PLASMA 24H",
            LocalDateTime.parse("2025-01-08T02:05:45.231"),
            ZonedDateTime.now(),
            "MIAMI",
            ProductFamily.PLASMA_TRANSFUSABLE,
            AboRhType.ABN);

        when(inventoryAggregateRepository.existsByLocationAndUnitNumberAndProductCode(input.location(), input.unitNumber(), input.productCode())).thenReturn(Mono.just(true));

        StepVerifier.create(labelAppliedUseCase.execute(input))
            .expectError(InventoryAlreadyExistsException.class)
            .verify();

        verify(inventoryAggregateRepository).existsByLocationAndUnitNumberAndProductCode(input.location(), input.unitNumber(), input.productCode());
    }
}
