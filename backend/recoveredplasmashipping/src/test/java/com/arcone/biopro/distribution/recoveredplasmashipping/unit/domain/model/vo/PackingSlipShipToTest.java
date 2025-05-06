package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShipmentCustomer;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.PackingSlipShipTo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PackingSlipShipToTest {

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
        PackingSlipShipTo shipTo = new PackingSlipShipTo(mockCustomer, validAddressFormat);

        // Then
        Assertions.assertNotNull(shipTo);
        Assertions.assertEquals(mockCustomer, shipTo.getShipmentCustomer());
        Assertions.assertEquals(validAddressFormat, shipTo.getAddressFormat());
    }

    @Test
    @DisplayName("Should format address correctly")
    void shouldFormatAddressCorrectly() {
        // Given
        PackingSlipShipTo shipTo = new PackingSlipShipTo(mockCustomer, validAddressFormat);

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
        PackingSlipShipTo shipTo = new PackingSlipShipTo(mockCustomer, validAddressFormat);

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
            () -> new PackingSlipShipTo(null, validAddressFormat)
        );
        Assertions.assertEquals("Ship Customer is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for null address format")
    void shouldThrowExceptionForNullAddressFormat() {
        // When/Then
        IllegalArgumentException exception = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new PackingSlipShipTo(mockCustomer, null)
        );
        Assertions.assertEquals("Address Format is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for blank address format")
    void shouldThrowExceptionForBlankAddressFormat() {
        // When/Then
        IllegalArgumentException exception = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new PackingSlipShipTo(mockCustomer, "   ")
        );
        Assertions.assertEquals("Address Format is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle different address format patterns")
    void shouldHandleDifferentAddressFormatPatterns() {
        // Given
        String customFormat = "{address} {city}, {state} {zipCode} {country}";
        PackingSlipShipTo shipTo = new PackingSlipShipTo(mockCustomer, customFormat);

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
        PackingSlipShipTo shipTo = new PackingSlipShipTo(mockCustomer, incompleteFormat);

        // When
        String formattedAddress = shipTo.getFormattedAddress();

        // Then
        String expectedAddress = "123 Test Street, Test City";
        Assertions.assertEquals(expectedAddress, formattedAddress);
    }
}

