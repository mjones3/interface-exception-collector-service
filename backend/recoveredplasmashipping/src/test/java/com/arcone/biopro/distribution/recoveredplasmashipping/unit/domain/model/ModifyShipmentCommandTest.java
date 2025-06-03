package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ModifyShipmentCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModifyShipmentCommandTest {

    @Test
    @DisplayName("Should create valid ModifyShipmentCommand when all required fields are provided")
    void shouldCreateValidCommand() {
        // Given
        Long shipmentId = 1L;
        String customerCode = "CUST001";
        String productType = "PLASMA";
        String modifyEmployeeId = "EMP123";
        String transportationRefNumber = "TRN001";
        LocalDate shipmentDate = LocalDate.now().plusDays(1);
        BigDecimal cartonTareWeight = new BigDecimal("10.5");
        String comments = "Test comments";

        // When/Then
        assertDoesNotThrow(() -> new ModifyShipmentCommand(
            shipmentId,
            customerCode,
            productType,
            modifyEmployeeId,
            transportationRefNumber,
            shipmentDate,
            cartonTareWeight,
            comments
        ));
    }

    @Test
    @DisplayName("Should throw exception when shipmentId is null")
    void shouldThrowExceptionWhenShipmentIdIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ModifyShipmentCommand(
                null,
                "CUST001",
                "PLASMA",
                "EMP123",
                "TRN001",
                LocalDate.now().plusDays(1),
                new BigDecimal("10.5"),
                "Test comments"
            )
        );
        assertEquals("Shipment is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when customerCode is blank")
    void shouldThrowExceptionWhenCustomerCodeIsBlank() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ModifyShipmentCommand(
                1L,
                "",
                "PLASMA",
                "EMP123",
                "TRN001",
                LocalDate.now().plusDays(1),
                new BigDecimal("10.5"),
                "Test comments"
            )
        );
        assertEquals("Customer code is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when productType is null")
    void shouldThrowExceptionWhenProductTypeIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ModifyShipmentCommand(
                1L,
                "CUST001",
                null,
                "EMP123",
                "TRN001",
                LocalDate.now().plusDays(1),
                new BigDecimal("10.5"),
                "Test comments"
            )
        );
        assertEquals("Product type is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when modifyEmployeeId is blank")
    void shouldThrowExceptionWhenModifyEmployeeIdIsBlank() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ModifyShipmentCommand(
                1L,
                "CUST001",
                "PLASMA",
                "",
                "TRN001",
                LocalDate.now().plusDays(1),
                new BigDecimal("10.5"),
                "Test comments"
            )
        );
        assertEquals("ModifyEmployeeId employee ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when shipmentDate is in the past")
    void shouldThrowExceptionWhenShipmentDateIsInPast() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ModifyShipmentCommand(
                1L,
                "CUST001",
                "PLASMA",
                "EMP123",
                "TRN001",
                LocalDate.now().minusDays(1),
                new BigDecimal("10.5"),
                "Test comments"
            )
        );
        assertEquals("Shipment date must be in the future", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when comments is null")
    void shouldThrowExceptionWhenCommentsIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ModifyShipmentCommand(
                1L,
                "CUST001",
                "PLASMA",
                "EMP123",
                "TRN001",
                LocalDate.now().plusDays(1),
                new BigDecimal("10.5"),
                null
            )
        );
        assertEquals("Comments is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when comments exceeds 250 characters")
    void shouldThrowExceptionWhenCommentsExceedsMaxLength() {
        String longComments = "a".repeat(251);
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ModifyShipmentCommand(
                1L,
                "CUST001",
                "PLASMA",
                "EMP123",
                "TRN001",
                LocalDate.now().plusDays(1),
                new BigDecimal("10.5"),
                longComments
            )
        );
        assertEquals("Comments cannot be greater than 250 chars.", exception.getMessage());
    }
}
