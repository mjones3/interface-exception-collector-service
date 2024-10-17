package com.arcone.biopro.distribution.shipping.application.mapper;

import com.arcone.biopro.distribution.shipping.application.dto.ReasonDTO;
import com.arcone.biopro.distribution.shipping.domain.model.Reason;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReasonDomainMapper {

    public ReasonDTO mapToDTO(Reason reason) {
        return ReasonDTO
            .builder()
            .id(reason.getId())
            .type(reason.getType())
            .reasonKey(reason.getReasonKey())
            .requireComments(reason.isRequireComments())
            .orderNumber(reason.getOrderNumber())
            .active(reason.isActive())
            .build();
    }

    public Mono<ReasonDTO> flatMapToDto(final Reason reason) {
        return Mono.just(mapToDTO(reason));
    }
}
