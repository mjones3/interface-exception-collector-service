package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonItemPackedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonPackedOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring" , unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RecoveredPlasmaCartonEventMapper {

    @Mapping(target ="productTye" , expression = "java(carton.getProducts().getFirst().getProductType())")
    @Mapping(source ="products" , target = "packedProducts")
    RecoveredPlasmaCartonPackedOutputDTO modelToPackedEventDTO(Carton carton);

    @Mapping(source = "createDate",target = "packedDate")
    RecoveredPlasmaCartonItemPackedOutputDTO modelToItemPackedEventDTO(CartonItem  cartonItem);

    List<RecoveredPlasmaCartonItemPackedOutputDTO> modelToItemPackedEventDTO(List<CartonItem> cartonItems);


}
