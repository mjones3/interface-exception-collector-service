package com.arcone.biopro.distribution.shipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Getter
@EqualsAndHashCode
@ToString
public class UnitNumberWithCheckDigit implements Validatable {

    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*";

    private final String unitNumber;
    private final String verifiedCheckDigit;

    public UnitNumberWithCheckDigit(String unitNumber, String checkDigit) {
        this.unitNumber = unitNumber;
        this.checkValid();

        this.verifiedCheckDigit = this.generateVerifiedCheckDigit(unitNumber, checkDigit);
    }

    private String generateVerifiedCheckDigit(String unitNumber, String checkDigit) {
        var sum = 0;
        for (int i = 0; i < unitNumber.length(); i++) {
            var val = ALLOWED_CHARACTERS.indexOf(unitNumber.charAt(i));
            sum = ((sum + val) * 2) % 37;
        }

        var calculatedChecksum = (38 - (sum % 37)) % 37; // Modulo 37 calculation
        var verifiedCheckDigit = String.valueOf(ALLOWED_CHARACTERS.charAt(calculatedChecksum));

        if (!this.isValid(checkDigit, verifiedCheckDigit)) {
            throw new IllegalArgumentException("Check Digit is invalid");
        }
        return verifiedCheckDigit;
    }

    public boolean isValid(String checkDigit, String verifiedCheckDigit) {
        return Objects.nonNull(checkDigit)
            && Objects.nonNull(verifiedCheckDigit)
            && Objects.equals(checkDigit, verifiedCheckDigit);
    }

    @Override
    public void checkValid() {
        if (this.unitNumber == null || this.unitNumber.isBlank()) {
            throw new IllegalArgumentException("unitNumber cannot be null or blank");
        }
    }

}
