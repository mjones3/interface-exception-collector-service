package com.arcone.biopro.distribution.receiving.infrastructure.mapper;

import com.arcone.biopro.distribution.receiving.domain.model.BarcodeTranslation;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.BarcodeTranslationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BarcodeTranslationEntityMapper {

    BarcodeTranslation toDomain(BarcodeTranslationEntity entity);

}


