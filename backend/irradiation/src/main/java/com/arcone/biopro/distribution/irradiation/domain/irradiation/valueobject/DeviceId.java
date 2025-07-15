package com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject;

import java.util.Objects;

public class DeviceId {
    private final String value;

    private DeviceId(String value) {
        this.value = value;
    }

    public static DeviceId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("DeviceId cannot be null or empty");
        }
        return new DeviceId(value.trim());
    }

    public static DeviceId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("DeviceId cannot be null");
        }
        return new DeviceId(value.toString());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceId deviceId = (DeviceId) o;
        return Objects.equals(value, deviceId.value);
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