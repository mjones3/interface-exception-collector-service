package com.arcone.biopro.distribution.orderservice.infrastructure.mapper;

import reactor.core.publisher.Mono;

public interface PersistenceMapper<DOMAIN, ENTITY> {

    ENTITY mapToEntity(final DOMAIN domain);
    default Mono<ENTITY> flatMapToEntity(final DOMAIN domain) {
        return Mono.just(mapToEntity(domain));
    }

    DOMAIN mapToDomain(final ENTITY entity);
    default Mono<DOMAIN> flatMapToDomain(final ENTITY entity) {
        return Mono.just(mapToDomain(entity));
    }

}
