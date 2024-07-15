package com.arcone.biopro.distribution.orderservice.infrastructure.mapper;

import com.arcone.biopro.distribution.orderservice.domain.model.Lookup;
import com.arcone.biopro.distribution.orderservice.domain.model.LookupId;
import com.arcone.biopro.distribution.orderservice.infrastructure.persistence.LookupEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LookupEntityMapper implements PersistenceMapper<Lookup, LookupEntity> {

    public LookupEntity mapToEntity(final Lookup lookup) {
        return LookupEntity.builder()
            .type(lookup.getId().type())
            .optionValue(lookup.getId().optionValue())
            .descriptionKey(lookup.getDescriptionKey())
            .orderNumber(lookup.getOrderNumber())
            .active(lookup.isActive())
            .build();
    }

    public Lookup mapToDomain(final LookupEntity lookupEntity) {
        return new Lookup(
            new LookupId(lookupEntity.getType(), lookupEntity.getOptionValue()),
            lookupEntity.getDescriptionKey(),
            lookupEntity.getOrderNumber(),
            lookupEntity.isActive()
        );
    }

}
