package com.arcone.biopro.distribution.shipping.unit.application.mapper;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.UnitNumberWithCheckDigitDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.UnitNumberWithCheckDigitMapper;
import com.arcone.biopro.distribution.shipping.domain.model.UnitNumberWithCheckDigit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = { UnitNumberWithCheckDigitMapper.class })
class UnitNumberWithCheckDigitMapperTest {

    private static final String SAMPLE_UNIT_NUMBER = "W036824935347";
    private static final String SAMPLE_UNIT_NUMBER_CHECK_DIGIT = "T";

    @Autowired
    UnitNumberWithCheckDigitMapper unitNumberWithCheckDigitMapper;

    @ParameterizedTest
    @ValueSource(strings = { SAMPLE_UNIT_NUMBER_CHECK_DIGIT, "A", "Z" })
    void testMapToDTO(String inputCheckDigit) {
        // Setup
        var unitNumberWithCheckDigit = new UnitNumberWithCheckDigit(SAMPLE_UNIT_NUMBER, inputCheckDigit);

        // Execute
        var result = unitNumberWithCheckDigitMapper.mapToDTO(unitNumberWithCheckDigit);

        // Verify
        assertEquals(unitNumberWithCheckDigit.getUnitNumber(), result.unitNumber());
        assertEquals(unitNumberWithCheckDigit.getCheckDigit(), result.checkDigit());
        assertEquals(unitNumberWithCheckDigit.getVerifiedCheckDigit(), result.verifiedCheckDigit());
        if (result.isValid()) {
            assertNull(result.message());
        } else {
            assertNotNull(result.message());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { SAMPLE_UNIT_NUMBER_CHECK_DIGIT, "A", "Z" })
    void testMapToDomain(String inputCheckDigit) {
        // Setup
        var unitNumberWithCheckDigitDTO = UnitNumberWithCheckDigitDTO.builder()
            .unitNumber(SAMPLE_UNIT_NUMBER)
            .checkDigit(inputCheckDigit)
            .build();

        // Execute
        var result = unitNumberWithCheckDigitMapper.mapToDomain(unitNumberWithCheckDigitDTO);

        // Verify
        assertEquals(unitNumberWithCheckDigitDTO.unitNumber(), result.getUnitNumber());
        assertEquals(unitNumberWithCheckDigitDTO.checkDigit(), result.getCheckDigit());
        if (SAMPLE_UNIT_NUMBER_CHECK_DIGIT.equals(inputCheckDigit)) {
            assertTrue(result.isValid());
            assertEquals(unitNumberWithCheckDigitDTO.checkDigit(), result.getVerifiedCheckDigit());
        } else {
            assertFalse(result.isValid());
            assertNotEquals(unitNumberWithCheckDigitDTO.checkDigit(), result.getVerifiedCheckDigit());
        }
    }

}
