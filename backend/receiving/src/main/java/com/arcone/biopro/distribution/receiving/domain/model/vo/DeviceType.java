package com.arcone.biopro.distribution.receiving.domain.model.vo;

import com.arcone.biopro.distribution.receiving.domain.exception.TypeNotConfiguredException;

import java.util.List;



public record DeviceType(String value, String errorMessage) {

    private static final String THERMOMETER = "THERMOMETER";
    private static final List<String> validDeviceTypes = List.of(THERMOMETER);

    public static void validateDeviceType(String value) {
        if (!validDeviceTypes.contains(value)) {
            throw new TypeNotConfiguredException("DEVICE_TYPE_NOT_CONFIGURED");
        }
    }

    public static DeviceType THERMOMETER() {
        return new DeviceType(THERMOMETER, "THERMOMETER_DEVICE_DOES_NOT_EXIST");
    }
    public static DeviceType getInstance(String value) {
        validateDeviceType(value);
        if(THERMOMETER.equals(value)){
            return THERMOMETER();
        }
        return null;
    }

}
