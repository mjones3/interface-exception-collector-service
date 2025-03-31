package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentCriteria;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaShipmentCriteriaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecoveredPlasmaShipmentCriteriaEntityMapper {

    RecoveredPlasmaShipmentCriteria toModel(RecoveredPlasmaShipmentCriteriaEntity entity);
}
