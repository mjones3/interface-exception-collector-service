package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CartonDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartonDtoMapper {
   CartonDTO toDto(CartonOutput cartonOutput);
}
