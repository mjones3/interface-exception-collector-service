package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShipmentCustomer;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShipTo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ShipToTest {

    private ShipmentCustomer mockCustomer;
    private String validAddressFormat;

    @BeforeEach
    void setUp() {
        // Setup mock customer with test data
        mockCustomer = Mockito.mock(ShipmentCustomer.class);
        Mockito.when(mockCustomer.getCustomerCode()).thenReturn("Test Customer Code");
        Mockito.when(mockCustomer.getCustomerName()).thenReturn("Test Customer");
        Mockito.when(mockCustomer.getCustomerAddressLine1()).thenReturn("123 Test Street");
        Mockito.when(mockCustomer.getCustomerCity()).thenReturn("Test City");
        Mockito.when(mockCustomer.getCustomerState()).thenReturn("TS");
        Mockito.when(mockCustomer.getCustomerPostalCode()).thenReturn("12345");
        Mockito.when(mockCustomer.getCustomerCountry()).thenReturn("USA");

        validAddressFormat = "{address}, {city}, {state} {zipCode}, {country}";
    }

    @Test
    @DisplayName("Should create PackingSlipShipTo successfully with valid parameters")
    void shouldCreatePackingSlipShipToSuccessfully() {
        // When
        ShipTo shipTo = new ShipTo(mockCustomer, validAddressFormat);

        // Then
        Assertions.assertNotNull(shipTo);
        Assertions.assertEquals(mockCustomer, shipTo.getShipmentCustomer());
        Assertions.assertEquals(validAddressFormat, shipTo.getAddressFormat());
    }

    @Test
    @DisplayName("Should format address correctly")
    void shouldFormatAddressCorrectly() {
        // Given
        ShipTo shipTo = new ShipTo(mockCustomer, validAddressFormat);

        // When
        String formattedAddress = shipTo.getFormattedAddress();

        // Then
        String expectedAddress = "123 Test Street, Test City, TS 12345, USA";
        Assertions.assertEquals(expectedAddress, formattedAddress);
    }

    @Test
    @DisplayName("Should return correct customer name")
    void shouldReturnCorrectCustomerName() {
        // Given
        ShipTo shipTo = new ShipTo(mockCustomer, validAddressFormat);

        // When
        String customerName = shipTo.getCustomerName();

        // Then
        Assertions.assertEquals("Test Customer", customerName);
    }

    @Test
    @DisplayName("Should throw exception for null shipment customer")
    void shouldThrowExceptionForNullShipmentCustomer() {
        // When/Then
        IllegalArgumentException exception = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new ShipTo(null, validAddressFormat)
        );
        Assertions.assertEquals("Ship Customer is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for null address format")
    void shouldThrowExceptionForNullAddressFormat() {
        // When/Then
        IllegalArgumentException exception = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new ShipTo(mockCustomer, null)
        );
        Assertions.assertEquals("Address Format is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for blank address format")
    void shouldThrowExceptionForBlankAddressFormat() {
        // When/Then
        IllegalArgumentException exception = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new ShipTo(mockCustomer, "   ")
        );
        Assertions.assertEquals("Address Format is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle different address format patterns")
    void shouldHandleDifferentAddressFormatPatterns() {
        // Given
        String customFormat = "{address} {city}, {state} {zipCode} {country}";
        ShipTo shipTo = new ShipTo(mockCustomer, customFormat);

        // When
        String formattedAddress = shipTo.getFormattedAddress();

        // Then
        String expectedAddress = "123 Test Street Test City, TS 12345 USA";
        Assertions.assertEquals(expectedAddress, formattedAddress);
    }

    @Test
    @DisplayName("Should handle address format with missing placeholders")
    void shouldHandleAddressFormatWithMissingPlaceholders() {
        // Given
        String incompleteFormat = "{address}, {city}";
        ShipTo shipTo = new ShipTo(mockCustomer, incompleteFormat);

        // When
        String formattedAddress = shipTo.getFormattedAddress();

        // Then
        String expectedAddress = "123 Test Street, Test City";
        Assertions.assertEquals(expectedAddress, formattedAddress);
    }
}

