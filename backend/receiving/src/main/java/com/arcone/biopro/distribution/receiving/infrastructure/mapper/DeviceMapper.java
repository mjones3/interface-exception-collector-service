package com.arcone.biopro.distribution.receiving.infrastructure.mapper;

import com.arcone.biopro.distribution.receiving.domain.model.vo.Barcode;
import com.arcone.biopro.distribution.receiving.domain.model.vo.BloodCenterLocation;
import com.arcone.biopro.distribution.receiving.domain.model.vo.Device;
import com.arcone.biopro.distribution.receiving.domain.model.vo.DeviceType;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.DeviceCreatedMessage;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.DevicePayload;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.DeviceUpdatedMessage;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.DeviceEntity;
import org.mapstruct.Mapper;

import java.time.ZonedDateTime;

@Mapper
public class DeviceMapper {

    public static final DeviceMapper INSTANCE = new DeviceMapper();

    public Device toDomain(DeviceCreatedMessage deviceCreatedMessage) {
        return fromPayload(null, deviceCreatedMessage.getPayload());
    }

    public Device toDomain(Long id, DeviceUpdatedMessage deviceUpdatedMessage) {
        return fromPayload(id, deviceUpdatedMessage.getPayload());
    }

    private Device fromPayload(Long id, DevicePayload payload) {
        return Device.builder()
            .id(id)
            .barcode(new Barcode(payload.getId()))
            .deviceCategory(payload.getDeviceCategory())
            .serialNumber(payload.getSerialNumber())
            .type(DeviceType.getInstance(payload.getDevice()))
            .location(new BloodCenterLocation(payload.getLocation()))
            .name(payload.getName())
            .active("ACTIVE".equals(payload.getStatus()))
            .createDate(payload.getCreateDate())
            .modificationDate(ZonedDateTime.now())
            .build();
    }


    public DeviceEntity toEntity(Device device) {
        return DeviceEntity.builder()
            .id(device.id())
            .serialNumber(device.serialNumber())
            .category(device.deviceCategory())
            .name(device.name())
            .type(device.type().value())
            .location(device.location().name())
            .active(device.active())
            .createDate(device.createDate())
            .modificationDate(ZonedDateTime.now())
            .bloodCenterId(device.barcode().bloodCenterId())
            .build();
    }

    public Device toDomain(DeviceEntity deviceEntity) {
        return Device.builder()
            .id(deviceEntity.getId())
            .barcode(new Barcode(deviceEntity.getBloodCenterId()))
            .deviceCategory(deviceEntity.getCategory())
            .serialNumber(deviceEntity.getSerialNumber())
            .type(DeviceType.getInstance(deviceEntity.getType()))
            .location(new BloodCenterLocation(deviceEntity.getLocation()))
            .name(deviceEntity.getName())
            .active(deviceEntity.getActive())
            .createDate(deviceEntity.getCreateDate())
            .modificationDate(ZonedDateTime.now())
            .build();
    }

}
