package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductCreatedInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.application.service.ConfigurationService;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryCreatedEvent;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.inventory.domain.exception.InvalidUpdateProductStatusException;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCreatedUseCaseTest {

    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    @Mock
    private InventoryEventPublisher publisher;

    @Spy
    private InventoryOutputMapper mapper = Mappers.getMapper(InventoryOutputMapper.class);

    @InjectMocks
    private ProductCreatedUseCase productCreatedUseCase;

    @Mock
    private ConfigurationService configurationService;

    @Test
    @DisplayName("should create inventory and publish event successfully")
    void test1() {
        when(configurationService.lookUpTemperatureCategory(any())).thenReturn(Mono.empty());
        var uuid = UUID.randomUUID();
        var input = ProductCreatedInput.builder()
            .unitNumber("W123456789012")
            .productCode("E123412")
            .productDescription("APH PLASMA 24H")
            .expirationDate("01/20/2025")
            .expirationTime("00:00")
            .collectionDate(ZonedDateTime.now())
            .inventoryLocation("LOCATION_1")
            .productFamily("PLASMA_TRANSFUSABLE")
            .aboRh(AboRhType.ABN)
            .build();

        var inventory = Inventory.builder()
            .id(uuid)
            .unitNumber(new UnitNumber("W123456789012"))
            .productCode(new ProductCode("E123412"))
            .shortDescription("APH PLASMA 24H")
            .inventoryStatus(InventoryStatus.AVAILABLE)
            .expirationDate(LocalDateTime.parse("2025-01-08T02:05:45.231"))
            .collectionDate(ZonedDateTime.now())
            .inventoryLocation("LOCATION_1")
            .productFamily("PLASMA_TRANSFUSABLE")
            .aboRh(AboRhType.ABN)
            .isLabeled(false)
            .build();

        var aggregate = InventoryAggregate.builder()
            .inventory(inventory)
            .build();

        var expectedOutput = InventoryOutput.builder()
            .unitNumber("W123456789012")
            .productCode("E123412")
            .inventoryStatus(InventoryStatus.AVAILABLE)
            .expirationDate(LocalDateTime.parse("2025-01-08T02:05:45.231"))
            .location("LOCATION_1")
            .build();

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber(), input.productCode()))
            .thenReturn(Mono.just(aggregate));
        when(inventoryAggregateRepository.saveInventory(any()))
            .thenReturn(Mono.just(aggregate));
        when(mapper.toOutput(any(Inventory.class)))
            .thenReturn(expectedOutput);
        doNothing().when(publisher).publish(any(InventoryCreatedEvent.class));

        StepVerifier.create(productCreatedUseCase.execute(input))
            .expectNextMatches(output -> output.equals(expectedOutput))
            .verifyComplete();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode(input.unitNumber(), input.productCode());
        verify(inventoryAggregateRepository).saveInventory(any());
        verify(publisher).publish(any(InventoryCreatedEvent.class));
        verify(mapper).toOutput(any(Inventory.class));
    }

    @Test
    @DisplayName("should create new inventory when not found")
    void test2() {
        when(configurationService.lookUpTemperatureCategory(any())).thenReturn(Mono.empty());
        var input = ProductCreatedInput.builder()
            .unitNumber("W123456789012")
            .productCode("E123412")
            .productDescription("APH PLASMA 24H")
            .expirationDate("01/20/2025")
            .expirationTime("00:00")
            .collectionDate(ZonedDateTime.now())
            .inventoryLocation("LOCATION_1")
            .productFamily("PLASMA_TRANSFUSABLE")
            .aboRh(AboRhType.ABN)
            .build();

        var aggregate = mock(InventoryAggregate.class);
        var inventory = mock(Inventory.class);
        var expectedOutput = mock(InventoryOutput.class);

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber(), input.productCode()))
            .thenReturn(Mono.empty());
        when(mapper.toAggregate(input)).thenReturn(aggregate);
        when(aggregate.isAvailable()).thenReturn(true);
        when(aggregate.getIsLabeled()).thenReturn(false);
        when(aggregate.isQuarantined()).thenReturn(false);
        when(inventory.getProductCode()).thenReturn(new ProductCode("E123412"));
        when(inventoryAggregateRepository.saveInventory(aggregate)).thenReturn(Mono.just(aggregate));
        when(aggregate.getInventory()).thenReturn(inventory);
        when(mapper.toOutput(inventory)).thenReturn(expectedOutput);

        StepVerifier.create(productCreatedUseCase.execute(input))
            .expectNext(expectedOutput)
            .verifyComplete();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode(input.unitNumber(), input.productCode());
        verify(inventoryAggregateRepository).saveInventory(aggregate);
        verify(publisher).publish(any(InventoryCreatedEvent.class));
    }

    @Test
    @DisplayName("should throw InvalidUpdateProductStatusException when inventory is not available")
    void test3() {
        var input = ProductCreatedInput.builder()
            .unitNumber("W123456789012")
            .productCode("E123412")
            .build();

        var aggregate = mock(InventoryAggregate.class);

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber(), input.productCode()))
            .thenReturn(Mono.just(aggregate));
        when(aggregate.isAvailable()).thenReturn(false);

        StepVerifier.create(productCreatedUseCase.execute(input))
            .expectError(InvalidUpdateProductStatusException.class)
            .verify();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode(input.unitNumber(), input.productCode());
        verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("should throw InvalidUpdateProductStatusException when inventory is available but is labeled")
    void test4() {
        var input = ProductCreatedInput.builder()
            .unitNumber("W123456789012")
            .productCode("E123412")
            .build();

        var aggregate = mock(InventoryAggregate.class);

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber(), input.productCode()))
            .thenReturn(Mono.just(aggregate));
        when(aggregate.isAvailable()).thenReturn(true);
        when(aggregate.getIsLabeled()).thenReturn(true);

        StepVerifier.create(productCreatedUseCase.execute(input))
            .expectError(InvalidUpdateProductStatusException.class)
            .verify();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode(input.unitNumber(), input.productCode());
        verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("should throw InvalidUpdateProductStatusException when inventory is available, is not labeled but is quarantined")
    void test5() {
        var input = ProductCreatedInput.builder()
            .unitNumber("W123456789012")
            .productCode("E123412")
            .build();

        var aggregate = mock(InventoryAggregate.class);

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber(), input.productCode()))
            .thenReturn(Mono.just(aggregate));
        when(aggregate.isAvailable()).thenReturn(true);
        when(aggregate.getIsLabeled()).thenReturn(false);
        when(aggregate.isQuarantined()).thenReturn(true);

        StepVerifier.create(productCreatedUseCase.execute(input))
            .expectError(InvalidUpdateProductStatusException.class)
            .verify();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode(input.unitNumber(), input.productCode());
        verifyNoInteractions(publisher);
    }
}
