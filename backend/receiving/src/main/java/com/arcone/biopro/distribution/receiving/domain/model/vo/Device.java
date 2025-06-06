package com.arcone.biopro.distribution.receiving.domain.model.vo;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record Device(Long id, DeviceType type , String deviceCategory, Barcode barcode, String serialNumber,
                     BloodCenterLocation location, String name, Boolean active,
                     ZonedDateTime createDate, ZonedDateTime modificationDate) {

    public Device(DeviceType type , String deviceCategory, Barcode barcode) {
        this(null, type,deviceCategory, barcode, barcode.bloodCenterId(), null, null, true, null, null);
    }

    public static Device of(DeviceType type,
                            Barcode barcode,
                            String serialNumber,
                            BloodCenterLocation location) {
        return new Device(null, type,null, barcode, serialNumber, location, null, true, null, null);
    }

    public boolean isValid() {
        return this.active;
    }

}
