package com.arcone.biopro.distribution.shipping.infrastructure.mapper;

import com.arcone.biopro.distribution.shipping.domain.model.Reason;
import com.arcone.biopro.distribution.shipping.infrastructure.persistence.ReasonEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReasonMapper {

    public Reason toDomain(ReasonEntity reasonEntity) {
        return new Reason(reasonEntity.getId(), reasonEntity.getType(), reasonEntity.getReasonKey()
            , reasonEntity.isRequireComments(), reasonEntity.getOrderNumber()
            , reasonEntity.isActive());
    }

    public Mono<Reason> flatMapToDomain(final ReasonEntity reasonEntity) {
        return Mono.just(toDomain(reasonEntity));
    }

}
