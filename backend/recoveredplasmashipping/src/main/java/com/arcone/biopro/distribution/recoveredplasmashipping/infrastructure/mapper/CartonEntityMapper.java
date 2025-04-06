package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;


import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartonEntityMapper {
    default Carton entityToModel(CartonEntity entity) {
        return Carton.fromRepository(
            entity.getId(), entity.getCartonNumber(), entity.getShipmentId(), entity.getCartonSequenceNumber(), entity.getCreateEmployeeId(),
            entity.getCloseEmployeeId(), entity.getCreateDate(), entity.getModificationDate(), entity.getCloseDate(), entity.getStatus()
        );
    }

    @Mapping(source = "cartonSequence", target = "cartonSequenceNumber")
    CartonEntity toEntity(Carton model);

    List<Carton> toModelList(List<CartonEntity> cartonEntityList);
}



