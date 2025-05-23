package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonItemPackedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonItemUnpackedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonPackedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonRemovedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonUnpackedOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring" , unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RecoveredPlasmaCartonEventMapper {

    @Mapping(target ="productType" , expression = "java(carton.getProducts().getFirst().getProductType())")
    @Mapping(source ="locationCode" , target = "locationCode")
    @Mapping(source ="carton.products" , target = "packedProducts")
    RecoveredPlasmaCartonPackedOutputDTO modelToPackedEventDTO(Carton carton , String locationCode);

    @Mapping(source = "createDate",target = "packedDate")
    RecoveredPlasmaCartonItemPackedOutputDTO modelToItemPackedEventDTO(CartonItem  cartonItem);

    List<RecoveredPlasmaCartonItemPackedOutputDTO> modelToItemPackedEventDTO(List<CartonItem> cartonItems);

    @Mapping(target ="productType" , expression = "java(carton.getProducts().getFirst().getProductType())")
    @Mapping(source ="locationCode" , target = "locationCode")
    @Mapping(source ="carton.products" , target = "unpackedProducts")
    @Mapping(source ="carton.repackEmployeeId" , target = "unpackEmployeeId")
    @Mapping(source ="carton.repackDate" , target = "unpackDate")
    RecoveredPlasmaCartonUnpackedOutputDTO modelToUnPackedEventDTO(Carton carton , String locationCode);

    @Mapping(target = "status" , constant = "REMOVED")
    RecoveredPlasmaCartonItemUnpackedOutputDTO modelToItemUnPackedEventDTO(CartonItem  cartonItem);

    List<RecoveredPlasmaCartonItemUnpackedOutputDTO> modelToItemUnpackedEventDTO(List<CartonItem> cartonItems);


    @Mapping(target ="productType" , source = "productType")
    @Mapping(source ="locationCode" , target = "locationCode")
    @Mapping(source ="carton.products" , target = "unpackedProducts")
    @Mapping(source ="carton.deleteEmployeeId" , target = "removeEmployeeId")
    @Mapping(source ="carton.deleteDate" , target = "removeDate")
    RecoveredPlasmaCartonRemovedOutputDTO modelToRemovedEventDTO(Carton carton , String locationCode , String productType);
}
