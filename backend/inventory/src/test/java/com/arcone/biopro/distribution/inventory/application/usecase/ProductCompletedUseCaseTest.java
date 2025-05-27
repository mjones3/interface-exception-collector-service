package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductCompletedInput;
import com.arcone.biopro.distribution.inventory.application.dto.VolumeInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.application.mapper.VolumeInputMapper;
import com.arcone.biopro.distribution.inventory.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.Volume;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCompletedUseCaseTest {

    private static final String UNIT_NUMBER = "W777725111110";
    private static final String PRODUCT_CODE = "E123412";
    private static final String SHORT_DESCRIPTION = "APH PLASMA 24H";
    private static final String LOCATION = "LOCATION_1";
    private static final String PRODUCT_FAMILY = "PLASMA_TRANSFUSABLE";
    private static final String VOLUME_TYPE = "volume";
    private static final int VOLUME_VALUE = 50;
    private static final String VOLUME_UNIT = "MILLILITERS";
    private static final String ANTICOAGULANT_VOLUME_TYPE = "anticoagulantVolume";

    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    @Spy
    private InventoryOutputMapper inventoryOutputMapper = Mappers.getMapper(InventoryOutputMapper.class);

    @Mock
    private VolumeInputMapper volumeInputMapper;

    @InjectMocks
    private ProductCompletedUseCase productCompletedUseCase;

    @Test
    @DisplayName("Should Throws An Exception When Product Not Found")
    void shouldThrowsAnExceptionWhenProductNotFound() {

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(UNIT_NUMBER, PRODUCT_CODE))
            .thenReturn(Mono.empty());

        var input = ProductCompletedInput.builder()
            .unitNumber(UNIT_NUMBER)
            .productCode(PRODUCT_CODE)
            .volumes(List.of())
            .aboRh(AboRhType.ABN)
            .build();

        Mono<InventoryOutput> result = productCompletedUseCase.execute(input);

        StepVerifier.create(result)
            .expectErrorMatches(throwable -> throwable instanceof InventoryNotFoundException && throwable.getMessage()
                .equals("Inventory does not exist"))
            .verify();
    }

    @Test
    @DisplayName("Should Update The Volumes To Default When No Volume Present")
    void shouldUpdateTheVolumeToDefaultWhenNoVolumePresent() {

        var inventory = Inventory.builder()
            .id(UUID.randomUUID())
            .unitNumber(new UnitNumber(UNIT_NUMBER))
            .productCode(new ProductCode(PRODUCT_CODE))
            .shortDescription(SHORT_DESCRIPTION)
            .inventoryStatus(InventoryStatus.AVAILABLE)
            .expirationDate(LocalDateTime.now().plusMonths(2))
            .collectionDate(ZonedDateTime.now())
            .inventoryLocation(LOCATION)
            .productFamily(PRODUCT_FAMILY)
            .aboRh(AboRhType.ABN)
            .isLabeled(false)
            .build();

        var expectedInventoryOutput = InventoryOutput.builder()
            .unitNumber(UNIT_NUMBER)
            .productCode(PRODUCT_CODE)
            .inventoryStatus(InventoryStatus.AVAILABLE)
            .expirationDate(LocalDateTime.now().plusMonths(2))
            .location(LOCATION)
            .build();

        var inventoryAggregate = InventoryAggregate.builder()
            .inventory(inventory)
            .build();

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(UNIT_NUMBER, PRODUCT_CODE))
            .thenReturn(Mono.just(inventoryAggregate));

        when(inventoryAggregateRepository.saveInventory(any()))
            .thenReturn(Mono.just(inventoryAggregate));

        when(inventoryOutputMapper.toOutput(any(Inventory.class)))
            .thenReturn(expectedInventoryOutput);

        when(volumeInputMapper.toDomain(anyList())).thenReturn(List.of());

        var input = ProductCompletedInput.builder()
            .unitNumber(UNIT_NUMBER)
            .productCode(PRODUCT_CODE)
            .volumes(List.of())
            .aboRh(AboRhType.ABN)
            .build();

        Mono<InventoryOutput> result = productCompletedUseCase.execute(input);

        StepVerifier.create(result)
            .expectNextMatches(output -> output.equals(expectedInventoryOutput))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should Update The Volumes")
    void shouldUpdateTheVolumes() {

        var inventory = Inventory.builder()
            .id(UUID.randomUUID())
            .unitNumber(new UnitNumber(UNIT_NUMBER))
            .productCode(new ProductCode(PRODUCT_CODE))
            .shortDescription(SHORT_DESCRIPTION)
            .inventoryStatus(InventoryStatus.AVAILABLE)
            .expirationDate(LocalDateTime.now().plusMonths(2))
            .collectionDate(ZonedDateTime.now())
            .inventoryLocation(LOCATION)
            .productFamily(PRODUCT_FAMILY)
            .aboRh(AboRhType.ABN)
            .isLabeled(false)
            .build();

        var expectedInventoryOutput = InventoryOutput.builder()
            .unitNumber(UNIT_NUMBER)
            .productCode(PRODUCT_CODE)
            .inventoryStatus(InventoryStatus.AVAILABLE)
            .expirationDate(LocalDateTime.now().plusMonths(2))
            .location(LOCATION)
            .build();

        var inventoryAggregate = InventoryAggregate.builder()
            .inventory(inventory)
            .build();

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(UNIT_NUMBER, PRODUCT_CODE))
            .thenReturn(Mono.just(inventoryAggregate));

        when(inventoryAggregateRepository.saveInventory(any()))
            .thenReturn(Mono.just(inventoryAggregate));

        when(inventoryOutputMapper.toOutput(any(Inventory.class)))
            .thenReturn(expectedInventoryOutput);

        var volume = Volume.builder()
            .type(VOLUME_TYPE)
            .unit(VOLUME_UNIT)
            .value(VOLUME_VALUE)
            .build();

        List<Volume> volumes = List.of(volume);
        when(volumeInputMapper.toDomain(anyList())).thenReturn(volumes);

        VolumeInput volumeInput = new VolumeInput(VOLUME_TYPE, VOLUME_VALUE, VOLUME_UNIT);
        VolumeInput anticoagulantVolumeInput = new VolumeInput(ANTICOAGULANT_VOLUME_TYPE, 50, VOLUME_UNIT);

        var input = ProductCompletedInput.builder()
            .unitNumber(UNIT_NUMBER)
            .productCode(PRODUCT_CODE)
            .volumes(List.of(volumeInput, anticoagulantVolumeInput))
            .aboRh(AboRhType.ABN)
            .build();

        Mono<InventoryOutput> result = productCompletedUseCase.execute(input);

        StepVerifier.create(result)
            .expectNextMatches(output -> output.equals(expectedInventoryOutput))
            .verifyComplete();
    }


}
