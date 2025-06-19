package com.arcone.biopro.distribution.receiving.domain.model.vo;

import com.arcone.biopro.distribution.receiving.domain.exception.TypeNotConfiguredException;

import java.util.List;

public record LicenseStatus(String value) {

    private static final String LICENSED = "LICENSED";
    private static final String UNLICENSED = "UNLICENSED";

    private static final List<String> validTypes = List.of(LICENSED,UNLICENSED);

    public static LicenseStatus LICENSED() {
        return new LicenseStatus(LICENSED);
    }

    public static LicenseStatus UNLICENSED() {
        return new LicenseStatus(UNLICENSED);
    }

    public static LicenseStatus getInstance(String value) {
        if (!validTypes.contains(value)) {
            throw new TypeNotConfiguredException("License Status Not Configured");
        }
        if (LICENSED.equals(value)) {
            return LICENSED();
        } else {
            return UNLICENSED();
        }
    }


}
