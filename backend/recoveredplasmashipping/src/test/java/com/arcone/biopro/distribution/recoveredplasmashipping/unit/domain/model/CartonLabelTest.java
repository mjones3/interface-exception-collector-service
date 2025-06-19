package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonLabel;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShipmentCustomer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartonLabelTest {

    private ShipmentCustomer mockShipmentCustomer;
    private Location mockLocation;
    private ZonedDateTime validDate;

    @BeforeEach
    void setUp() {
        // Setup mock ShipmentCustomer
        mockShipmentCustomer = Mockito.mock(ShipmentCustomer.class);
        // Assuming ShipmentCustomer has these setter methods
        Mockito.when(mockShipmentCustomer.getCustomerCode()).thenReturn("CUST001");
        Mockito.when(mockShipmentCustomer.getCustomerName()).thenReturn("Test Customer");
        Mockito.when(mockShipmentCustomer.getCustomerAddressLine1()).thenReturn("123 Test St");
        Mockito.when(mockShipmentCustomer.getCustomerCity()).thenReturn("Test City");
        Mockito.when(mockShipmentCustomer.getCustomerState()).thenReturn("TS");
        Mockito.when(mockShipmentCustomer.getCustomerPostalCode()).thenReturn("12345");
        Mockito.when(mockShipmentCustomer.getCustomerCountry()).thenReturn("USA");

        // Setup mock Location
        mockLocation = Mockito.mock(Location.class);
        Mockito.when(mockLocation.getAddressLine1()).thenReturn("456 Center St");
        Mockito.when(mockLocation.getCity()).thenReturn("Center City");
        Mockito.when(mockLocation.getState()).thenReturn("CS");
        Mockito.when(mockLocation.getPostalCode()).thenReturn("67890");

        // Setup valid date
        validDate = ZonedDateTime.now(ZoneId.systemDefault());
    }

    @Test
    @DisplayName("Should create valid CartonLabel")
    void testValidCartonLabelCreation() {
        CartonLabel cartonLabel = new CartonLabel(
            mockShipmentCustomer,
            "CARTON123",
            1,
            validDate,
            "Test Blood Center",
            mockLocation,
            "TRANS123",
            "SHIP123",
            "PROD123",false,false
        );

        assertNotNull(cartonLabel);
    }

    @Test
    @DisplayName("Should throw exception when shipmentCustomer is null")
    void testNullShipmentCustomer() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CartonLabel(
                null,
                "CARTON123",
                1,
                validDate,
                "Test Blood Center",
                mockLocation,
                "TRANS123",
                "SHIP123",
                "PROD123",false,false
            )
        );
        assertEquals("Shipment Customer is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should correctly map all values")
    void testToMap() {
        CartonLabel cartonLabel = new CartonLabel(
            mockShipmentCustomer,
            "CARTON123",
            1,
            validDate,
            "Test Blood Center",
            mockLocation,
            "TRANS123",
            "SHIP123",
            "PROD123",false,false
        );

        Map<String, Object> result = cartonLabel.toMap();

        // Test upper left quadrant values
        assertEquals("CUST001", result.get("CUSTOMER_CODE"));
        assertEquals("Test Customer", result.get("CUSTOMER_NAME"));
        assertEquals("123 Test St", result.get("CUSTOMER_ADDRESS"));
        assertEquals("Test City", result.get("CUSTOMER_CITY"));
        assertEquals("TS", result.get("CUSTOMER_STATE"));
        assertEquals("12345", result.get("CUSTOMER_ZIP_CODE"));
        assertEquals("USA", result.get("CUSTOMER_COUNTRY"));
        assertEquals("CARTON123", result.get("CARTON_NUMBER"));
        assertNotNull(result.get("CLOSE_DATE")); // Date format check

        // Test upper right quadrant values
        assertEquals("Test Blood Center", result.get("BLOOD_CENTER_NAME"));
        assertEquals("456 Center St", result.get("ADDRESS_LINE"));
        assertEquals("Center City", result.get("CITY"));
        assertEquals("CS", result.get("STATE"));
        assertEquals("67890", result.get("ZIPCODE"));
        assertEquals("USA", result.get("COUNTRY"));
        assertEquals("TRANS123", result.get("TRANSPORTATION_NUMBER"));
        assertNull(result.get("DISPLAY_TRANSPORTATION_NUMBER"));

        // Test lower quadrant values
        assertEquals("PROD123", result.get("PRODUCT_CODE"));
        assertEquals("Carton Sequence in Shipment 1", result.get("CARTON_SEQUENCE"));
        assertEquals("SHIP123", result.get("SHIPMENT_NUMBER"));
    }

    @Test
    @DisplayName("Should Format Carton Sequence correctly")
    void testToMapWhenDisplayTotalCartons() {
        CartonLabel cartonLabel = new CartonLabel(
            mockShipmentCustomer,
            "CARTON123",
            1,
            validDate,
            "Test Blood Center",
            mockLocation,
            "TRANS123",
            "SHIP123",
            "PROD123",true,true
        );

        Map<String, Object> result = cartonLabel.toMap();

        // Test upper left quadrant values
        assertEquals("CUST001", result.get("CUSTOMER_CODE"));
        assertEquals("Test Customer", result.get("CUSTOMER_NAME"));
        assertEquals("123 Test St", result.get("CUSTOMER_ADDRESS"));
        assertEquals("Test City", result.get("CUSTOMER_CITY"));
        assertEquals("TS", result.get("CUSTOMER_STATE"));
        assertEquals("12345", result.get("CUSTOMER_ZIP_CODE"));
        assertEquals("USA", result.get("CUSTOMER_COUNTRY"));
        assertEquals("CARTON123", result.get("CARTON_NUMBER"));
        assertNotNull(result.get("CLOSE_DATE")); // Date format check

        // Test upper right quadrant values
        assertEquals("Test Blood Center", result.get("BLOOD_CENTER_NAME"));
        assertEquals("456 Center St", result.get("ADDRESS_LINE"));
        assertEquals("Center City", result.get("CITY"));
        assertEquals("CS", result.get("STATE"));
        assertEquals("67890", result.get("ZIPCODE"));
        assertEquals("USA", result.get("COUNTRY"));
        assertEquals("TRANS123", result.get("TRANSPORTATION_NUMBER"));
        assertEquals("Y", result.get("DISPLAY_TRANSPORTATION_NUMBER"));

        // Test lower quadrant values
        assertEquals("PROD123", result.get("PRODUCT_CODE"));
        assertEquals("Carton Sequence in Shipment 1 of ____", result.get("CARTON_SEQUENCE"));
        assertEquals("SHIP123", result.get("SHIPMENT_NUMBER"));
    }

    @Test
    @DisplayName("Should throw exception for blank cartonNumber")
    void testBlankCartonNumber() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CartonLabel(
                mockShipmentCustomer,
                "",
                1,
                validDate,
                "Test Blood Center",
                mockLocation,
                "TRANS123",
                "SHIP123",
                "PROD123",false,false
            )
        );
        assertEquals("Carton Number is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for null cartonSequenceNumber")
    void testNullCartonSequenceNumber() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CartonLabel(
                mockShipmentCustomer,
                "CARTON123",
                null,
                validDate,
                "Test Blood Center",
                mockLocation,
                "TRANS123",
                "SHIP123",
                "PROD123",false,false
            )
        );
        assertEquals("Carton Sequence Number is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should format date correctly in toMap")
    void testDateFormatting() {
        CartonLabel cartonLabel = new CartonLabel(
            mockShipmentCustomer,
            "CARTON123",
            1,
            validDate,
            "Test Blood Center",
            mockLocation,
            "TRANS123",
            "SHIP123",
            "PROD123",false,false
        );

        Map<String, Object> result = cartonLabel.toMap();
        String formattedDate = (String) result.get("CLOSE_DATE");

        // Verify date format matches MM/dd/yyyy
        assertTrue(formattedDate.matches("\\d{2}/\\d{2}/\\d{4}"));
    }
}

