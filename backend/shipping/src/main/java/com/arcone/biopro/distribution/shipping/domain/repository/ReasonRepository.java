package com.arcone.biopro.distribution.shipping.domain.repository;

import com.arcone.biopro.distribution.shipping.domain.model.Reason;
import reactor.core.publisher.Flux;

public interface ReasonRepository {

    Flux<Reason> findAllByType(String type);


}
