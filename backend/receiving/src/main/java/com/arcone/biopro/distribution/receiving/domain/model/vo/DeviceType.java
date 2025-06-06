package com.arcone.biopro.distribution.receiving.domain.model.vo;

import com.arcone.biopro.distribution.receiving.domain.exception.TypeNotConfiguredException;

import java.util.List;



public record DeviceType(String value, String errorMessage) {

    private static final String STERILE_CONNECTION = "STERILE_CONNECTION";
    private static final String PRODUCT_SCALE = "PRODUCT_SCALE";
    private static final String ILLUMINATOR = "ILLUMINATOR";
    private static final List<String> validDeviceTypes = List.of(STERILE_CONNECTION,
        PRODUCT_SCALE,
        ILLUMINATOR);

    public static void validateDeviceType(String value) {
        if (!validDeviceTypes.contains(value)) {
            throw new TypeNotConfiguredException("DEVICE_TYPE_NOT_CONFIGURED");
        }
    }

    public static DeviceType STERILE_CONNECTION() {
        return new DeviceType(STERILE_CONNECTION, "STERILE_CONNECTION_DEVICE_DOES_NOT_EXIST");
    }

    public static DeviceType PRODUCT_SCALE() {
        return new DeviceType(PRODUCT_SCALE, "SCALE_DEVICE_DOES_NOT_EXIST");
    }

    public static DeviceType ILLUMINATOR() {
        return new DeviceType(ILLUMINATOR, "ILLUMINATOR_DOES_NOT_EXIST");
    }

    public static DeviceType getInstance(String value) {
        validateDeviceType(value);
        return switch (value) {
            case "PRODUCT_SCALE" -> PRODUCT_SCALE();
            case "STERILE_CONNECTION" -> STERILE_CONNECTION();
            case "ILLUMINATOR" -> ILLUMINATOR();
            default -> null;
        };
    }

}
