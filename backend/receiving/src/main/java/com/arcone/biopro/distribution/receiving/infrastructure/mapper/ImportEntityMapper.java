package com.arcone.biopro.distribution.receiving.infrastructure.mapper;

import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.model.ImportItem;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.ImportEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImportEntityMapper {

    default Import mapToDomain(final ImportEntity importEntity, int maxNumberOfProducts) {
        return Import.fromRepository(importEntity.getId(), importEntity.getTemperatureCategory(), importEntity.getTransitStartDateTime()
            , importEntity.getTransitStartTimeZone(), importEntity.getTransitEndDateTime(), importEntity.getTransitEndTimeZone(), importEntity.getTotalTransitTime()
            , importEntity.getTransitTimeResult(), importEntity.getTemperature(), importEntity.getThermometerCode(), importEntity.getTemperatureResult()
            , importEntity.getLocationCode(), importEntity.getComments(), importEntity.getStatus(), importEntity.getEmployeeId(), importEntity.getCreateDate()
            , importEntity.getModificationDate(),null,maxNumberOfProducts);
    }

    default Import mapToDomain(final ImportEntity importEntity , List<ImportItem> importItemList , int maxNumberOfProducts) {
        return Import.fromRepository(importEntity.getId(), importEntity.getTemperatureCategory(), importEntity.getTransitStartDateTime()
            , importEntity.getTransitStartTimeZone(), importEntity.getTransitEndDateTime(), importEntity.getTransitEndTimeZone(), importEntity.getTotalTransitTime()
            , importEntity.getTransitTimeResult(), importEntity.getTemperature(), importEntity.getThermometerCode(), importEntity.getTemperatureResult()
            , importEntity.getLocationCode(), importEntity.getComments(), importEntity.getStatus(), importEntity.getEmployeeId(), importEntity.getCreateDate()
            , importEntity.getModificationDate(),importItemList,maxNumberOfProducts);
    }

    ImportEntity toEntity(Import importDomain);
}
