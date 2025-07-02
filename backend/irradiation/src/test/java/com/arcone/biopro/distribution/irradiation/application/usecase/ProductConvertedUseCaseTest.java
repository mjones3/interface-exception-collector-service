package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.dto.ProductConvertedInput;
import com.arcone.biopro.distribution.irradiation.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.irradiation.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.irradiation.domain.model.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.UnitNumber;
import com.arcone.biopro.distribution.irradiation.domain.repository.InventoryAggregateRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductConvertedUseCaseTest {

    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    @Spy
    private InventoryOutputMapper inventoryOutputMapper = Mappers.getMapper(InventoryOutputMapper.class);

    @InjectMocks
    private ProductConvertedUseCase productConvertedUseCase;

    @Test
    @DisplayName("should convert product successfully")
    void test1() {
        var uuid = UUID.randomUUID();
        var input = ProductConvertedInput.builder()
            .unitNumber(new UnitNumber("W123456789012"))
            .productCode(new ProductCode("E123412"))
            .build();

        var inventory = Inventory.builder()
            .id(uuid)
            .unitNumber(new UnitNumber("W123456789012"))
            .productCode(new ProductCode("E123412"))
            .shortDescription("APH PLASMA 24H")
            .inventoryStatus(InventoryStatus.AVAILABLE)
            .expirationDate(LocalDateTime.parse("2025-01-08T02:05:45.231"))
            .inventoryLocation("LOCATION_1")
            .build();

        var convertedInventory = Inventory.builder()
            .id(uuid)
            .unitNumber(new UnitNumber("W123456789012"))
            .productCode(new ProductCode("E123412"))
            .shortDescription("APH PLASMA 24H")
            .inventoryStatus(InventoryStatus.CONVERTED)
            .expirationDate(LocalDateTime.parse("2025-01-08T02:05:45.231"))
            .inventoryLocation("LOCATION_1")
            .build();

        var aggregate = InventoryAggregate.builder()
            .inventory(inventory)
            .build();

        var expectedOutput = InventoryOutput.builder()
            .unitNumber("W123456789012")
            .productCode("E123412")
            .inventoryStatus(InventoryStatus.CONVERTED)
            .expirationDate(LocalDateTime.parse("2025-01-08T02:05:45.231"))
            .location("LOCATION_1")
            .build();

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber().value(), input.productCode().value()))
            .thenReturn(Mono.just(aggregate));
        when(inventoryAggregateRepository.saveInventory(aggregate))
            .thenReturn(Mono.just(aggregate));
        when(inventoryOutputMapper.toOutput(any(Inventory.class)))
            .thenReturn(expectedOutput);

        StepVerifier.create(productConvertedUseCase.execute(input))
            .expectNextMatches(output -> {
                assertEquals("W123456789012", output.unitNumber());
                assertEquals("E123412", output.productCode());
                assertEquals(InventoryStatus.CONVERTED, output.inventoryStatus());
                assertEquals(LocalDateTime.parse("2025-01-08T02:05:45.231"), output.expirationDate());
                assertEquals("LOCATION_1", output.location());
                return true;
            })
            .verifyComplete();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode(input.unitNumber().value(), input.productCode().value());
        verify(inventoryAggregateRepository).saveInventory(aggregate);
        verify(inventoryOutputMapper).toOutput(any(Inventory.class));
    }

    @Test
    @DisplayName("should throw InventoryNotFoundException when irradiation is not found")
    void test2() {
        var input = ProductConvertedInput.builder()
            .unitNumber(new UnitNumber("W123456789012"))
            .productCode(new ProductCode("E123412"))
            .build();

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber().value(), input.productCode().value()))
            .thenReturn(Mono.empty());

        StepVerifier.create(productConvertedUseCase.execute(input))
            .expectError(InventoryNotFoundException.class)
            .verify();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode(input.unitNumber().value(), input.productCode().value());
        verifyNoMoreInteractions(inventoryAggregateRepository);
        verifyNoInteractions(inventoryOutputMapper);
    }

    @Test
    @DisplayName("should handle conversion error")
    void test3() {
        var input = ProductConvertedInput.builder()
            .unitNumber(new UnitNumber("W123456789012"))
            .productCode(new ProductCode("E123412"))
            .build();

        var aggregate = mock(InventoryAggregate.class);

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber().value(), input.productCode().value()))
            .thenReturn(Mono.just(aggregate));
        when(aggregate.convertProduct())
            .thenThrow(new IllegalStateException("Cannot convert product"));

        StepVerifier.create(productConvertedUseCase.execute(input))
            .expectError(IllegalStateException.class)
            .verify();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode(input.unitNumber().value(), input.productCode().value());
        verify(aggregate).convertProduct();
        verifyNoMoreInteractions(inventoryAggregateRepository);
        verifyNoInteractions(inventoryOutputMapper);
    }
}
