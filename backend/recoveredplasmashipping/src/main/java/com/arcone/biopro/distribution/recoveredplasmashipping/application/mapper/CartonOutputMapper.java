package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CartonItemOutputMapper.class)
public interface CartonOutputMapper {
    @Mapping(source = "products", target = "packedProducts")
    CartonOutput toOutput(Carton carton);

}
