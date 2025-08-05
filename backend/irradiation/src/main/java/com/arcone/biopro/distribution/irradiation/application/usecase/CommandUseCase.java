package com.arcone.biopro.distribution.irradiation.application.usecase;

import reactor.core.publisher.Mono;

/**
 * Marker interface for command use cases that modify state.
 * Commands represent write operations in CQRS pattern.
 *
 * @param <I> Input command type
 * @param <O> Output result type
 */
public interface CommandUseCase<I, O> {
    Mono<O> execute(I command);
}