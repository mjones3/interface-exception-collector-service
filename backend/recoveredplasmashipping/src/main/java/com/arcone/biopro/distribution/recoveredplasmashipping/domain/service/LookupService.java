package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.LookupOutput;
import reactor.core.publisher.Flux;

public interface LookupService {

    Flux<LookupOutput> findAllByType(String type);

}
