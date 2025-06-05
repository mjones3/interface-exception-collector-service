package com.arcone.biopro.distribution.eventbridge.unit.domain.model;

import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedCartonItemOutbound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecoveredPlasmaShipmentClosedCartonItemOutboundTest {

    @Test
    @DisplayName("Should create valid object with all required fields")
    void shouldCreateValidObject() {
        // Given
        String unitNumber = "UN123";
        String productCode = "PC456";
        String collectionFacility = "CF789";
        ZonedDateTime collectionDate = ZonedDateTime.parse("2025-05-30T17:06:35.691463Z");
        Integer productVolume = 255;
        String bloodType = "A+";
        String collectionTimeZone = "America/New_York";

        // When
        RecoveredPlasmaShipmentClosedCartonItemOutbound outbound =
            new RecoveredPlasmaShipmentClosedCartonItemOutbound(
                unitNumber,
                productCode,
                collectionFacility,
                collectionDate,
                productVolume,
                bloodType,
                collectionTimeZone
            );

        // Then
        assertNotNull(outbound);
        assertEquals(unitNumber, outbound.getUnitNumber());
        assertEquals(productCode, outbound.getProductCode());
        assertEquals(collectionFacility, outbound.getCollectionFacility());
        assertEquals(collectionDate, outbound.getCollectionDate());
        assertEquals("0.255", outbound.getProductVolume().toString());
        assertEquals(bloodType, outbound.getBloodType());
        assertEquals("13:06", outbound.getDrawBeginTime());
        assertEquals("2025-06-30 13:06", outbound.getCollectionDateFormatted());
        assertEquals("America/New_York", outbound.getCollectionTimeZone());

    }

    @Test
    @DisplayName("Should throw IllegalStateException when unitNumber is null")
    void shouldThrowExceptionWhenUnitNumberIsNull() {
        // Given
        String unitNumber = null;
        String productCode = "PC456";
        String collectionFacility = "CF789";
        ZonedDateTime collectionDate = ZonedDateTime.now(ZoneId.of("UTC"));
        Integer productVolume = 100;
        String bloodType = "A+";
        String collectionTimeZone = "America/New_York";

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new RecoveredPlasmaShipmentClosedCartonItemOutbound(
                unitNumber,
                productCode,
                collectionFacility,
                collectionDate,
                productVolume,
                bloodType,
                collectionTimeZone
            );
        });
        assertEquals("Unit number is null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when productCode is null")
    void shouldThrowExceptionWhenProductCodeIsNull() {
        // Given
        String unitNumber = "UN123";
        String productCode = null;
        String collectionFacility = "CF789";
        ZonedDateTime collectionDate = ZonedDateTime.now(ZoneId.of("UTC"));
        Integer productVolume = 100;
        String bloodType = "A+";
        String collectionTimeZone = "America/New_York";

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new RecoveredPlasmaShipmentClosedCartonItemOutbound(
                unitNumber,
                productCode,
                collectionFacility,
                collectionDate,
                productVolume,
                bloodType,
                collectionTimeZone
            );
        });
        assertEquals("Product code is null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when collectionTimeZone is null")
    void shouldThrowExceptionWhenCollectionTimeZoneIsNull() {
        // Given
        String unitNumber = "UN123";
        String productCode = "PC456";
        String collectionFacility = "CF789";
        ZonedDateTime collectionDate = ZonedDateTime.now(ZoneId.of("UTC"));
        Integer productVolume = 100;
        String bloodType = "A+";
        String collectionTimeZone = null;

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new RecoveredPlasmaShipmentClosedCartonItemOutbound(
                unitNumber,
                productCode,
                collectionFacility,
                collectionDate,
                productVolume,
                bloodType,
                collectionTimeZone
            );
        });
        assertEquals("Collection Timezone is null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when collectionDate is null")
    void shouldThrowExceptionWhenCollectionDateIsNull() {
        // Given
        String unitNumber = "UN123";
        String productCode = "PC456";
        String collectionFacility = "CF789";
        ZonedDateTime collectionDate = null;
        Integer productVolume = 100;
        String bloodType = "A+";
        String collectionTimeZone = "America/New_York";

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new RecoveredPlasmaShipmentClosedCartonItemOutbound(
                unitNumber,
                productCode,
                collectionFacility,
                collectionDate,
                productVolume,
                bloodType,
                collectionTimeZone
            );
        });
        assertEquals("Collection Date is null", exception.getMessage());
    }

    @Test
    @DisplayName("Should format date and time correctly")
    void shouldFormatDateTimeCorrectly() {
        // Given
        String unitNumber = "UN123";
        String productCode = "PC456";
        String collectionFacility = "CF789";
        ZonedDateTime collectionDate = ZonedDateTime.of(2024, 1, 1, 14, 30, 0, 0, ZoneId.of("UTC"));
        Integer productVolume = 100;
        String bloodType = "A+";
        String collectionTimeZone = "America/New_York";

        // When
        RecoveredPlasmaShipmentClosedCartonItemOutbound outbound =
            new RecoveredPlasmaShipmentClosedCartonItemOutbound(
                unitNumber,
                productCode,
                collectionFacility,
                collectionDate,
                productVolume,
                bloodType,
                collectionTimeZone
            );

        // Then
        assertNotNull(outbound);
        assertEquals("09:30", outbound.getDrawBeginTime());
        assertEquals("2024-30-01 09:30", outbound.getCollectionDateFormatted());
    }
}

