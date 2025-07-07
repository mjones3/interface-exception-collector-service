package com.arcone.biopro.distribution.irradiation.domain.util;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductCodeUtilTest {

    @Test
    @DisplayName("Is a final product code with sixth digit")
    void testIsAFinalProductCodeWithSixthDigit_true() {
        assertTrue(ProductCodeUtil.isAFinalProductCodeWithSixthDigit("AB123X45"));
    }

    @Test
    @DisplayName("Is a final product code without sixth digit")
    void testIsAFinalProductCodeWithSixthDigit_false() {
        assertFalse(ProductCodeUtil.isAFinalProductCodeWithSixthDigit("AB12345"));
    }

    @Test
    @DisplayName("Is a final product code")
    void testIsAFinalProductCode_true() {
        assertTrue(ProductCodeUtil.isAFinalProductCode("AB12345"));
    }

    @Test
    @DisplayName("Is not a final product code")
    void testIsAFinalProductCode_false() {
        assertFalse(ProductCodeUtil.isAFinalProductCode("AB123X45"));
    }

    @Test
    @DisplayName("Remove the sixth digit from a final product code")
    void testRetrieveFinalProductCodeWithoutSixthDigit_removesSixthChar() {
        String result = ProductCodeUtil.retrieveFinalProductCodeWithoutSixthDigit("AB123X45");
        assertEquals("AB12345", result);
    }

    @Test
    @DisplayName("Retrieve a final product code from a final product code with no sixth digit")
    void testRetrieveFinalProductCodeWithoutSixthDigit_noChange() {
        String result = ProductCodeUtil.retrieveFinalProductCodeWithoutSixthDigit("AB12345");
        assertEquals("AB12345", result);
    }

    @Test
    @DisplayName("Retrieve a final product code from a final product code with  sixth digit")
    void testRetrieveFinalProductCodeWithoutSixthDigit_invalidFormat() {
        String result = ProductCodeUtil.retrieveFinalProductCodeWithoutSixthDigit("A123X45");
        assertEquals("A123X45", result);
    }
}
