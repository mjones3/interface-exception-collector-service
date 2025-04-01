package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Lookup;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.LookupEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LookupEntityMapper {

    default Lookup mapToDomain(final LookupEntity entity) {
        return Lookup.fromRepository(entity);
    }

    LookupEntity mapToEntity(final Lookup lookup);

}
