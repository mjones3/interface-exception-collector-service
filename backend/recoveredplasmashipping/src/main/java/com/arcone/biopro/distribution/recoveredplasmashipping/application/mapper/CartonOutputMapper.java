package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartonOutputMapper {
    CartonOutput toOutput(Carton carton);
}
