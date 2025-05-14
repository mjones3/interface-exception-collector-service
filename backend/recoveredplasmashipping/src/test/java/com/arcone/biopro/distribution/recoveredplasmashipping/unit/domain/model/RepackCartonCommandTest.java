package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RepackCartonCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepackCartonCommandTest {

    @Test
    void testValidRepackCartonCommand() {
        // Given
        Long cartonId = 1L;
        String employeeId = "EMP123";
        String locationCode = "LOC456";
        String reasonComments = "Valid reason for repacking";

        // When/Then
        RepackCartonCommand command = new RepackCartonCommand(cartonId, employeeId, locationCode, reasonComments);

        // Assert all fields are set correctly
        assertEquals(cartonId, command.getCartonId());
        assertEquals(employeeId, command.getEmployeeId());
        assertEquals(locationCode, command.getLocationCode());
        assertEquals(reasonComments, command.getReasonComments());
    }

    @Test
    void testNullCartonId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new RepackCartonCommand(null, "EMP123", "LOC456", "Valid reason");
        });
        assertEquals("Carton ID is required", exception.getMessage());
    }

    @Test
    void testNullEmployeeId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new RepackCartonCommand(1L, null, "LOC456", "Valid reason");
        });
        assertEquals("Employee ID is required", exception.getMessage());
    }

    @Test
    void testBlankEmployeeId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new RepackCartonCommand(1L, "  ", "LOC456", "Valid reason");
        });
        assertEquals("Employee ID is required", exception.getMessage());
    }

    @Test
    void testNullLocationCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new RepackCartonCommand(1L, "EMP123", null, "Valid reason");
        });
        assertEquals("Location code is required", exception.getMessage());
    }

    @Test
    void testBlankLocationCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new RepackCartonCommand(1L, "EMP123", "", "Valid reason");
        });
        assertEquals("Location code is required", exception.getMessage());
    }

    @Test
    void testNullReasonComments() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new RepackCartonCommand(1L, "EMP123", "LOC456", null);
        });
        assertEquals("Reason comments is required", exception.getMessage());
    }

    @Test
    void testBlankReasonComments() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new RepackCartonCommand(1L, "EMP123", "LOC456", "   ");
        });
        assertEquals("Reason comments is required", exception.getMessage());
    }

    @Test
    void testReasonCommentsExceedsMaxLength() {
        // Create a string longer than MAX_COMMENTS (250 characters)
        String longComment = "a".repeat(251);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new RepackCartonCommand(1L, "EMP123", "LOC456", longComment);
        });
        assertEquals("Reason comments cannot exceed : 250", exception.getMessage());
    }

    @Test
    void testEqualsAndHashCode() {
        RepackCartonCommand command1 = new RepackCartonCommand(1L, "EMP123", "LOC456", "Reason");
        RepackCartonCommand command2 = new RepackCartonCommand(1L, "EMP123", "LOC456", "Reason");
        RepackCartonCommand command3 = new RepackCartonCommand(2L, "EMP123", "LOC456", "Reason");

        // Test equals
        assertEquals(command1, command2);
        assertNotEquals(command1, command3);

        // Test hashCode
        assertEquals(command1.hashCode(), command2.hashCode());
        assertNotEquals(command1.hashCode(), command3.hashCode());
    }
}
