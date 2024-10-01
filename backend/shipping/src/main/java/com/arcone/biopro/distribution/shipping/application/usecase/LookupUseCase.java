package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.application.exception.DomainDoesNotExistException;
import com.arcone.biopro.distribution.shipping.application.exception.DomainExistsException;
import com.arcone.biopro.distribution.shipping.domain.model.Lookup;
import com.arcone.biopro.distribution.shipping.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.shipping.domain.service.LookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LookupUseCase implements LookupService {

    private final LookupRepository lookupRepository;

    @Override
    public Flux<Lookup> findAllByType(String type) {
        return this.lookupRepository.findAllByType(type);
    }

    @Override
    public Mono<Lookup> insert(Lookup lookup) {
        return lookup.exists(this.lookupRepository)
            .flatMap(exists -> exists
                ? Mono.error(() -> new DomainExistsException(String.valueOf(lookup.getId())))
                : this.lookupRepository.insert(lookup)
            );
    }

    @Override
    public Mono<Lookup> update(Lookup lookup) {
        return lookup.exists(this.lookupRepository)
            .flatMap(exists -> exists
                ? this.lookupRepository.update(lookup)
                : Mono.error(() -> new DomainDoesNotExistException(String.valueOf(lookup.getId())))
            );
    }

    public Mono<Lookup> delete(Lookup lookup) {
        return lookup.exists(this.lookupRepository)
            .flatMap(exists -> exists
                ? lookup.delete(this.lookupRepository)
                : Mono.error(() -> new DomainDoesNotExistException(String.valueOf(lookup.getId())))
            );
    }

}
