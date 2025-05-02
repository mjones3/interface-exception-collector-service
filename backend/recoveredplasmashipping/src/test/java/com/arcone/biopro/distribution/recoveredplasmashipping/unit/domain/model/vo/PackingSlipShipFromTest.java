package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.LocationProperty;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.PackingSlipShipFrom;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PackingSlipShipFromTest {

    @Test
    void shouldCreateValidPackingSlipShipFrom() {
        // Given
        String bloodCenterName = "Test Blood Center";
        String locationAddress = "123 Main St";
        String licenseNumber = "LIC123";
        String locationCity = "Test City";
        String locationState = "TS";
        String locationZipcode = "12345";

        String addressFormat = "{address}, {city}, {state} {zipCode}, {country}";

        var location = Mockito.mock(Location.class);
        Mockito.when(location.getAddressLine1()).thenReturn(locationAddress);
        Mockito.when(location.getCity()).thenReturn(locationCity);
        Mockito.when(location.getState()).thenReturn(locationState);
        Mockito.when(location.getPostalCode()).thenReturn(locationZipcode);

        LocationProperty locationProperty = Mockito.mock(LocationProperty.class);
        Mockito.when(locationProperty.getPropertyValue()).thenReturn(licenseNumber);

        Mockito.when(location.findProperty(Mockito.eq("LICENSE_NUMBER"))).thenReturn(Optional.of(locationProperty));


        // When
        PackingSlipShipFrom shipFrom = new PackingSlipShipFrom(
            bloodCenterName,
            location,
            addressFormat
        );

        // Then
        assertNotNull(shipFrom);
        assertEquals(bloodCenterName, shipFrom.getBloodCenterName());
        assertEquals(licenseNumber, shipFrom.getLicenseNumber());
        assertEquals(addressFormat, shipFrom.getAddressFormat());

    }

    @Test
    void shouldThrowExceptionWhenBloodCenterNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PackingSlipShipFrom(
                null,
                Mockito.mock(Location.class),
                "{address}, {city}, {state} {zipcode}, {country}"
            );
        });
    }

    @Test
    void shouldThrowExceptionWhenBloodCenterNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PackingSlipShipFrom(
                "   ",
                Mockito.mock(Location.class),
                "{address}, {city}, {state} {zipcode}, {country}"
            );
        });
    }

    @Test
    void shouldThrowExceptionWhenLocationIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PackingSlipShipFrom(
                "Test Blood Center",
                null,
                "{address}, {city}, {state} {zipcode}, {country}"
            );
        });
    }

    @Test
    void shouldFormatPackingSlipShipFromAddress() {
        // Given
        String bloodCenterName = "Test Blood Center";
        String locationAddress = "123 Main St";
        String licenseNumber = "LIC123";
        String locationCity = "Test City";
        String locationState = "TS";
        String locationZipcode = "12345";
        String addressFormat = "{address}, {city}, {state} {zipCode}, {country}";

        var location = Mockito.mock(Location.class);
        Mockito.when(location.getAddressLine1()).thenReturn(locationAddress);
        Mockito.when(location.getCity()).thenReturn(locationCity);
        Mockito.when(location.getState()).thenReturn(locationState);
        Mockito.when(location.getPostalCode()).thenReturn(locationZipcode);

        // When
        PackingSlipShipFrom shipFrom = new PackingSlipShipFrom(
            bloodCenterName,
            location,
            addressFormat
        );

        // Then
        assertNotNull(shipFrom);
        assertEquals(bloodCenterName, shipFrom.getBloodCenterName());
        assertEquals(addressFormat, shipFrom.getAddressFormat());
        assertEquals("123 Main St, Test City, TS 12345, USA", shipFrom.getLocationAddressFormatted());
    }

}

