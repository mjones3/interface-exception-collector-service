package com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject;

import java.util.Objects;

public class Location {
    private final String value;

    private Location(String value) {
        this.value = value;
    }

    public static Location of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }
        return new Location(value.trim());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(value, location.value);
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