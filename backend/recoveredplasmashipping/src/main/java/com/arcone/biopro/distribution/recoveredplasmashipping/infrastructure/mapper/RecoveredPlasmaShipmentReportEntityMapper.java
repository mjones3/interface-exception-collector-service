package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;


import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentReport;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaShipmentReportEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RecoveredPlasmaShipmentReportEntityMapper {
    @Mapping(source ="id" , target = "shipmentId")
    @Mapping(source ="productTypeDescription" , target = "productType")
    RecoveredPlasmaShipmentReport toModel(RecoveredPlasmaShipmentReportEntity recoveredPlasmaShipmentReportEntity);
}



