package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CreateShipmentCommand;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateShipmentCommandTest {

    @Test
    void shouldCreateValidCommand() {
        // Given
        String customerCode = "CUST123";
        String locationCode = "LOC456";
        String productType = "PLASMA";
        String createEmployeeId = "EMP789";
        String transportationReferenceNumber = "TRN001";
        LocalDate shipmentDate = LocalDate.now().plusDays(1);
        BigDecimal cartonTareWeight = new BigDecimal("10.5");

        // When
        CreateShipmentCommand command = new CreateShipmentCommand(
            customerCode,
            locationCode,
            productType,
            createEmployeeId,
            transportationReferenceNumber,
            shipmentDate,
            cartonTareWeight
        );

        // Then
        assertAll(
            () -> assertEquals(customerCode, command.getCustomerCode()),
            () -> assertEquals(locationCode, command.getLocationCode()),
            () -> assertEquals(productType, command.getProductType()),
            () -> assertEquals(createEmployeeId, command.getCreateEmployeeId()),
            () -> assertEquals(transportationReferenceNumber, command.getTransportationReferenceNumber()),
            () -> assertEquals(shipmentDate, command.getShipmentDate()),
            () -> assertEquals(cartonTareWeight, command.getCartonTareWeight())
        );
    }

    @Test
    void shouldThrowExceptionWhenCustomerCodeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateShipmentCommand(
                null,
                "LOC456",
                "PLASMA",
                "EMP789",
                "TRN001",
                LocalDate.now().plusDays(1),
                new BigDecimal("10.5")
            );
        });
    }

    @Test
    void shouldThrowExceptionWhenCustomerCodeIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateShipmentCommand(
                "",
                "LOC456",
                "PLASMA",
                "EMP789",
                "TRN001",
                LocalDate.now().plusDays(1),
                new BigDecimal("10.5")
            );
        });
    }

    @Test
    void shouldThrowExceptionWhenLocationCodeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateShipmentCommand(
                "CUST123",
                null,
                "PLASMA",
                "EMP789",
                "TRN001",
                LocalDate.now().plusDays(1),
                new BigDecimal("10.5")
            );
        });
    }

    @Test
    void shouldThrowExceptionWhenLocationCodeIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateShipmentCommand(
                "CUST123",
                "",
                "PLASMA",
                "EMP789",
                "TRN001",
                LocalDate.now().plusDays(1),
                new BigDecimal("10.5")
            );
        });
    }

    @Test
    void shouldThrowExceptionWhenProductTypeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateShipmentCommand(
                "CUST123",
                "LOC456",
                null,
                "EMP789",
                "TRN001",
                LocalDate.now().plusDays(1),
                new BigDecimal("10.5")
            );
        });
    }

    @Test
    void shouldThrowExceptionWhenProductTypeIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateShipmentCommand(
                "CUST123",
                "LOC456",
                "",
                "EMP789",
                "TRN001",
                LocalDate.now().plusDays(1),
                new BigDecimal("10.5")
            );
        });
    }

    @Test
    void shouldThrowExceptionWhenCreateEmployeeIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateShipmentCommand(
                "CUST123",
                "LOC456",
                "PLASMA",
                null,
                "TRN001",
                LocalDate.now().plusDays(1),
                new BigDecimal("10.5")
            );
        });
    }

    @Test
    void shouldThrowExceptionWhenCreateEmployeeIdIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateShipmentCommand(
                "CUST123",
                "LOC456",
                "PLASMA",
                "",
                "TRN001",
                LocalDate.now().plusDays(1),
                new BigDecimal("10.5")
            );
        });
    }

    @Test
    void shouldThrowExceptionWhenShipmentDateIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateShipmentCommand(
                "CUST123",
                "LOC456",
                "PLASMA",
                "EMP789",
                "TRN001",
                null,
                new BigDecimal("10.5")
            );
        });
    }

    @Test
    void shouldThrowExceptionWhenShipmentDateIsInPast() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateShipmentCommand(
                "CUST123",
                "LOC456",
                "PLASMA",
                "EMP789",
                "TRN001",
                LocalDate.now().minusDays(1),
                new BigDecimal("10.5")
            );
        });
    }
}
