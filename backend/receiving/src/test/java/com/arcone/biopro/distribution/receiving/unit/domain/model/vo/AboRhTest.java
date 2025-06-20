package com.arcone.biopro.distribution.receiving.unit.domain.model.vo;

import com.arcone.biopro.distribution.receiving.domain.exception.TypeNotConfiguredException;
import com.arcone.biopro.distribution.receiving.domain.model.vo.AboRh;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AboRhTest {

    @Test
    void shouldCreateValidAboRhInstances() {
        // Test all factory methods
        assertEquals("O Negative", AboRh.ON().description());
        assertEquals("ON", AboRh.ON().value());

        assertEquals("A Negative", AboRh.AN().description());
        assertEquals("AN", AboRh.AN().value());

        assertEquals("B Negative", AboRh.BN().description());
        assertEquals("BN", AboRh.BN().value());

        assertEquals("AB Negative", AboRh.ABN().description());
        assertEquals("ABN", AboRh.ABN().value());

        assertEquals("O Positive", AboRh.OP().description());
        assertEquals("OP", AboRh.OP().value());

        assertEquals("A Positive", AboRh.AP().description());
        assertEquals("AP", AboRh.AP().value());

        assertEquals("B Positive", AboRh.BP().description());
        assertEquals("BP", AboRh.BP().value());

        assertEquals("AB Positive", AboRh.ABP().description());
        assertEquals("ABP", AboRh.ABP().value());
    }

    @Test
    void shouldGetInstanceForValidValues() {
        // Test getInstance method for all valid values
        assertEquals(AboRh.AP(), AboRh.getInstance("AP"));
        assertEquals(AboRh.AN(), AboRh.getInstance("AN"));
        assertEquals(AboRh.BP(), AboRh.getInstance("BP"));
        assertEquals(AboRh.BN(), AboRh.getInstance("BN"));
        assertEquals(AboRh.ABP(), AboRh.getInstance("ABP"));
        assertEquals(AboRh.ABN(), AboRh.getInstance("ABN"));
        assertEquals(AboRh.OP(), AboRh.getInstance("OP"));
        assertEquals(AboRh.ON(), AboRh.getInstance("ON"));
    }

    @Test
    void shouldValidateValidAboRhValues() {
        // Test validation with valid values
        assertDoesNotThrow(() -> AboRh.validateAboRh("AP"));
        assertDoesNotThrow(() -> AboRh.validateAboRh("AN"));
        assertDoesNotThrow(() -> AboRh.validateAboRh("BP"));
        assertDoesNotThrow(() -> AboRh.validateAboRh("BN"));
        assertDoesNotThrow(() -> AboRh.validateAboRh("ABP"));
        assertDoesNotThrow(() -> AboRh.validateAboRh("ABN"));
        assertDoesNotThrow(() -> AboRh.validateAboRh("OP"));
        assertDoesNotThrow(() -> AboRh.validateAboRh("ON"));
    }

    @Test
    void shouldThrowExceptionForInvalidAboRhValue() {
        // Test validation with invalid values
        assertThrows(TypeNotConfiguredException.class,
            () -> AboRh.validateAboRh("XY"));
        assertThrows(TypeNotConfiguredException.class,
            () -> AboRh.validateAboRh(""));
        assertThrows(TypeNotConfiguredException.class,
            () -> AboRh.validateAboRh(null));
    }
}
