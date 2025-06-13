package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.ProductConsequence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductConsequenceTest {

    @Test
    void shouldCreateProductConsequenceSuccessfully() {
        // Given
        Long id = 1L;
        String productCategory = "ELECTRONICS";
        boolean acceptable = true;
        String resultProperty = "TEMPERATURE";
        String resultType = "RANGE";
        String resultValue = "0-30";
        String consequenceType = "REJECT";
        String consequenceReason = "Temperature out of range";

        // When
        ProductConsequence consequence = new ProductConsequence(
            id,
            productCategory,
            acceptable,
            resultProperty,
            resultType,
            resultValue,
            consequenceType,
            consequenceReason
        );

        // Then
        assertNotNull(consequence);
        assertEquals(id, consequence.getId());
        assertEquals(productCategory, consequence.getProductCategory());
        assertEquals(acceptable, consequence.isAcceptable());
        assertEquals(resultProperty, consequence.getResultProperty());
        assertEquals(resultType, consequence.getResultType());
        assertEquals(resultValue, consequence.getResultValue());
        assertEquals(consequenceType, consequence.getConsequenceType());
        assertEquals(consequenceReason, consequence.getConsequenceReason());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldThrowExceptionWhenProductCategoryIsInvalid(String invalidProductCategory) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ProductConsequence(
                1L,
                invalidProductCategory,
                true,
                "TEMPERATURE",
                "RANGE",
                "0-30",
                "REJECT",
                "Temperature out of range"
            )
        );
        assertEquals("Product category is required", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldThrowExceptionWhenResultPropertyIsInvalid(String invalidResultProperty) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ProductConsequence(
                1L,
                "ELECTRONICS",
                true,
                invalidResultProperty,
                "RANGE",
                "0-30",
                "REJECT",
                "Temperature out of range"
            )
        );
        assertEquals("Result property is required", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldThrowExceptionWhenResultTypeIsInvalid(String invalidResultType) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ProductConsequence(
                1L,
                "ELECTRONICS",
                true,
                "TEMPERATURE",
                invalidResultType,
                "0-30",
                "REJECT",
                "Temperature out of range"
            )
        );
        assertEquals("Result type is required", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldThrowExceptionWhenResultValueIsInvalid(String invalidResultValue) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ProductConsequence(
                1L,
                "ELECTRONICS",
                true,
                "TEMPERATURE",
                "RANGE",
                invalidResultValue,
                "REJECT",
                "Temperature out of range"
            )
        );
        assertEquals("Result value is required", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldThrowExceptionWhenConsequenceTypeIsInvalid(String invalidConsequenceType) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ProductConsequence(
                1L,
                "ELECTRONICS",
                true,
                "TEMPERATURE",
                "RANGE",
                "0-30",
                invalidConsequenceType,
                "Temperature out of range"
            )
        );
        assertEquals("Consequence type is required", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldThrowExceptionWhenConsequenceReasonIsInvalid(String invalidConsequenceReason) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ProductConsequence(
                1L,
                "ELECTRONICS",
                true,
                "TEMPERATURE",
                "RANGE",
                "0-30",
                "REJECT",
                invalidConsequenceReason
            )
        );
        assertEquals("Consequence reason is required", exception.getMessage());
    }

    @Test
    void shouldAllowNullId() {
        // When
        ProductConsequence consequence = new ProductConsequence(
            null,
            "ELECTRONICS",
            true,
            "TEMPERATURE",
            "RANGE",
            "0-30",
            "REJECT",
            "Temperature out of range"
        );

        // Then
        assertNotNull(consequence);
        assertNull(consequence.getId());
    }
}

