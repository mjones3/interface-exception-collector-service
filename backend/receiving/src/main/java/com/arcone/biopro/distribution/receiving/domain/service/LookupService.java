package com.arcone.biopro.distribution.receiving.domain.service;

import com.arcone.biopro.distribution.receiving.application.dto.LookupOutput;
import reactor.core.publisher.Flux;

public interface LookupService {

    Flux<LookupOutput> findAllByType(String type);

}
