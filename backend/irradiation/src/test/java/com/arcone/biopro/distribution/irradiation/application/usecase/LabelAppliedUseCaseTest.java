package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.InventoryInput;
import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.irradiation.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.irradiation.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.irradiation.domain.model.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.UnitNumber;
import com.arcone.biopro.distribution.irradiation.domain.repository.InventoryAggregateRepository;
import com.arcone.biopro.distribution.irradiation.domain.util.ProductCodeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LabelAppliedUseCaseTest {

    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    @InjectMocks
    private LabelAppliedUseCase labelAppliedUseCase;

    @Mock
    private InventoryEventPublisher inventoryEventPublisher;

    @Spy
    private InventoryOutputMapper mapper = Mappers.getMapper(InventoryOutputMapper.class);

    @ParameterizedTest
    @DisplayName("should update isLabeled, isLicensed and productCode information")
    @MethodSource("provideLabelAppliedUseCase")
    void test1(Boolean isLicensed, String productCode) {
        var uuid = UUID.randomUUID();
        Inventory inventory = Inventory.builder()
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

        InventoryInput input = InventoryInput.builder()
            .unitNumber("W123456789012")
            .productCode(productCode)
            .shortDescription("APH PLASMA 24H")
            .expirationDate(LocalDateTime.parse("2025-01-08T02:05:45.231"))
            .collectionDate(ZonedDateTime.now())
            .inventoryLocation("LOCATION_1")
            .productFamily("PLASMA_TRANSFUSABLE")
            .aboRh(AboRhType.ABN)
            .isLicensed(isLicensed)
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

        var productCodeWithoutSixthDigit = ProductCodeUtil.retrieveFinalProductCodeWithoutSixthDigit(input.productCode());
        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber(), productCodeWithoutSixthDigit))
            .thenReturn(Mono.just(inventoryAggregate));
        when(inventoryAggregateRepository.saveInventory(any())).thenReturn(Mono.just(inventoryAggregate));
        when(mapper.toOutput(any(Inventory.class))).thenReturn(expectedOutput);

        StepVerifier.create(labelAppliedUseCase.execute(input))
            .expectNextMatches(output -> output.equals(expectedOutput))
            .verifyComplete();

        ArgumentCaptor<InventoryAggregate> captor = ArgumentCaptor.forClass(InventoryAggregate.class);
        verify(inventoryAggregateRepository).saveInventory(captor.capture());

        InventoryAggregate savedAggregate = captor.getValue();
        assertEquals("W123456789012", savedAggregate.getInventory().getUnitNumber().value());
        assertEquals(productCode, savedAggregate.getInventory().getProductCode().value());
        assertTrue(savedAggregate.getInventory().getIsLabeled());
        assertEquals(isLicensed, savedAggregate.getInventory().getIsLicensed());
        assertEquals("2025-01-08T02:05:45.231", savedAggregate.getInventory().getExpirationDate().toString());
        assertEquals("LOCATION_1", savedAggregate.getInventory().getInventoryLocation());
    }

    private static Stream<Arguments> provideLabelAppliedUseCase() {
        return Stream.of(
            Arguments.of(true, "E1234V12"),
            Arguments.of(false, "E1234V12")
        );
    }

    @Test
    @DisplayName("should Throw InventoryNotFoundException when irradiation is not found")
    void test2() {
        InventoryInput input = new InventoryInput(
            "W123456789012",
            "E1234V12",
            "APH PLASMA 24H",
            LocalDateTime.parse("2025-01-08T02:05:45.231"),
            true,
            300,
            ZonedDateTime.now(),
            "MIAMI",
            "MIAMI",
            ZonedDateTime.now().getZone().getId(),
            "PLASMA_TRANSFUSABLE",
            AboRhType.ABN);

        var productCodeWithoutSixthDigit = ProductCodeUtil.retrieveFinalProductCodeWithoutSixthDigit(input.productCode());
        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber(), productCodeWithoutSixthDigit)).thenReturn(Mono.empty());

        StepVerifier.create(labelAppliedUseCase.execute(input))
            .expectError(InventoryNotFoundException.class)
            .verify();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode(input.unitNumber(), productCodeWithoutSixthDigit);
    }
}
