package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonClosedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonItemClosedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaShipmentClosedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonEntity;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonItemEntity;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaShipmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring" , unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RecoveredPlasmaShipmentClosedEventMapper {

    @Mapping(target = "closedEmployeeId", source = "recoveredPlasmaShipment.closeEmployeeId")
    @Mapping(target = "closeDate", source = "recoveredPlasmaShipment.modificationDate")
    @Mapping(target = "cartonList", source = "cartonList")
    @Mapping(target = "totalCartons", expression = "java(mapTotalSize(cartonList))")
    @Mapping(target = "locationShipmentCode", source = "locationShipmentCodeParam")
    @Mapping(target = "locationCartonCode", source = "locationCartonCodeParam")
    @Mapping(target = "status", constant = "CLOSED")
    RecoveredPlasmaShipmentClosedOutputDTO entityToCloseEventDTO(RecoveredPlasmaShipmentEntity recoveredPlasmaShipment, List<RecoveredPlasmaCartonClosedOutputDTO> cartonList , String locationShipmentCodeParam , String locationCartonCodeParam );

    @Mapping(target = "productType", source = "recoveredPlasmaShipment.productType")
    @Mapping(target = "locationCode", source = "recoveredPlasmaShipment.locationCode")
    @Mapping(target = "closeEmployeeId", source = "carton.closeEmployeeId")
    @Mapping(target = "closeDate", source = "carton.closeDate")
    @Mapping(target = "status", source = "carton.status")
    @Mapping(target = "cartonSequence", source = "carton.cartonSequenceNumber")
    @Mapping(target = "packedProducts", expression = "java(mapClosedProducts(cartonEntityList))")
    @Mapping(target = "totalProducts", expression = "java(mapTotalSize(cartonEntityList))")
    RecoveredPlasmaCartonClosedOutputDTO cartonModelToEventDTO(CartonEntity carton, RecoveredPlasmaShipment recoveredPlasmaShipment , List<CartonItemEntity> cartonEntityList);

    default List<RecoveredPlasmaCartonItemClosedOutputDTO> mapClosedProducts(List<CartonItemEntity> cartonItems){
        if(cartonItems == null){
            return null;
        }
        return cartonItems.stream().map(this::productModelToEventDTO).toList();
    }

    @Mapping(target = "donationDate", source = "collectionDate")
    @Mapping(target = "collectionFacility", source = "collectionLocation")
    @Mapping(target = "collectionTimeZone", source = "collectionTimeZone")
    RecoveredPlasmaCartonItemClosedOutputDTO productModelToEventDTO(CartonItemEntity cartonItem);

    default int mapTotalSize(List list) {
        return list != null ? list.size() : 0;
    }


}
