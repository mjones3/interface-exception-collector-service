package com.arcone.biopro.distribution.irradiation.unit.domain.irradiation.aggregate;

import com.arcone.biopro.distribution.irradiation.domain.exception.BatchSubmissionException;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate.IrradiationAggregate;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.ProductDetermination;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.ProductCode;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import com.arcone.biopro.distribution.irradiation.domain.service.ProductDeterminationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IrradiationAggregateTest {

    @Mock
    private ProductDeterminationService productDeterminationService;

    @Mock
    private Device device;

    @Mock
    private Batch batch;

    @Test
    void canSubmitBatch_ShouldReturnTrue_WhenAllUnitNumbersAreAvailable() {
        // Given
        Location location = new Location("123456789");

        List<Inventory> inventories = List.of(
            Inventory.builder()
                .unitNumber(new UnitNumber("W777725001001"))
                .productCode("PROD001")
                .location(location)
                .status("AVAILABLE")
                .build(),
            Inventory.builder()
                .unitNumber(new UnitNumber("W777725001002"))
                .productCode("PROD002")
                .location(location)
                .status("AVAILABLE")
                .build()
        );

        IrradiationAggregate aggregate = new IrradiationAggregate(null, inventories, null);
        List<BatchItem> batchItems = List.of(

            BatchItem.builder()
                .unitNumber(new UnitNumber("W777725001001"))
                .productCode("PROD001")
                .lotNumber("LOT001")
                .build(),
            BatchItem.builder()
                .unitNumber(new UnitNumber("W777725001001"))
                .productCode("PROD001")
                .lotNumber("LOT001")
                .build()
        );

        // When
        boolean result = aggregate.canSubmitBatch(batchItems, location);

        // Then
        assertTrue(result);
    }

    @Test
    void canSubmitBatch_ShouldReturnFalse_WhenUnitNumberNotAvailable() {
        // Given
        Location location = new Location("123456789");
        List<Inventory> inventories = List.of(
            Inventory.builder()
                .unitNumber(new UnitNumber("W777725001001"))
                .productCode("PROD001")
                .location(location)
                .status("AVAILABLE")
                .build()
        );

        IrradiationAggregate aggregate = new IrradiationAggregate(null, inventories, null);
        List<BatchItem> batchItems = List.of(
            BatchItem.builder()
                .unitNumber(new UnitNumber("W777725001001"))
                .productCode("PROD001")
                .lotNumber("LOT001")
                .build(),
            BatchItem.builder()
                .unitNumber(new UnitNumber("W777725001999"))
                .productCode("PROD999")
                .lotNumber("LOT999")
                .build() // Not available
        );

        // When
        boolean result = aggregate.canSubmitBatch(batchItems, location);

        // Then
        assertFalse(result);
    }

    @Test
    void canSubmitBatch_ShouldReturnFalse_WhenUnitNumbersListIsEmpty() {
        // Given
        Location location = new Location("123456789");
        IrradiationAggregate aggregate = new IrradiationAggregate(null, List.of(), null);

        // When
        boolean result = aggregate.canSubmitBatch(List.of(), location);

        // Then
        assertFalse(result);
    }

    @Test
    void validateDevice_ShouldReturnTrue_WhenDeviceIsAtLocation() {
        // Given
        Location location = new Location("123456789");
        when(device.isAtLocation(location)).thenReturn(true);
        IrradiationAggregate aggregate = new IrradiationAggregate(device, List.of(), null);

        // When
        boolean result = aggregate.validateDevice(location);

        // Then
        assertTrue(result);
    }

    @Test
    void validateDevice_ShouldReturnFalse_WhenDeviceIsNull() {
        // Given
        Location location = new Location("123456789");
        IrradiationAggregate aggregate = new IrradiationAggregate(null, List.of(), null);

        // When
        boolean result = aggregate.validateDevice(location);

        // Then
        assertFalse(result);
    }

    @Test
    void validateDeviceIsInUse_ShouldReturnTrue_WhenBatchIsActive() {
        // Given
        when(batch.isActive()).thenReturn(true);
        IrradiationAggregate aggregate = new IrradiationAggregate(null, List.of(), batch);

        // When
        boolean result = aggregate.validateDeviceIsInUse();

        // Then
        assertTrue(result);
    }

    @Test
    void getValidInventoriesForIrradiation_ShouldReturnAvailableInventories() {
        // Given
        Location location = new Location("123456789");
        List<Inventory> inventories = List.of(
            Inventory.builder()
                .unitNumber(new UnitNumber("W777725001001"))
                .productCode("PROD001")
                .location(location)
                .status("AVAILABLE")
                .build(),
            Inventory.builder()
                .unitNumber(new UnitNumber("W777725001002"))
                .productCode("PROD002")
                .location(location)
                .status("USED")
                .build()
        );
        IrradiationAggregate aggregate = new IrradiationAggregate(null, inventories, null);

        // When
        List<Inventory> result = aggregate.getValidInventoriesForIrradiation(location);

        // Then
        assertEquals(1, result.size());
        assertEquals("AVAILABLE", result.get(0).getStatus());
    }

    @Test
    void determineTargetProduct_ShouldReturnProductCode_WhenServiceAvailable() {
        // Given
        ProductCode sourceCode = new ProductCode("SOURCE001");
        ProductCode targetCode = new ProductCode("TARGET001");
        when(productDeterminationService.determineTargetProduct(sourceCode)).thenReturn(Mono.just(targetCode));
        IrradiationAggregate aggregate = new IrradiationAggregate(null, List.of(), null, productDeterminationService);

        // When & Then
        StepVerifier.create(aggregate.determineTargetProduct(sourceCode))
            .expectNext(targetCode)
            .verifyComplete();
    }

    @Test
    void determineTargetProduct_ShouldThrowException_WhenServiceNotAvailable() {
        // Given
        ProductCode sourceCode = new ProductCode("SOURCE001");
        IrradiationAggregate aggregate = new IrradiationAggregate(null, List.of(), null);

        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> aggregate.determineTargetProduct(sourceCode));
    }

    @Test
    void getIrradiatedItems_ShouldReturnOnlyIrradiatedItems() {
        // Given
        List<IrradiationAggregate.BatchItemCompletion> completions = List.of(
            new IrradiationAggregate.BatchItemCompletion("W777725001001", "PROD001", true),
            new IrradiationAggregate.BatchItemCompletion("W777725001002", "PROD002", false)
        );
        IrradiationAggregate aggregate = new IrradiationAggregate(null, null, completions, Map.of(), LocalDateTime.now());

        // When
        List<IrradiationAggregate.BatchItemCompletion> result = aggregate.getIrradiatedItems();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).isIrradiated());
    }

    @Test
    void getNonIrradiatedItems_ShouldReturnOnlyNonIrradiatedItems() {
        // Given
        List<IrradiationAggregate.BatchItemCompletion> completions = List.of(
            new IrradiationAggregate.BatchItemCompletion("W777725001001", "PROD001", true),
            new IrradiationAggregate.BatchItemCompletion("W777725001002", "PROD002", false)
        );
        IrradiationAggregate aggregate = new IrradiationAggregate(null, null, completions, Map.of(), LocalDateTime.now());

        // When
        List<IrradiationAggregate.BatchItemCompletion> result = aggregate.getNonIrradiatedItems();

        // Then
        assertEquals(1, result.size());
        assertFalse(result.get(0).isIrradiated());
    }

    @Test
    void isConfiguredForCompletion_ShouldReturnTrue_WhenAllRequiredFieldsPresent() {
        // Given
        List<IrradiationAggregate.BatchItemCompletion> completions = List.of();
        Map<String, ProductDetermination> determinations = Map.of();
        LocalDateTime completionTime = LocalDateTime.now();
        IrradiationAggregate aggregate = new IrradiationAggregate(null, null, completions, determinations, completionTime);

        // When
        boolean result = aggregate.isConfiguredForCompletion();

        // Then
        assertTrue(result);
    }

    @Test
    void processIrradiatedItem_ShouldReturnUpdatedBatchItem() {
        // Given
        ProductDetermination determination = ProductDetermination.builder()
            .sourceProductCode(new ProductCode("PROD001"))
            .targetProductCode(new ProductCode("TARGET001"))
            .build();
        Map<String, ProductDetermination> determinations = Map.of("PROD001", determination);
        IrradiationAggregate.BatchItemCompletion completion = new IrradiationAggregate.BatchItemCompletion("W777725001001", "PROD001", true);
        BatchItem originalItem = BatchItem.builder()
            .unitNumber(new UnitNumber("W777725001001"))
            .productCode("PROD001")
            .lotNumber("LOT001")
            .build();
        IrradiationAggregate aggregate = new IrradiationAggregate(null, null, List.of(), determinations, LocalDateTime.now());

        // When
        BatchItem result = aggregate.processIrradiatedItem(completion, originalItem);

        // Then
        assertEquals("TARGET001", result.newProductCode());
        assertEquals("PROD001", result.productCode());
    }

    @Test
    void validateBatchCompletion_ShouldThrowException_WhenBatchNotActive() {
        // Given
        when(batch.isActive()).thenReturn(false);
        IrradiationAggregate aggregate = new IrradiationAggregate(null, null, batch);

        // When & Then
        assertThrows(BatchSubmissionException.class, () -> aggregate.validateBatchCompletion());
    }
}
