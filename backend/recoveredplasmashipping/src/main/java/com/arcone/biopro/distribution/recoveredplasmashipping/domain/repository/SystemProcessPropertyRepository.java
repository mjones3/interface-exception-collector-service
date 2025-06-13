package com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.SystemProcessProperty;
import reactor.core.publisher.Flux;

public interface SystemProcessPropertyRepository {

    Flux<SystemProcessProperty> findAllByType(String type);
}
