package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.PackingSlipProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
class PackingSlipProductTest {

    @Test
    @DisplayName("Should create PackingSlipProduct successfully with valid parameters")
    void shouldCreatePackingSlipProductSuccessfully() {
        // Given
        String unitNumber = "UNIT001";
        ZonedDateTime collectionDate = ZonedDateTime.now(ZoneId.of("UTC"));
        Integer volume = 100;
        String dateFormat = "yyyy-MM-dd HH:mm:ss";
        String timeZone = "UTC";

        // When
        PackingSlipProduct product = new PackingSlipProduct(unitNumber, collectionDate, volume, dateFormat, timeZone);

        // Then
        assertNotNull(product);
        assertEquals(unitNumber, product.getUnitNumber());
        assertEquals(volume, product.getVolume());
        assertEquals(dateFormat, product.getDateFormat());
        assertEquals(timeZone, product.getTimeZone());
    }

    @Test
    @DisplayName("Should format collection date correctly")
    void shouldFormatCollectionDateCorrectly() {
        // Given
        ZonedDateTime collectionDate = ZonedDateTime.of(2023, 12, 25, 10, 30, 0, 0, ZoneId.of("UTC"));
        PackingSlipProduct product = new PackingSlipProduct(
            "UNIT001",
            collectionDate,
            100,
            "yyyy-MM-dd HH:mm:ss",
            "UTC"
        );

        // When
        String formattedDate = product.getCollectionDateFormatted();

        // Then
        assertEquals("2023-12-25 10:30:00", formattedDate);
    }

    @Test
    @DisplayName("Should throw exception for null unit number")
    void shouldThrowExceptionForNullUnitNumber() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PackingSlipProduct(
                null,
                ZonedDateTime.now(),
                100,
                "yyyy-MM-dd",
                "UTC"
            )
        );
        assertEquals("Unit Number is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for blank unit number")
    void shouldThrowExceptionForBlankUnitNumber() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PackingSlipProduct(
                "  ",
                ZonedDateTime.now(),
                100,
                "yyyy-MM-dd",
                "UTC"
            )
        );
        assertEquals("Unit Number is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for null collection date")
    void shouldThrowExceptionForNullCollectionDate() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PackingSlipProduct(
                "UNIT001",
                null,
                100,
                "yyyy-MM-dd",
                "UTC"
            )
        );
        assertEquals("Collection Date is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for invalid volume")
    void shouldThrowExceptionForInvalidVolume() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PackingSlipProduct(
                "UNIT001",
                ZonedDateTime.now(),
                0,
                "yyyy-MM-dd",
                "UTC"
            )
        );
        assertEquals("Volume is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for invalid date format")
    void shouldThrowExceptionForInvalidDateFormat() {
        PackingSlipProduct product = new PackingSlipProduct(
            "UNIT001",
            ZonedDateTime.now(),
            100,
            "invalid-format",
            "UTC"
        );

        assertThrows(IllegalArgumentException.class, product::getCollectionDateFormatted);
    }

    @Test
    @DisplayName("Should throw exception for null date format")
    void shouldThrowExceptionForNullDateFormat() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PackingSlipProduct(
                "UNIT001",
                ZonedDateTime.now(),
                100,
                null,
                "UTC"
            )
        );
        assertEquals("Date Format is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for null timezone")
    void shouldThrowExceptionForNullTimezone() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PackingSlipProduct(
                "UNIT001",
                ZonedDateTime.now(),
                100,
                "yyyy-MM-dd",
                null
            )
        );
        assertEquals("Timezone is required", exception.getMessage());
    }
}

