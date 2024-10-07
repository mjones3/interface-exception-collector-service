package com.arcone.biopro.distribution.shipping.unit.domain;

import com.arcone.biopro.distribution.shipping.domain.model.UnitNumberWithCheckDigit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnitNumberWithCheckDigitTest {

    private static final String SAMPLE_UNIT_NUMBER = "W036824954244";
    private static final String SAMPLE_UNIT_NUMBER_CHECK_DIGIT = "W";

    @Test
    void shouldBuild() {
        assertDoesNotThrow(() -> new UnitNumberWithCheckDigit(SAMPLE_UNIT_NUMBER, SAMPLE_UNIT_NUMBER_CHECK_DIGIT));
    }

    @Test
    void shouldNotBuild() {
        assertThrows(IllegalArgumentException.class, () -> new UnitNumberWithCheckDigit(null, null));
        assertThrows(IllegalArgumentException.class, () -> new UnitNumberWithCheckDigit("", ""));
        assertThrows(IllegalArgumentException.class, () -> new UnitNumberWithCheckDigit(SAMPLE_UNIT_NUMBER, null));
        assertThrows(IllegalArgumentException.class, () -> new UnitNumberWithCheckDigit(null, SAMPLE_UNIT_NUMBER_CHECK_DIGIT));
        assertThrows(IllegalArgumentException.class, () -> new UnitNumberWithCheckDigit(SAMPLE_UNIT_NUMBER, "A"));
    }

}
