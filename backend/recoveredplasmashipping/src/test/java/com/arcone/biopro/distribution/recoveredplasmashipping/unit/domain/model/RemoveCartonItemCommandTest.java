package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RemoveCartonItemCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RemoveCartonItemCommandTest {

    @Test
    void shouldCreateValidCommand() {
        // Arrange
        Long cartonId = 1L;
        String employeeId = "EMP123";
        List<Long> cartonItemIds = Arrays.asList(1L, 2L);

        // Act & Assert
        RemoveCartonItemCommand command = new RemoveCartonItemCommand(cartonId, employeeId, cartonItemIds);
        assertNotNull(command);
    }

    @Test
    void shouldThrowExceptionWhenCartonIdIsNull() {
        // Arrange
        Long cartonId = null;
        String employeeId = "EMP123";
        List<Long> cartonItemIds = Arrays.asList(1L, 2L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new RemoveCartonItemCommand(cartonId, employeeId, cartonItemIds));
        assertEquals("Carton is required", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void shouldThrowExceptionWhenEmployeeIdIsNullOrBlank(String employeeId) {
        // Arrange
        Long cartonId = 1L;
        List<Long> cartonItemIds = Arrays.asList(1L, 2L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new RemoveCartonItemCommand(cartonId, employeeId, cartonItemIds));
        assertEquals("Employee ID is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCartonItemIdsIsNull() {
        // Arrange
        Long cartonId = 1L;
        String employeeId = "EMP123";
        List<Long> cartonItemIds = null;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new RemoveCartonItemCommand(cartonId, employeeId, cartonItemIds));
        assertEquals("Carton items are required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCartonItemIdsIsEmpty() {
        // Arrange
        Long cartonId = 1L;
        String employeeId = "EMP123";
        List<Long> cartonItemIds = Collections.emptyList();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new RemoveCartonItemCommand(cartonId, employeeId, cartonItemIds));
        assertEquals("Carton items are required", exception.getMessage());
    }

    @Test
    void shouldAllowMultipleCartonItems() {
        // Arrange
        Long cartonId = 1L;
        String employeeId = "EMP123";
        List<Long> cartonItemIds = Arrays.asList(1L, 2L, 3L);

        // Act & Assert
        RemoveCartonItemCommand command = new RemoveCartonItemCommand(cartonId, employeeId, cartonItemIds);
        assertNotNull(command);
    }
}
