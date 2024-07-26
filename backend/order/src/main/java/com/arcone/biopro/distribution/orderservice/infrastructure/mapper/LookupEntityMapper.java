package com.arcone.biopro.distribution.orderservice.infrastructure.mapper;

import com.arcone.biopro.distribution.orderservice.domain.model.Lookup;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.orderservice.infrastructure.persistence.LookupEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LookupEntityMapper {

    public LookupEntity mapToEntity(final Lookup lookup) {
        return LookupEntity.builder()
            .type(lookup.getId().getType())
            .optionValue(lookup.getId().getOptionValue())
            .descriptionKey(lookup.getDescriptionKey())
            .orderNumber(lookup.getOrderNumber())
            .active(lookup.isActive())
            .build();
    }

    public Mono<LookupEntity> flatMapToEntity(final Lookup lookup) {
        return Mono.just(mapToEntity(lookup));
    }

    public Lookup mapToDomain(final LookupEntity lookupEntity) {
        return new Lookup(
            new LookupId(lookupEntity.getType(), lookupEntity.getOptionValue()),
            lookupEntity.getDescriptionKey(),
            lookupEntity.getOrderNumber(),
            lookupEntity.isActive()
        );
    }

    public Mono<Lookup> flatMapToDomain(final LookupEntity lookupEntity) {
        return Mono.just(mapToDomain(lookupEntity));
    }

}
