package com.arcone.biopro.distribution.shipping.unit.application.mapper;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.UnitNumberWithCheckDigitDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.UnitNumberWithCheckDigitMapper;
import com.arcone.biopro.distribution.shipping.domain.model.UnitNumberWithCheckDigit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(classes = { UnitNumberWithCheckDigitMapper.class })
class UnitNumberWithCheckDigitMapperTest {

    private static final String SAMPLE_UNIT_NUMBER = "W036824935347";
    private static final String SAMPLE_UNIT_NUMBER_CHECK_DIGIT = "T";

    @Autowired
    UnitNumberWithCheckDigitMapper unitNumberWithCheckDigitMapper;

    @Test
    void shouldMapToDTO() {
        // Setup
        var unitNumberWithCheckDigit = new UnitNumberWithCheckDigit(SAMPLE_UNIT_NUMBER, SAMPLE_UNIT_NUMBER_CHECK_DIGIT);

        // Execute
        var result = unitNumberWithCheckDigitMapper.mapToDTO(unitNumberWithCheckDigit);

        // Verify
        assertEquals(unitNumberWithCheckDigit.getUnitNumber(), result.unitNumber());
        assertEquals(unitNumberWithCheckDigit.getVerifiedCheckDigit(), result.verifiedCheckDigit());
    }

    @Test
    void shouldMapToDomain() {
        // Setup
        var unitNumberWithCheckDigitDTO = UnitNumberWithCheckDigitDTO.builder()
            .unitNumber(SAMPLE_UNIT_NUMBER)
            .verifiedCheckDigit(SAMPLE_UNIT_NUMBER_CHECK_DIGIT)
            .build();

        // Execute
        var result = unitNumberWithCheckDigitMapper.mapToDomain(unitNumberWithCheckDigitDTO);

        // Verify
        assertEquals(unitNumberWithCheckDigitDTO.unitNumber(), result.getUnitNumber());
        assertEquals(unitNumberWithCheckDigitDTO.verifiedCheckDigit(), result.getVerifiedCheckDigit());
    }

}
