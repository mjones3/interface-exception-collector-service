package com.arcone.biopro.distribution.shipping.application.mapper;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.UnitNumberWithCheckDigitDTO;
import com.arcone.biopro.distribution.shipping.domain.model.UnitNumberWithCheckDigit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnitNumberWithCheckDigitMapper {

    public UnitNumberWithCheckDigitDTO mapToDTO(final UnitNumberWithCheckDigit unitNumberWithCheckDigit) {
        return UnitNumberWithCheckDigitDTO.builder()
            .checkDigit(unitNumberWithCheckDigit.getCheckDigit())
            .unitNumber(unitNumberWithCheckDigit.getUnitNumber())
            .verifiedCheckDigit(unitNumberWithCheckDigit.getVerifiedCheckDigit())
            .message(unitNumberWithCheckDigit.isValid() ? null : "Check Digit is invalid")
            .build();
    }

    public UnitNumberWithCheckDigit mapToDomain(final UnitNumberWithCheckDigitDTO unitNumberWithCheckDigitDTO) {
        return new UnitNumberWithCheckDigit(unitNumberWithCheckDigitDTO.unitNumber(), unitNumberWithCheckDigitDTO.checkDigit());
    }

}
