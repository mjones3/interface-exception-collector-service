package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonItemOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartonItemOutputMapper {

    CartonItemOutput toOutput(CartonItem cartonItem);
}
