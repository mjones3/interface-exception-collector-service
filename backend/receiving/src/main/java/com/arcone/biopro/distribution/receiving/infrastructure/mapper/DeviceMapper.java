package com.arcone.biopro.distribution.receiving.infrastructure.mapper;

import com.arcone.biopro.distribution.receiving.domain.model.Device;
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
        return Device.fromEvent(id, payload.getDevice(), payload.getDeviceCategory(), payload.getId(), payload.getSerialNumber()
            , payload.getLocation(), payload.getName(), payload.getStatus(), payload.getCreateDate(), ZonedDateTime.now());
    }


    public DeviceEntity toEntity(Device device) {
        return DeviceEntity.builder()
            .id(device.getId())
            .serialNumber(device.getSerialNumber())
            .category(device.getDeviceCategory().value())
            .name(device.getName())
            .type(device.getType().value())
            .location(device.getLocation().code())
            .active(device.getActive())
            .createDate(device.getCreateDate())
            .modificationDate(ZonedDateTime.now())
            .bloodCenterId(device.getBarcode().bloodCenterId())
            .build();
    }

    public Device toDomain(DeviceEntity deviceEntity) {
        return Device.fromRepository(deviceEntity.getId(), deviceEntity.getType(), deviceEntity.getCategory()
            , deviceEntity.getBloodCenterId(), deviceEntity.getSerialNumber(), deviceEntity.getLocation(), deviceEntity.getName()
            , deviceEntity.getActive(), deviceEntity.getCreateDate(), ZonedDateTime.now());
    }

}
