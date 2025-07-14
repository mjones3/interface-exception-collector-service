package com.arcone.biopro.distribution.irradiation.unit.domain.irradiation.aggregate;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate.IrradiationAggregate;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IrradiationAggregateTest {

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
}
