package com.arcone.biopro.distribution.orderservice.application.mapper;

import com.arcone.biopro.distribution.orderservice.adapter.in.web.dto.LookupDTO;
import com.arcone.biopro.distribution.orderservice.domain.model.Lookup;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.LookupId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LookupMapper {

    public LookupDTO mapToDTO(final Lookup lookup) {
        return LookupDTO.builder()
            .type(lookup.getId().getType())
            .optionValue(lookup.getId().getOptionValue())
            .descriptionKey(lookup.getDescriptionKey())
            .orderNumber(lookup.getOrderNumber())
            .active(lookup.isActive())
            .build();
    }

    public Lookup mapToDomain(final LookupDTO lookupDTO) {
        return new Lookup(
            new LookupId(lookupDTO.type(), lookupDTO.optionValue()),
            lookupDTO.descriptionKey(),
            lookupDTO.orderNumber(),
            lookupDTO.active()
        );
    }

}
