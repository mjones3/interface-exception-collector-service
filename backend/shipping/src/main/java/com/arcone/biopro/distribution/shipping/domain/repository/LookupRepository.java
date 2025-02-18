package com.arcone.biopro.distribution.shipping.domain.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface LookupRepository {

    Mono<String> findFirstConfigAsString(String type);

    Mono<Boolean> findFirstConfigAsBoolean(String type);

    Mono<Integer> findFirstConfigAsInteger(String type);

    Mono<Long> findFirstConfigAsLong(String type);

    Mono<BigDecimal> findFirstConfigAsBigDecimal(String type);

    Flux<String> findAllConfigsAsStrings(String type);

    Flux<Integer> findAllConfigsAsIntegers(String type);

    Flux<Long> findAllConfigsAsLongs(String type);

    Flux<BigDecimal> findAllConfigsAsBigDecimals(String type);

}
