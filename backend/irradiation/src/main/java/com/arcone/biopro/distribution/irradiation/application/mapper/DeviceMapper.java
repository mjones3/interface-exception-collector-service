package com.arcone.biopro.distribution.irradiation.application.mapper;

import com.arcone.biopro.distribution.irradiation.application.usecase.CreateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    @Mapping(target = "deviceId", expression = "java(DeviceId.of(input.deviceId()))")
    @Mapping(target = "location.value", source = "location")
    @Mapping(target = "status", source = "status")
    Device toDevice(CreateDeviceUseCase.Input input);
}
