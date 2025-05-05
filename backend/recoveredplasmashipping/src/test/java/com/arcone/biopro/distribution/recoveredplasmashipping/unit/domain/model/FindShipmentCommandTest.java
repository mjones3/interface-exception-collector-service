package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.FindShipmentCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FindShipmentCommandTest {

    @Test
    void shouldCreateValidCommand() {
        // Given
        Long shipmentId = 1L;
        String locationCode = "LOC123";
        String employeeId = "EMP456";

        // When/Then
        FindShipmentCommand command = new FindShipmentCommand(shipmentId, locationCode, employeeId);

        assertNotNull(command);
        assertEquals(shipmentId, command.getShipmentId());
        assertEquals(locationCode, command.getLocationCode());
        assertEquals(employeeId, command.getEmployeeId());
    }

    @Test
    void shouldThrowExceptionWhenShipmentIdIsNull() {
        // Given
        Long shipmentId = null;
        String locationCode = "LOC123";
        String employeeId = "EMP456";

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new FindShipmentCommand(shipmentId, locationCode, employeeId)
        );
        assertEquals("Shipment ID is required", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void shouldThrowExceptionWhenLocationCodeIsNullOrBlank(String locationCode) {
        // Given
        Long shipmentId = 1L;
        String employeeId = "EMP456";

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new FindShipmentCommand(shipmentId, locationCode, employeeId)
        );
        assertEquals("Location code is required", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void shouldThrowExceptionWhenEmployeeIdIsNullOrBlank(String employeeId) {
        // Given
        Long shipmentId = 1L;
        String locationCode = "LOC123";

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new FindShipmentCommand(shipmentId, locationCode, employeeId)
        );
        assertEquals("Employee ID is required", exception.getMessage());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        // Given
        FindShipmentCommand command1 = new FindShipmentCommand(1L, "LOC123", "EMP456");
        FindShipmentCommand command2 = new FindShipmentCommand(1L, "LOC123", "EMP456");
        FindShipmentCommand command3 = new FindShipmentCommand(2L, "LOC123", "EMP456");

        // Then
        assertEquals(command1, command2);
        assertNotEquals(command1, command3);
        assertEquals(command1.hashCode(), command2.hashCode());
        assertNotEquals(command1.hashCode(), command3.hashCode());
    }

    @Test
    void shouldImplementToString() {
        // Given
        FindShipmentCommand command = new FindShipmentCommand(1L, "LOC123", "EMP456");

        // When
        String toString = command.toString();

        // Then
        assertTrue(toString.contains("shipmentId=1"));
        assertTrue(toString.contains("locationCode=LOC123"));
        assertTrue(toString.contains("employeeId=EMP456"));
    }
}

