package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.CheckInCompletedInput;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.exception.InventoryAlreadyExistsException;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckInCompletedUseCaseTest {

    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    @Spy
    private InventoryOutputMapper mapper = Mappers.getMapper(InventoryOutputMapper.class);

    @InjectMocks
    private CheckInCompletedUseCase checkInCompletedUseCase;

    @Test
    @DisplayName("should create new inventory successfully when inventory does not exist")
    void test1() {
        var input = CheckInCompletedInput.builder()
            .unitNumber("W123456789012")
            .productCode("E123412")
            .productDescription("APH PLASMA 24H")
            .collectionDate(ZonedDateTime.now())
            .inventoryLocation("LOCATION_1")
            .collectionLocation("LOCATION_1")
            .productFamily("PLASMA_TRANSFUSABLE")
            .aboRh(AboRhType.ABN)
            .build();

        var aggregate = mock(InventoryAggregate.class);
        var inventory = mock(Inventory.class);
        var expectedOutput = InventoryOutput.builder()
            .unitNumber("W123456789012")
            .productCode("E123412")
            .inventoryStatus(InventoryStatus.AVAILABLE)
            .expirationDate(LocalDateTime.parse("2025-01-08T02:05:45.231"))
            .location("LOCATION_1")
            .build();

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber(), input.productCode()))
            .thenReturn(Mono.empty());
        when(mapper.toAggregate(input)).thenReturn(aggregate);
        when(inventoryAggregateRepository.saveInventory(aggregate))
            .thenReturn(Mono.just(aggregate));
        when(aggregate.getInventory()).thenReturn(inventory);
        when(mapper.toOutput(inventory)).thenReturn(expectedOutput);

        StepVerifier.create(checkInCompletedUseCase.execute(input))
            .expectNext(expectedOutput)
            .verifyComplete();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode(input.unitNumber(), input.productCode());
        verify(inventoryAggregateRepository).saveInventory(aggregate);
        verify(mapper).toOutput(inventory);
    }

    @Test
    @DisplayName("should throw InventoryAlreadyExistsException when inventory exists")
    void test2() {
        var input = CheckInCompletedInput.builder()
            .unitNumber("W123456789012")
            .productCode("E123412")
            .build();

        var existingAggregate = mock(InventoryAggregate.class);

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber(), input.productCode()))
            .thenReturn(Mono.just(existingAggregate));

        StepVerifier.create(checkInCompletedUseCase.execute(input))
            .expectError(InventoryAlreadyExistsException.class)
            .verify();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode(input.unitNumber(), input.productCode());
        verifyNoMoreInteractions(inventoryAggregateRepository);
        verifyNoInteractions(mapper);
    }
}
