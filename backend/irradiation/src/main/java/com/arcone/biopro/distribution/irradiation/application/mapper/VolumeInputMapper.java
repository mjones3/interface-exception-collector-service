package com.arcone.biopro.distribution.irradiation.application.mapper;

import com.arcone.biopro.distribution.irradiation.application.dto.VolumeInput;
import com.arcone.biopro.distribution.irradiation.domain.model.Volume;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VolumeInputMapper {
    List<Volume> toDomain(List<VolumeInput> volumes);
}
