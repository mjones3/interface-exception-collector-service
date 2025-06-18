package com.arcone.biopro.distribution.receiving.domain.model.vo;

import com.arcone.biopro.distribution.receiving.domain.exception.TypeNotConfiguredException;

import java.util.List;


public record DeviceCategory(String value, String errorMessage) {

    private static final String TEMPERATURE = "TEMPERATURE";
    private static final List<String> validDeviceCategories = List.of(TEMPERATURE);

    public static void validateDeviceCategory(String value) {
        if (!validDeviceCategories.contains(value)) {
            throw new TypeNotConfiguredException("DEVICE_CATEGORY_NOT_CONFIGURED");
        }
    }

    public static DeviceCategory TEMPERATURE() {
        return new DeviceCategory(TEMPERATURE, "TEMPERATURE_DEVICE_DOES_NOT_EXIST");
    }

    public static DeviceCategory getInstance(String value) {
        validateDeviceCategory(value);
            if(TEMPERATURE.equals(value)){
                return TEMPERATURE();
            }
            return null;
    }

}
