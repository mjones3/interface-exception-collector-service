package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;


import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShipmentHistory;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.ShipmentHistoryEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentHistoryEntityMapper {
    ShipmentHistory toModel(ShipmentHistoryEntity shipmentHistoryEntity);
    ShipmentHistoryEntity toEntity(ShipmentHistory shipmentHistory);
}



