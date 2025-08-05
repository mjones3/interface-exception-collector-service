package com.arcone.biopro.distribution.irradiation.unit.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.IrradiationInventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.mapper.IrradiationInventoryMapper;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateUnitNumberUseCase;
import com.arcone.biopro.distribution.irradiation.domain.exception.NoEligibleProductForIrradiationException;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.InventoryClient;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.ProductDeterminationRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.ProductCode;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateUnitNumberUseCaseTest {

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private IrradiationInventoryMapper mapper;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private ProductDeterminationRepository productDeterminationRepository;

    @InjectMocks
    private ValidateUnitNumberUseCase validateUnitNumberUseCase;

    @Test
    @DisplayName("Should return enriched inventory when valid inventory exists at target location")
    void execute_ShouldReturnEnrichedInventory_WhenValidInventoryExistsAtTargetLocation() {
        // Given
        String unitNumber = "W777725001001";
        String location = "123456789";

        Inventory inventory = createValidInventory(unitNumber, location);
        IrradiationInventoryOutput mappedDto = createMappedDto();
        IrradiationInventoryOutput expectedOutput = createExpectedOutput();

        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
                .thenReturn(Flux.just(inventory));
        when(mapper.toDomain(inventory)).thenReturn(mappedDto);
        when(batchRepository.isUnitAlreadyIrradiated(anyString(), anyString()))
                .thenReturn(Mono.just(false));
        when(productDeterminationRepository.existsBySourceProductCode(any(ProductCode.class)))
                .thenReturn(Mono.just(true));
        when(batchRepository.isUnitBeingIrradiated(anyString(), anyString()))
                .thenReturn(Mono.just(false));

        // When
        Flux<IrradiationInventoryOutput> result = validateUnitNumberUseCase.execute(unitNumber, location);

        // Then
        StepVerifier.create(result)
                .expectNext(expectedOutput)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return enriched inventory with irradiation flags when unit is already irradiated")
    void execute_ShouldReturnEnrichedInventory_WhenUnitIsAlreadyIrradiated() {
        // Given
        String unitNumber = "W777725001001";
        String location = "123456789";

        Inventory inventory = createValidInventory(unitNumber, location);
        IrradiationInventoryOutput mappedDto = createMappedDto();

        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
                .thenReturn(Flux.just(inventory));
        when(mapper.toDomain(inventory)).thenReturn(mappedDto);
        when(batchRepository.isUnitAlreadyIrradiated(anyString(), anyString()))
                .thenReturn(Mono.just(true));
        when(productDeterminationRepository.existsBySourceProductCode(any(ProductCode.class)))
                .thenReturn(Mono.just(true));
        when(batchRepository.isUnitBeingIrradiated(anyString(), anyString()))
                .thenReturn(Mono.just(false));

        // When
        Flux<IrradiationInventoryOutput> result = validateUnitNumberUseCase.execute(unitNumber, location);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(output ->
                    output.alreadyIrradiated() &&
                    !output.notConfigurableForIrradiation() &&
                    !output.isBeingIrradiated())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return enriched inventory when product is not configurable for irradiation")
    void execute_ShouldReturnEnrichedInventory_WhenProductNotConfigurableForIrradiation() {
        // Given
        String unitNumber = "W777725001001";
        String location = "123456789";

        Inventory inventory = createValidInventory(unitNumber, location);
        IrradiationInventoryOutput mappedDto = createMappedDto();

        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
                .thenReturn(Flux.just(inventory));
        when(mapper.toDomain(inventory)).thenReturn(mappedDto);
        when(batchRepository.isUnitAlreadyIrradiated(anyString(), anyString()))
                .thenReturn(Mono.just(false));
        when(productDeterminationRepository.existsBySourceProductCode(any(ProductCode.class)))
                .thenReturn(Mono.just(false));
        when(batchRepository.isUnitBeingIrradiated(anyString(), anyString()))
                .thenReturn(Mono.just(false));

        // When
        Flux<IrradiationInventoryOutput> result = validateUnitNumberUseCase.execute(unitNumber, location);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(output ->
                    !output.alreadyIrradiated() &&
                    output.notConfigurableForIrradiation() &&
                    !output.isBeingIrradiated())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return enriched inventory when unit is being irradiated")
    void execute_ShouldReturnEnrichedInventory_WhenUnitIsBeingIrradiated() {
        // Given
        String unitNumber = "W777725001001";
        String location = "123456789";

        Inventory inventory = createValidInventory(unitNumber, location);
        IrradiationInventoryOutput mappedDto = createMappedDto();

        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
                .thenReturn(Flux.just(inventory));
        when(mapper.toDomain(inventory)).thenReturn(mappedDto);
        when(batchRepository.isUnitAlreadyIrradiated(anyString(), anyString()))
                .thenReturn(Mono.just(false));
        when(productDeterminationRepository.existsBySourceProductCode(any(ProductCode.class)))
                .thenReturn(Mono.just(true));
        when(batchRepository.isUnitBeingIrradiated(anyString(), anyString()))
                .thenReturn(Mono.just(true));

        // When
        Flux<IrradiationInventoryOutput> result = validateUnitNumberUseCase.execute(unitNumber, location);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(output ->
                    !output.alreadyIrradiated() &&
                    !output.notConfigurableForIrradiation() &&
                    output.isBeingIrradiated())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle multiple valid inventories at target location")
    void execute_ShouldHandleMultipleValidInventories_WhenMultipleInventoriesAtTargetLocation() {
        // Given
        String unitNumber = "W777725001001";
        String location = "123456789";

        Inventory inventory1 = createValidInventory(unitNumber, location);
        Inventory inventory2 = createValidInventory(unitNumber, location);
        IrradiationInventoryOutput mappedDto = createMappedDto();

        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
                .thenReturn(Flux.just(inventory1, inventory2));
        when(mapper.toDomain(any(Inventory.class))).thenReturn(mappedDto);
        when(batchRepository.isUnitAlreadyIrradiated(anyString(), anyString()))
                .thenReturn(Mono.just(false));
        when(productDeterminationRepository.existsBySourceProductCode(any(ProductCode.class)))
                .thenReturn(Mono.just(true));
        when(batchRepository.isUnitBeingIrradiated(anyString(), anyString()))
                .thenReturn(Mono.just(false));

        // When
        Flux<IrradiationInventoryOutput> result = validateUnitNumberUseCase.execute(unitNumber, location);

        // Then
        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw NoEligibleProductForIrradiationException when no inventory found")
    void execute_ShouldThrowException_WhenNoInventoryFound() {
        // Given
        String unitNumber = "INVALID-UNIT";
        String location = "123456789";
        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
                .thenReturn(Flux.empty());

        // When
        Flux<IrradiationInventoryOutput> result = validateUnitNumberUseCase.execute(unitNumber, location);

        // Then
        StepVerifier.create(result)
                .expectError(NoEligibleProductForIrradiationException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw NoEligibleProductForIrradiationException when inventory not at target location")
    void execute_ShouldThrowException_WhenInventoryNotAtTargetLocation() {
        // Given
        String unitNumber = "W777725001001";
        String location = "123456789";
        String differentLocation = "987654321";

        Inventory inventory = createValidInventory(unitNumber, differentLocation);

        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
                .thenReturn(Flux.just(inventory));

        // When
        Flux<IrradiationInventoryOutput> result = validateUnitNumberUseCase.execute(unitNumber, location);

        // Then
        StepVerifier.create(result)
                .expectError(NoEligibleProductForIrradiationException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw NoEligibleProductForIrradiationException when inventory not available")
    void execute_ShouldThrowException_WhenInventoryNotAvailable() {
        // Given
        String unitNumber = "W777725001001";
        String location = "123456789";

        Inventory inventory = createInventoryWithStatus(unitNumber, location, "SHIPPED");

        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
                .thenReturn(Flux.just(inventory));

        // When
        Flux<IrradiationInventoryOutput> result = validateUnitNumberUseCase.execute(unitNumber, location);

        // Then
        StepVerifier.create(result)
                .expectError(NoEligibleProductForIrradiationException.class)
                .verify();
    }

    @Test
    @DisplayName("Should handle discarded inventory as valid for irradiation")
    void execute_ShouldHandleDiscardedInventory_WhenInventoryIsDiscarded() {
        // Given
        String unitNumber = "W777725001001";
        String location = "123456789";

        Inventory inventory = createInventoryWithStatus(unitNumber, location, "DISCARDED");
        IrradiationInventoryOutput mappedDto = createMappedDto();

        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
                .thenReturn(Flux.just(inventory));
        when(mapper.toDomain(inventory)).thenReturn(mappedDto);
        when(batchRepository.isUnitAlreadyIrradiated(anyString(), anyString()))
                .thenReturn(Mono.just(false));
        when(productDeterminationRepository.existsBySourceProductCode(any(ProductCode.class)))
                .thenReturn(Mono.just(true));
        when(batchRepository.isUnitBeingIrradiated(anyString(), anyString()))
                .thenReturn(Mono.just(false));

        // When
        Flux<IrradiationInventoryOutput> result = validateUnitNumberUseCase.execute(unitNumber, location);

        // Then
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should propagate error when inventory client fails")
    void execute_ShouldPropagateError_WhenInventoryClientFails() {
        // Given
        String unitNumber = "W777725001001";
        String location = "123456789";
        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
                .thenReturn(Flux.error(new RuntimeException("Client error")));

        // When
        Flux<IrradiationInventoryOutput> result = validateUnitNumberUseCase.execute(unitNumber, location);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should propagate error when batch repository isUnitAlreadyIrradiated fails")
    void execute_ShouldPropagateError_WhenBatchRepositoryIsUnitAlreadyIrradiatedFails() {
        // Given
        String unitNumber = "W777725001001";
        String location = "123456789";

        Inventory inventory = createValidInventory(unitNumber, location);
        IrradiationInventoryOutput mappedDto = createMappedDto();

        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
                .thenReturn(Flux.just(inventory));

        when(batchRepository.isUnitAlreadyIrradiated(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Repository error")));

        // When
        Flux<IrradiationInventoryOutput> result = validateUnitNumberUseCase.execute(unitNumber, location);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should propagate error when batch repository isUnitBeingIrradiated fails")
    void execute_ShouldPropagateError_WhenBatchRepositoryIsUnitBeingIrradiatedFails() {
        // Given
        String unitNumber = "W777725001001";
        String location = "123456789";

        Inventory inventory = createValidInventory(unitNumber, location);
        IrradiationInventoryOutput mappedDto = createMappedDto();

        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
                .thenReturn(Flux.just(inventory));

        when(batchRepository.isUnitAlreadyIrradiated(anyString(), anyString()))
                .thenReturn(Mono.just(false));
        when(productDeterminationRepository.existsBySourceProductCode(any(ProductCode.class)))
                .thenReturn(Mono.just(true));
        when(batchRepository.isUnitBeingIrradiated(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Repository error")));

        // When
        Flux<IrradiationInventoryOutput> result = validateUnitNumberUseCase.execute(unitNumber, location);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should propagate error when product determination repository fails")
    void execute_ShouldPropagateError_WhenProductDeterminationRepositoryFails() {
        // Given
        String unitNumber = "W777725001001";
        String location = "123456789";

        Inventory inventory = createValidInventory(unitNumber, location);
        IrradiationInventoryOutput mappedDto = createMappedDto();

        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
                .thenReturn(Flux.just(inventory));

        when(batchRepository.isUnitAlreadyIrradiated(anyString(), anyString()))
                .thenReturn(Mono.just(false));
        when(productDeterminationRepository.existsBySourceProductCode(any(ProductCode.class)))
                .thenReturn(Mono.error(new RuntimeException("Repository error")));

        // When
        Flux<IrradiationInventoryOutput> result = validateUnitNumberUseCase.execute(unitNumber, location);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    private Inventory createValidInventory(String unitNumber, String location) {
        return createInventoryWithStatus(unitNumber, location, "AVAILABLE");
    }

    private Inventory createInventoryWithStatus(String unitNumber, String location, String status) {
        return Inventory.builder()
                .unitNumber(new UnitNumber(unitNumber))
                .productCode("PROD001")
                .location(new Location(location))
                .status(status)
                .productDescription("Test Product")
                .productFamily("Test Family")
                .statusReason("Test Reason")
                .unsuitableReason(null)
                .expired(false)
                .isBeingIrradiated(false)
                .quarantines(List.of())
                .build();
    }

    private IrradiationInventoryOutput createMappedDto() {
        return IrradiationInventoryOutput.builder()
                .unitNumber("W777725001001")
                .productCode("PROD001")
                .location("123456789")
                .status("AVAILABLE")
                .productDescription("Test Product")
                .productFamily("Test Family")
                .shortDescription("Short Desc")
                .isLabeled(true)
                .statusReason("Test Reason")
                .unsuitableReason(null)
                .expired(false)
                .isBeingIrradiated(false)
                .quarantines(List.of())
                .alreadyIrradiated(false)
                .notConfigurableForIrradiation(false)
                .build();
    }

    private IrradiationInventoryOutput createExpectedOutput() {
        return IrradiationInventoryOutput.builder()
                .unitNumber("W777725001001")
                .productCode("PROD001")
                .location("123456789")
                .status("AVAILABLE")
                .productDescription("Test Product")
                .productFamily("Test Family")
                .shortDescription("Short Desc")
                .isLabeled(true)
                .statusReason("Test Reason")
                .unsuitableReason(null)
                .expired(false)
                .isBeingIrradiated(false)
                .quarantines(List.of())
                .alreadyIrradiated(false)
                .notConfigurableForIrradiation(false)
                .build();
    }
}
