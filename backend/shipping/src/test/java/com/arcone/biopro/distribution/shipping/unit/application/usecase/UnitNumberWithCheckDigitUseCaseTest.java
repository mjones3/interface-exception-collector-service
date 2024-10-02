package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.application.usecase.UnitNumberWithCheckDigitUseCase;
import com.arcone.biopro.distribution.shipping.domain.service.UnitNumberWithCheckDigitService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = { UnitNumberWithCheckDigitUseCase.class })
class UnitNumberWithCheckDigitUseCaseTest {

    private static final String SAMPLE_UNIT_NUMBER = "W036824944450";
    private static final String SAMPLE_UNIT_NUMBER_CHECK_DIGIT = "F";

    @Autowired
    UnitNumberWithCheckDigitService unitNumberWithCheckDigitService;

    @ParameterizedTest
    @ValueSource(strings = { SAMPLE_UNIT_NUMBER_CHECK_DIGIT, "A", "Z" })
    void testVerifyCheckDigit(String inputCheckDigit) {
        var result = requireNonNull(unitNumberWithCheckDigitService.verifyCheckDigit(SAMPLE_UNIT_NUMBER, inputCheckDigit).block());

        // Verify
        if (SAMPLE_UNIT_NUMBER_CHECK_DIGIT.equals(inputCheckDigit)) {
            assertTrue(result.isValid());
            assertEquals(result.getCheckDigit(), result.getVerifiedCheckDigit());
        } else {
            assertFalse(result.isValid());
            assertNotEquals(result.getCheckDigit(), result.getVerifiedCheckDigit());
        }
    }

}
