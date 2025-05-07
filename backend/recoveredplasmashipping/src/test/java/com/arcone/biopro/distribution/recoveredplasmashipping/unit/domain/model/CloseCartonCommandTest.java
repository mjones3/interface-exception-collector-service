package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CloseCartonCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CloseCartonCommandTest {

    @Test
    @DisplayName("Should create command successfully when all parameters are valid")
    void shouldCreateCommandSuccessfully() {
        // Given
        Long cartonId = 1L;
        String employeeId = "EMP123";
        String locationCode = "LOC456";

        // When/Then
        assertDoesNotThrow(() -> new CloseCartonCommand(cartonId, employeeId, locationCode));
    }

    @Test
    @DisplayName("Should throw exception when cartonId is null")
    void shouldThrowExceptionWhenCartonIdIsNull() {
        // Given
        Long cartonId = null;
        String employeeId = "EMP123";
        String locationCode = "LOC456";

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CloseCartonCommand(cartonId, employeeId, locationCode)
        );
        assertEquals("Carton ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when employeeId is null")
    void shouldThrowExceptionWhenEmployeeIdIsNull() {
        // Given
        Long cartonId = 1L;
        String employeeId = null;
        String locationCode = "LOC456";

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CloseCartonCommand(cartonId, employeeId, locationCode)
        );
        assertEquals("Employee ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when employeeId is blank")
    void shouldThrowExceptionWhenEmployeeIdIsBlank() {
        // Given
        Long cartonId = 1L;
        String employeeId = "   ";
        String locationCode = "LOC456";

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CloseCartonCommand(cartonId, employeeId, locationCode)
        );
        assertEquals("Employee ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when locationCode is null")
    void shouldThrowExceptionWhenLocationCodeIsNull() {
        // Given
        Long cartonId = 1L;
        String employeeId = "EMP123";
        String locationCode = null;

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CloseCartonCommand(cartonId, employeeId, locationCode)
        );
        assertEquals("Location code is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when locationCode is blank")
    void shouldThrowExceptionWhenLocationCodeIsBlank() {
        // Given
        Long cartonId = 1L;
        String employeeId = "EMP123";
        String locationCode = "  ";

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new CloseCartonCommand(cartonId, employeeId, locationCode)
        );
        assertEquals("Location code is required", exception.getMessage());
    }
}
