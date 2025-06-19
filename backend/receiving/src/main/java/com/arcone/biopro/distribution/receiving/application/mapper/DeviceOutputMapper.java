package com.arcone.biopro.distribution.receiving.application.mapper;

import com.arcone.biopro.distribution.receiving.application.dto.DeviceOutput;
import com.arcone.biopro.distribution.receiving.domain.model.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeviceOutputMapper {
    @Mapping(source ="barcode.bloodCenterId" , target = "bloodCenterId")
    @Mapping(source ="type.value" , target = "deviceType")
    @Mapping(source ="deviceCategory.value" , target = "deviceCategory")
    @Mapping(source ="location.code" , target = "locationCode")
    DeviceOutput toOutput(Device device);
}
