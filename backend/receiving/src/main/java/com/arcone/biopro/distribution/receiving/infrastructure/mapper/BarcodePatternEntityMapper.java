package com.arcone.biopro.distribution.receiving.infrastructure.mapper;

import com.arcone.biopro.distribution.receiving.domain.model.BarcodePattern;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.BarcodePatternEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BarcodePatternEntityMapper {

    BarcodePattern toDomain(BarcodePatternEntity entity);

}


