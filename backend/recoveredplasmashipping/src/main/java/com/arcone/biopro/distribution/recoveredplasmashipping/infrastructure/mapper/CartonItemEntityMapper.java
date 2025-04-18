package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonItemEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartonItemEntityMapper {

    CartonItemEntity toEntity(CartonItem cartonItem);

    default CartonItem entityToModel(CartonItemEntity entity) {
        if (entity == null){
            return null;
        }
        return CartonItem.fromRepository(
            entity.getId(), entity.getCartonId(), entity.getUnitNumber(), entity.getProductCode(), entity.getProductDescription(),
            entity.getProductType(), entity.getVolume(), entity.getWeight(), entity.getPackedByEmployeeId(), entity.getAboRh(),
            entity.getStatus(), entity.getExpirationDate(), entity.getCollectionDate(), entity.getCreateDate(), entity.getModificationDate()
        );
    }

    List<CartonItem> toModelList(List<CartonItemEntity> cartonItemEntityList);

}
