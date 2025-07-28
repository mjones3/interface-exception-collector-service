package com.arcone.biopro.distribution.irradiation.unit.application.mapper;

import com.arcone.biopro.distribution.irradiation.application.dto.IrradiationInventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.mapper.IrradiationInventoryMapper;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.InventoryQuarantine;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IrradiationInventoryMapperTest {

    private final IrradiationInventoryMapper mapper = Mappers.getMapper(IrradiationInventoryMapper.class);

    @Test
    @DisplayName("Should map Inventory to IrradiationInventoryOutput successfully")
    void toDomain_Success() {
        // Given
        UnitNumber unitNumber = new UnitNumber("W777725001001");
        Location location = new Location("123456789");
        List<InventoryQuarantine> quarantines = List.of(
            new InventoryQuarantine("POSITIVE_REACTIVE_TEST_RESULTS", "Test comment", true),
            new InventoryQuarantine("EXPIRED", "Expired product", false)
        );

        Inventory inventory = Inventory.builder()
            .unitNumber(unitNumber)
            .productCode("E033600")
            .location(location)
            .status("AVAILABLE")
            .productDescription("AS1 LR RBC")
            .productFamily("RED_BLOOD_CELLS_LEUKOREDUCED")
            .expirationDate(LocalDateTime.now().plusDays(30))
            .statusReason("NORMAL")
            .unsuitableReason(null)
            .expired(false)
            .isImported(false)
            .isBeingIrradiated(false)
            .quarantines(quarantines)
            .build();

        // When
        IrradiationInventoryOutput result = mapper.toDomain(inventory);

        // Then
        assertNotNull(result);
        assertEquals("W777725001001", result.unitNumber());
        assertEquals("E033600", result.productCode());
        assertEquals("123456789", result.location());
        assertEquals("AVAILABLE", result.status());
        assertEquals("AS1 LR RBC", result.productDescription());
        assertEquals("RED_BLOOD_CELLS_LEUKOREDUCED", result.productFamily());
        assertEquals("NORMAL", result.statusReason());
        assertNull(result.unsuitableReason());
        assertEquals(false, result.expired());
        assertEquals(false, result.isImported());
        assertNull(result.isBeingIrradiated());
        assertEquals(2, result.quarantines().size());

        // Verify ignored fields are set to default values
        assertFalse(result.alreadyIrradiated());
        assertFalse(result.notConfigurableForIrradiation());

        // Verify missing fields have default values
        assertNull(result.shortDescription());
        assertFalse(result.isLabeled());
    }

    @Test
    @DisplayName("Should handle null values in Inventory mapping")
    void toDomain_WithNullValues() {
        // Given
        UnitNumber unitNumber = new UnitNumber("W777725001002");
        Location location = new Location("987654321");

        Inventory inventory = Inventory.builder()
            .unitNumber(unitNumber)
            .productCode("E068600")
            .location(location)
            .status("QUARANTINE")
            .productDescription(null)
            .productFamily(null)
            .expirationDate(null)
            .statusReason(null)
            .unsuitableReason(null)
            .expired(null)
            .isImported(null)
            .isBeingIrradiated(null)
            .quarantines(null)
            .build();

        // When
        IrradiationInventoryOutput result = mapper.toDomain(inventory);

        // Then
        assertNotNull(result);
        assertEquals("W777725001002", result.unitNumber());
        assertEquals("E068600", result.productCode());
        assertEquals("987654321", result.location());
        assertEquals("QUARANTINE", result.status());
        assertNull(result.productDescription());
        assertNull(result.productFamily());
        assertNull(result.statusReason());
        assertNull(result.unsuitableReason());
        assertNull(result.expired());
        assertNull(result.isImported());
        assertNull(result.isBeingIrradiated());
        assertNull(result.quarantines());

        // Verify ignored fields are set to default values
        assertFalse(result.alreadyIrradiated());
        assertFalse(result.notConfigurableForIrradiation());

        // Verify missing fields have default values
        assertNull(result.shortDescription());
        assertFalse(result.isLabeled());
    }

    @Test
    @DisplayName("Should map InventoryQuarantine correctly")
    void toDomain_InventoryQuarantine_Success() {
        // Given
        InventoryQuarantine quarantine = new InventoryQuarantine(
            "POSITIVE_REACTIVE_TEST_RESULTS",
            "Product failed safety tests",
            true
        );

        // When
        InventoryQuarantine result = mapper.toDomain(quarantine);

        // Then
        assertNotNull(result);
        assertEquals("POSITIVE_REACTIVE_TEST_RESULTS", result.reason());
        assertEquals("Product failed safety tests", result.comments());
        assertTrue(result.stopsManufacturing());
    }

    @Test
    @DisplayName("Should map list of InventoryQuarantine correctly")
    void toDomain_InventoryQuarantineList_Success() {
        // Given
        List<InventoryQuarantine> quarantines = List.of(
            new InventoryQuarantine("EXPIRED", "Product expired", false),
            new InventoryQuarantine("DAMAGED", "Physical damage", true),
            new InventoryQuarantine("CONTAMINATED", "Contamination detected", true)
        );

        // When
        List<InventoryQuarantine> result = mapper.toDomain(quarantines);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        assertEquals("EXPIRED", result.get(0).reason());
        assertEquals("Product expired", result.get(0).comments());
        assertFalse(result.get(0).stopsManufacturing());

        assertEquals("DAMAGED", result.get(1).reason());
        assertEquals("Physical damage", result.get(1).comments());
        assertTrue(result.get(1).stopsManufacturing());

        assertEquals("CONTAMINATED", result.get(2).reason());
        assertEquals("Contamination detected", result.get(2).comments());
        assertTrue(result.get(2).stopsManufacturing());
    }

    @Test
    @DisplayName("Should handle empty list of InventoryQuarantine")
    void toDomain_EmptyInventoryQuarantineList() {
        // Given
        List<InventoryQuarantine> emptyQuarantines = List.of();

        // When
        List<InventoryQuarantine> result = mapper.toDomain(emptyQuarantines);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should map Inventory with empty quarantines list")
    void toDomain_WithEmptyQuarantines() {
        // Given
        UnitNumber unitNumber = new UnitNumber("W777725001003");
        Location location = new Location("555666777");

        Inventory inventory = Inventory.builder()
            .unitNumber(unitNumber)
            .productCode("E003300")
            .location(location)
            .status("AVAILABLE")
            .productDescription("CP2D WB")
            .productFamily("WHOLE_BLOOD")
            .expirationDate(LocalDateTime.now().plusDays(15))
            .statusReason("NORMAL")
            .unsuitableReason(null)
            .expired(false)
            .isImported(true)
            .isBeingIrradiated(false)
            .quarantines(List.of())
            .build();

        // When
        IrradiationInventoryOutput result = mapper.toDomain(inventory);

        // Then
        assertNotNull(result);
        assertEquals("W777725001003", result.unitNumber());
        assertEquals("E003300", result.productCode());
        assertEquals("555666777", result.location());
        assertEquals("AVAILABLE", result.status());
        assertEquals("CP2D WB", result.productDescription());
        assertEquals("WHOLE_BLOOD", result.productFamily());
        assertEquals("NORMAL", result.statusReason());
        assertEquals(false, result.expired());
        assertEquals(true, result.isImported());
        assertNull(result.isBeingIrradiated());
        assertNotNull(result.quarantines());
        assertTrue(result.quarantines().isEmpty());
    }

    @Test
    @DisplayName("Should handle null InventoryQuarantine in toDomain method")
    void toDomain_NullInventoryQuarantine() {
        // Given
        InventoryQuarantine nullQuarantine = null;

        // When
        InventoryQuarantine result = mapper.toDomain(nullQuarantine);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should map Inventory with all boolean flags set to true")
    void toDomain_WithAllBooleanFlagsTrue() {
        // Given
        UnitNumber unitNumber = new UnitNumber("W777725001004");
        Location location = new Location("111222333");

        Inventory inventory = Inventory.builder()
            .unitNumber(unitNumber)
            .productCode("E0869V00")
            .location(location)
            .status("DISCARDED")
            .productDescription("APH FFP")
            .productFamily("FRESH_FROZEN_PLASMA")
            .expirationDate(LocalDateTime.now().minusDays(5))
            .statusReason("EXPIRED")
            .unsuitableReason("POSITIVE_REACTIVE_TEST_RESULTS")
            .expired(true)
            .isImported(true)
            .isBeingIrradiated(true)
            .quarantines(List.of(
                new InventoryQuarantine("EXPIRED", "Past expiration date", true)
            ))
            .build();

        // When
        IrradiationInventoryOutput result = mapper.toDomain(inventory);

        // Then
        assertNotNull(result);
        assertEquals("W777725001004", result.unitNumber());
        assertEquals("E0869V00", result.productCode());
        assertEquals("111222333", result.location());
        assertEquals("DISCARDED", result.status());
        assertEquals("APH FFP", result.productDescription());
        assertEquals("FRESH_FROZEN_PLASMA", result.productFamily());
        assertEquals("EXPIRED", result.statusReason());
        assertEquals("POSITIVE_REACTIVE_TEST_RESULTS", result.unsuitableReason());
        assertEquals(true, result.expired());
        assertEquals(true, result.isImported());
        assertNull(result.isBeingIrradiated());
        assertEquals(1, result.quarantines().size());
        assertEquals("EXPIRED", result.quarantines().get(0).reason());
    }
}
