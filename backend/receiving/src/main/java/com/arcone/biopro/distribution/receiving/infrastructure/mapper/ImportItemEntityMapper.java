package com.arcone.biopro.distribution.receiving.infrastructure.mapper;

import com.arcone.biopro.distribution.receiving.domain.model.ImportItem;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ImportItemConsequence;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.ImportItemConsequenceEntity;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.ImportItemEntity;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.ImportItemPropertyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring" , unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ImportItemEntityMapper {

    @Mapping(expression ="java(importItemModel.getAboRh().value())" , target = "bloodType")
    @Mapping(source ="employeeId" , target = "createEmployeeId")
    ImportItemEntity toEntity(ImportItem importItemModel);

    default ImportItem mapToDomain(ImportItemEntity importItemEntity ,List<ImportItemConsequenceEntity> consequenceEntities , List<ImportItemPropertyEntity> propertyEntities) {
        return ImportItem.fromRepository(importItemEntity.getId(), importItemEntity.getImportId(), importItemEntity.getUnitNumber()
            , importItemEntity.getProductCode(), importItemEntity.getBloodType(), importItemEntity.getExpirationDate()
            , importItemEntity.getProductFamily(), importItemEntity.getProductDescription(), importItemEntity.getCreateDate()
            , importItemEntity.getModificationDate(), importItemEntity.getCreateEmployeeId(), toPropertiesMap(propertyEntities)
            , toConsequenceList(consequenceEntities));
    }

   List<ImportItemConsequence> toConsequenceList(List<ImportItemConsequenceEntity> consequenceEntities);

   default Map<String,String> toPropertiesMap(List<ImportItemPropertyEntity> propertyEntities) {
        if (propertyEntities == null) {
            return null;
        }
        return propertyEntities.stream().collect(
            java.util.stream.Collectors.toMap(ImportItemPropertyEntity::getPropertyKey, ImportItemPropertyEntity::getPropertyValue));

    }

    default List<ImportItemPropertyEntity> toPropertyEntities(ImportItemEntity importItem ,Map<String,String> properties) {
        if (properties == null) {
            return null;
        }
        return properties.entrySet().stream().map(entry -> ImportItemPropertyEntity.builder()
            .propertyKey(entry.getKey())
            .importItemId(importItem.getId())
            .propertyValue(entry.getValue())
            .build()).toList();
    }

    default List<ImportItemConsequenceEntity> toConsequenceEntities(ImportItemEntity importItem, List<ImportItemConsequence> consequences) {
        if (consequences == null) {
            return null;
        }
        return consequences.stream().map(consequence -> ImportItemConsequenceEntity.builder()
            .importItemId(importItem.getId())
            .consequenceType(consequence.consequenceType())
            .consequenceReason(consequence.consequenceReason())
            .build())
            .toList();
    }
}
