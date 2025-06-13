package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CloseShipmentCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CloseShipmentCommandTest {

    @Test
    @DisplayName("Should create valid command when all parameters are correct")
    void shouldCreateValidCommand() {
        // Given
        Long shipmentId = 1L;
        String employeeId = "EMP123";
        String locationCode = "LOC456";
        LocalDate shipDate = LocalDate.now().plusDays(1);

        // When/Then
        assertDoesNotThrow(() -> new CloseShipmentCommand(shipmentId, employeeId, locationCode, shipDate));
    }

    @Test
    @DisplayName("Should throw exception when shipmentId is null")
    void shouldThrowExceptionWhenShipmentIdIsNull() {
        // Given
        String employeeId = "EMP123";
        String locationCode = "LOC456";
        LocalDate shipDate = LocalDate.now().plusDays(1);

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CloseShipmentCommand(null, employeeId, locationCode, shipDate)
        );
        assertEquals("Shipment ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when employeeId is null")
    void shouldThrowExceptionWhenEmployeeIdIsNull() {
        // Given
        Long shipmentId = 1L;
        String locationCode = "LOC456";
        LocalDate shipDate = LocalDate.now().plusDays(1);

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CloseShipmentCommand(shipmentId, null, locationCode, shipDate)
        );
        assertEquals("Employee ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when employeeId is blank")
    void shouldThrowExceptionWhenEmployeeIdIsBlank() {
        // Given
        Long shipmentId = 1L;
        String locationCode = "LOC456";
        LocalDate shipDate = LocalDate.now().plusDays(1);

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CloseShipmentCommand(shipmentId, "  ", locationCode, shipDate)
        );
        assertEquals("Employee ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when locationCode is null")
    void shouldThrowExceptionWhenLocationCodeIsNull() {
        // Given
        Long shipmentId = 1L;
        String employeeId = "EMP123";
        LocalDate shipDate = LocalDate.now().plusDays(1);

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CloseShipmentCommand(shipmentId, employeeId, null, shipDate)
        );
        assertEquals("Location code is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when locationCode is blank")
    void shouldThrowExceptionWhenLocationCodeIsBlank() {
        // Given
        Long shipmentId = 1L;
        String employeeId = "EMP123";
        LocalDate shipDate = LocalDate.now().plusDays(1);

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CloseShipmentCommand(shipmentId, employeeId, "  ", shipDate)
        );
        assertEquals("Location code is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when shipDate is null")
    void shouldThrowExceptionWhenShipDateIsNull() {
        // Given
        Long shipmentId = 1L;
        String employeeId = "EMP123";
        String locationCode = "LOC456";

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CloseShipmentCommand(shipmentId, employeeId, locationCode, null)
        );
        assertEquals("Ship date is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when shipDate is in the past")
    void shouldThrowExceptionWhenShipDateIsInPast() {
        // Given
        Long shipmentId = 1L;
        String employeeId = "EMP123";
        String locationCode = "LOC456";
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CloseShipmentCommand(shipmentId, employeeId, locationCode, pastDate)
        );
        assertEquals("Ship date cannot be in the past", exception.getMessage());
    }
}
