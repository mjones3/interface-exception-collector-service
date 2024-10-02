package com.arcone.biopro.distribution.shipping.domain.model.vo;

import com.arcone.biopro.distribution.shipping.domain.model.Validatable;
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
    private final String checkDigit;
    private final String verifiedCheckDigit;

    public UnitNumberWithCheckDigit(String unitNumber, String checkDigit) {
        this.unitNumber = unitNumber;
        this.checkDigit = checkDigit;
        this.verifiedCheckDigit = this.generateVerifiedCheckDigit(unitNumber);
        this.checkValid();
    }

    private String generateVerifiedCheckDigit(String unitNumber) {
        if (unitNumber == null) {
            return null;
        }

        try {
            var sum = 0;
            for (int i = 0; i < unitNumber.length(); i++) {
                var val = ALLOWED_CHARACTERS.indexOf(unitNumber.charAt(i));
                sum = ((sum + val) * 2) % 37;
            }

            var calculatedChecksum = (38 - (sum % 37)) % 37; // Modulo 37 calculation
            return String.valueOf(ALLOWED_CHARACTERS.charAt(calculatedChecksum));
        } catch (Exception e) {
            log.warn("Failed to generate verified check digit", e);
            return null;
        }
    }

    public boolean isValid() {
        return Objects.nonNull(this.checkDigit)
            && Objects.nonNull(this.verifiedCheckDigit)
            && Objects.equals(checkDigit, verifiedCheckDigit);
    }

    @Override
    public void checkValid() {
        if (unitNumber == null) {
            throw new IllegalArgumentException("unitNumber cannot be null");
        }

        if (this.checkDigit == null
            || this.checkDigit.isBlank()
            || this.verifiedCheckDigit == null
            || this.verifiedCheckDigit.isBlank()
        ) {
            throw new IllegalArgumentException("checkDigit and verifiedCheckDigit cannot be null or blank");
        }
    }

}
