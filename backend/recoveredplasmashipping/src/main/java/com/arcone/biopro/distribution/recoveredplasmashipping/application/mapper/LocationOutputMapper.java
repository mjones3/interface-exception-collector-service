package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;


import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.LocationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.LocationProperty;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring" )
public interface LocationOutputMapper {

    @Mapping(source = "locationProperties" , target = "properties")
    LocationOutput toLocationOutput(Location location);

    default Map<String, String> properties(List<LocationProperty> locationProperties) {
        return Optional.ofNullable(locationProperties)
            .map(list -> locationProperties.stream()
                .collect(Collectors.toMap(LocationProperty::getPropertyKey, LocationProperty::getPropertyValue)))
            .orElse(new HashMap<>()) ;
    }

}
