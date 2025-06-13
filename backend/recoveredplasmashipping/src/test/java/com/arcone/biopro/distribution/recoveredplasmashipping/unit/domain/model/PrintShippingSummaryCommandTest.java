package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.PrintShippingSummaryCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrintShippingSummaryCommandTest {

    private static final Long VALID_SHIPMENT_ID = 1L;
    private static final String VALID_EMPLOYEE_ID = "EMP123";
    private static final String VALID_LOCATION_CODE = "LOC456";

    @Test
    @DisplayName("Should create PrintShippingSummaryCommand successfully when all fields are valid")
    void shouldCreateCommandSuccessfully() {
        // Act
        PrintShippingSummaryCommand command = new PrintShippingSummaryCommand(
            VALID_SHIPMENT_ID,
            VALID_EMPLOYEE_ID,
            VALID_LOCATION_CODE
        );

        // Assert
        assertNotNull(command);
        assertEquals(VALID_SHIPMENT_ID, command.getShipmentId());
        assertEquals(VALID_EMPLOYEE_ID, command.getEmployeeId());
        assertEquals(VALID_LOCATION_CODE, command.getLocationCode());
    }

    @Test
    @DisplayName("Should throw exception when shipment id is null")
    void shouldThrowExceptionWhenShipmentIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PrintShippingSummaryCommand(
                null,
                VALID_EMPLOYEE_ID,
                VALID_LOCATION_CODE
            )
        );
        assertEquals("Shipment id is required", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Should throw exception when employee id is null, empty or blank")
    void shouldThrowExceptionWhenEmployeeIdIsInvalid(String employeeId) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PrintShippingSummaryCommand(
                VALID_SHIPMENT_ID,
                employeeId,
                VALID_LOCATION_CODE
            )
        );
        assertEquals("Employee id is required", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Should throw exception when location code is null, empty or blank")
    void shouldThrowExceptionWhenLocationCodeIsInvalid(String locationCode) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PrintShippingSummaryCommand(
                VALID_SHIPMENT_ID,
                VALID_EMPLOYEE_ID,
                locationCode
            )
        );
        assertEquals("Location code is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should create command with minimum valid values")
    void shouldCreateCommandWithMinimumValidValues() {
        // Arrange
        Long minShipmentId = 0L;
        String minEmployeeId = "E";
        String minLocationCode = "L";

        // Act
        PrintShippingSummaryCommand command = new PrintShippingSummaryCommand(
            minShipmentId,
            minEmployeeId,
            minLocationCode
        );

        // Assert
        assertNotNull(command);
        assertEquals(minShipmentId, command.getShipmentId());
        assertEquals(minEmployeeId, command.getEmployeeId());
        assertEquals(minLocationCode, command.getLocationCode());
    }

    @Test
    @DisplayName("Should create command with maximum valid values")
    void shouldCreateCommandWithMaximumValidValues() {
        // Arrange
        Long maxShipmentId = Long.MAX_VALUE;
        String maxEmployeeId = "E" + "1".repeat(50);  // Assuming reasonable max length
        String maxLocationCode = "L" + "1".repeat(50); // Assuming reasonable max length

        // Act
        PrintShippingSummaryCommand command = new PrintShippingSummaryCommand(
            maxShipmentId,
            maxEmployeeId,
            maxLocationCode
        );

        // Assert
        assertNotNull(command);
        assertEquals(maxShipmentId, command.getShipmentId());
        assertEquals(maxEmployeeId, command.getEmployeeId());
        assertEquals(maxLocationCode, command.getLocationCode());
    }

    @Test
    @DisplayName("Should create command with trimmed values")
    void shouldCreateCommandWithTrimmedValues() {
        // Arrange
        String employeeIdWithSpaces = " EMP123 ";
        String locationCodeWithSpaces = " LOC456 ";

        // Act
        PrintShippingSummaryCommand command = new PrintShippingSummaryCommand(
            VALID_SHIPMENT_ID,
            employeeIdWithSpaces,
            locationCodeWithSpaces
        );

        // Assert
        assertNotNull(command);
        assertEquals(VALID_SHIPMENT_ID, command.getShipmentId());
        assertEquals(employeeIdWithSpaces, command.getEmployeeId());
        assertEquals(locationCodeWithSpaces, command.getLocationCode());
    }
}

