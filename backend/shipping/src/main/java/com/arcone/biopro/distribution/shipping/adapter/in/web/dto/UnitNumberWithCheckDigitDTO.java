package com.arcone.biopro.distribution.shipping.adapter.in.web.dto;

import lombok.Builder;

import java.util.Objects;

@Builder
public record UnitNumberWithCheckDigitDTO(
    String unitNumber,
    String checkDigit,
    String verifiedCheckDigit,
    String message
) {

    public boolean isValid() {
        return Objects.nonNull(this.checkDigit)
            && Objects.nonNull(this.verifiedCheckDigit)
            && Objects.equals(checkDigit, verifiedCheckDigit);
    }

}
