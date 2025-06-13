package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.LocationEntity;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.LocationPropertyEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;

@Component
public class LocationEntityMapper {

    public Location toDomain(LocationEntity locationEntity, List<LocationPropertyEntity> locationProperties) {

        var locationDomain = new Location(locationEntity.getId(), locationEntity.getName()
            , locationEntity.getCode(), locationEntity.getExternalId()
            , locationEntity.getAddressLine1(), locationEntity.getAddressLine2()
            , locationEntity.getPostalCode(), locationEntity.getCity(), locationEntity.getState());

        ofNullable(locationProperties)
            .filter(list -> !list.isEmpty())
            .orElseGet(Collections::emptyList)
            .forEach(locationProperty -> locationDomain.addProperty(locationProperty.getPropertyKey(), locationProperty.getPropertyValue()));

        return locationDomain;
    }
}
