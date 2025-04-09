package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;


import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonEntity;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CartonEntityMapper {

    CartonItemEntityMapper cartonItemEntityMapper = Mappers.getMapper(CartonItemEntityMapper.class);

    default Carton entityToModel(CartonEntity entity , List<CartonItemEntity> cartonItemEntityList) {
        return Carton.fromRepository(
            entity.getId(), entity.getCartonNumber(), entity.getShipmentId(), entity.getCartonSequenceNumber(), entity.getCreateEmployeeId(),
            entity.getCloseEmployeeId(), entity.getCreateDate(), entity.getModificationDate(), entity.getCloseDate(), entity.getStatus() , cartonItemEntityMapper.toModelList(cartonItemEntityList)
        );
    }

    default Carton entityToModel(CartonEntity entity) {
        return Carton.fromRepository(
            entity.getId(), entity.getCartonNumber(), entity.getShipmentId(), entity.getCartonSequenceNumber(), entity.getCreateEmployeeId(),
            entity.getCloseEmployeeId(), entity.getCreateDate(), entity.getModificationDate(), entity.getCloseDate(), entity.getStatus() , Collections.emptyList()
            entity.getCloseEmployeeId(), entity.getCreateDate(), entity.getModificationDate(), entity.getCloseDate(), entity.getStatus() , entity.getTotalVolume() , entity.getTotalWeight()
        );
    }

    @Mapping(source = "cartonSequence", target = "cartonSequenceNumber")
    CartonEntity toEntity(Carton model);

    List<Carton> toModelList(List<CartonEntity> cartonEntityList);
}



