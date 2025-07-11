package com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject;

import java.util.Objects;

public class UnitNumber {
    private final String value;

    private UnitNumber(String value) {
        this.value = value;
    }

    public static UnitNumber of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("UnitNumber cannot be null or empty");
        }
        return new UnitNumber(value.trim());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnitNumber that = (UnitNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}