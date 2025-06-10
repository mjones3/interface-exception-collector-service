package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.EnterShippingInformationCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnterShippingInformationCommandTest {

    @Test
    void shouldCreateCommandSuccessfullyWithValidData() {
        // Given
        String productCategory = "ELECTRONICS";
        String employeeId = "EMP123";
        String locationCode = "LOC456";

        // When
        EnterShippingInformationCommand command = new EnterShippingInformationCommand(
            productCategory,
            employeeId,
            locationCode
        );

        // Then
        assertNotNull(command);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldThrowExceptionWhenProductCategoryIsInvalid(String invalidProductCategory) {
        // Given
        String employeeId = "EMP123";
        String locationCode = "LOC456";

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new EnterShippingInformationCommand(invalidProductCategory, employeeId, locationCode)
        );
        assertEquals("Product category is required", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldThrowExceptionWhenEmployeeIdIsInvalid(String invalidEmployeeId) {
        // Given
        String productCategory = "ELECTRONICS";
        String locationCode = "LOC456";

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new EnterShippingInformationCommand(productCategory, invalidEmployeeId, locationCode)
        );
        assertEquals("Employee ID is required", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldThrowExceptionWhenLocationCodeIsInvalid(String invalidLocationCode) {
        // Given
        String productCategory = "ELECTRONICS";
        String employeeId = "EMP123";

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new EnterShippingInformationCommand(productCategory, employeeId, invalidLocationCode)
        );
        assertEquals("Location code is required", exception.getMessage());
    }
}

