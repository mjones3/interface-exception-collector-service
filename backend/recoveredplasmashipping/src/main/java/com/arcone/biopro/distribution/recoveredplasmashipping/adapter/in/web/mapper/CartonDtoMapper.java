package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CartonDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartonDtoMapper {

    @Mapping(target = "canVerify", expression = "java(cartonOutput.canVerify())")
    @Mapping(target = "canClose", expression = "java(cartonOutput.canClose())")
    CartonDTO toDto(CartonOutput cartonOutput);
}
