package com.arcone.biopro.distribution.shipping.unit.domain;

import com.arcone.biopro.distribution.shipping.domain.model.UnitNumberWithCheckDigit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UnitNumberWithCheckDigitTest {

    private static final String SAMPLE_UNIT_NUMBER = "W036824954244";
    private static final String SAMPLE_UNIT_NUMBER_CHECK_DIGIT = "W";

    @ParameterizedTest
    @ValueSource(strings = { SAMPLE_UNIT_NUMBER_CHECK_DIGIT, "A", "Z" })
    void testValidation(String inputCheckDigit) {
        assertThrows(IllegalArgumentException.class, () -> new UnitNumberWithCheckDigit(null, null));
        assertThrows(IllegalArgumentException.class, () -> new UnitNumberWithCheckDigit(SAMPLE_UNIT_NUMBER, null));

        var result = assertDoesNotThrow(() -> new UnitNumberWithCheckDigit(SAMPLE_UNIT_NUMBER, inputCheckDigit));
        if (SAMPLE_UNIT_NUMBER_CHECK_DIGIT.equals(inputCheckDigit)) {
            assertTrue(result.isValid());
            assertEquals(result.getCheckDigit(), result.getVerifiedCheckDigit());
        } else {
            assertFalse(result.isValid());
            assertNotEquals(result.getCheckDigit(), result.getVerifiedCheckDigit());
        }
    }

}
