package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CustomerOutput;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {

    Flux<CustomerOutput> findAll();
    Mono<CustomerOutput> findById(Long id);
    Mono<CustomerOutput> findByCode(String code);

}
