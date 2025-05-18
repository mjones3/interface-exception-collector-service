package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.LocationProperty;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShippingSummaryShipFrom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShippingSummaryShipFromTest {

    private static final String VALID_ADDRESS_FORMAT = "{address}, {city}, {state} {zipCode} {country}";
    private static final String VALID_BLOOD_CENTER = "Test Blood Center";

    @Test
    @DisplayName("Should create ShippingSummaryShipFrom successfully when all fields are valid")
    void shouldCreateShippingSummaryShipFromSuccessfully() {
        // Arrange
        Location location = mock(Location.class);
        when(location.getAddressLine1()).thenReturn("123 Main St");
        when(location.getCity()).thenReturn("Boston");
        when(location.getState()).thenReturn("MA");
        when(location.getPostalCode()).thenReturn("02108");
        when(location.findProperty("PHONE_NUMBER")).thenReturn(Optional.of(new LocationProperty(1L,"PHONE_NUMBER","123-456-7894")));

        // Act
        ShippingSummaryShipFrom shipFrom = new ShippingSummaryShipFrom(
            VALID_BLOOD_CENTER,
            location,
            VALID_ADDRESS_FORMAT
        );

        // Assert
        assertNotNull(shipFrom);
        // Verify the formatted address
        String expectedAddress = "123 Main St, Boston, MA 02108 USA";
        assertEquals(expectedAddress, shipFrom.getLocationAddress());
        assertEquals("123-456-7894", shipFrom.getPhoneNumber());
    }

    @Test
    @DisplayName("Should throw exception when blood center name is null")
    void shouldThrowExceptionWhenBloodCenterNameIsNull() {
        // Arrange
        Location location = mock(Location.class);
        when(location.getAddressLine1()).thenReturn("123 Main St");
        when(location.getCity()).thenReturn("Boston");
        when(location.getState()).thenReturn("MA");
        when(location.getPostalCode()).thenReturn("02108");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ShippingSummaryShipFrom(null, location, VALID_ADDRESS_FORMAT)
        );
        assertEquals("Blood Center Name is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when blood center name is blank")
    void shouldThrowExceptionWhenBloodCenterNameIsBlank() {
        // Arrange
        Location location = mock(Location.class);
        when(location.getAddressLine1()).thenReturn("123 Main St");
        when(location.getCity()).thenReturn("Boston");
        when(location.getState()).thenReturn("MA");
        when(location.getPostalCode()).thenReturn("02108");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ShippingSummaryShipFrom("  ", location, VALID_ADDRESS_FORMAT)
        );
        assertEquals("Blood Center Name is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when location is null")
    void shouldThrowExceptionWhenLocationIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ShippingSummaryShipFrom(VALID_BLOOD_CENTER, null, VALID_ADDRESS_FORMAT)
        );
        assertEquals("Location is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should format address correctly with all location fields")
    void shouldFormatAddressCorrectlyWithAllLocationFields() {
        // Arrange
        Location location = mock(Location.class);
        when(location.getAddressLine1()).thenReturn("456 Oak Street");
        when(location.getCity()).thenReturn("Chicago");
        when(location.getState()).thenReturn("IL");
        when(location.getPostalCode()).thenReturn("60601");

        // Act
        ShippingSummaryShipFrom shipFrom = new ShippingSummaryShipFrom(
            VALID_BLOOD_CENTER,
            location,
            VALID_ADDRESS_FORMAT
        );

        // Assert
        String expectedAddress = "456 Oak Street, Chicago, IL 60601 USA";
        assertEquals(expectedAddress, shipFrom.getLocationAddress());
    }

    @Test
    @DisplayName("Should handle custom address format")
    void shouldHandleCustomAddressFormat() {
        // Arrange
        Location location = mock(Location.class);
        when(location.getAddressLine1()).thenReturn("789 Pine Ave");
        when(location.getCity()).thenReturn("Miami");
        when(location.getState()).thenReturn("FL");
        when(location.getPostalCode()).thenReturn("33101");

        String customFormat = "{address} - {city} - {state}, {zipCode} ({country})";

        // Act
        ShippingSummaryShipFrom shipFrom = new ShippingSummaryShipFrom(
            VALID_BLOOD_CENTER,
            location,
            customFormat
        );

        // Assert
        String expectedAddress = "789 Pine Ave - Miami - FL, 33101 (USA)";
        assertEquals(expectedAddress, shipFrom.getLocationAddress());
    }
}

