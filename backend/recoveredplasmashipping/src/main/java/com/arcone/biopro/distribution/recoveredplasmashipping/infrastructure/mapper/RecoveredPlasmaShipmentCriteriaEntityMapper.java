package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentCriteria;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.RecoveredPlasmaShipmentCriteriaItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaShipmentCriteriaEntity;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaShipmentCriteriaItemEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecoveredPlasmaShipmentCriteriaEntityMapper {

    default RecoveredPlasmaShipmentCriteria toModel(RecoveredPlasmaShipmentCriteriaEntity entity , List<RecoveredPlasmaShipmentCriteriaItemEntity> criteriaItemList){
        return new RecoveredPlasmaShipmentCriteria(entity.getId(), entity.getCustomerCode(), entity.getProductType(), toModelList(criteriaItemList));
    }

    RecoveredPlasmaShipmentCriteria toModel(RecoveredPlasmaShipmentCriteriaEntity entity);

    List<RecoveredPlasmaShipmentCriteriaItem> toModelList(List<RecoveredPlasmaShipmentCriteriaItemEntity> entityList);
}
