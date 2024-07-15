package com.arcone.biopro.distribution.orderservice.domain.model;

import com.arcone.biopro.distribution.orderservice.domain.repository.LookupRepository;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Mono;

import static java.util.Optional.ofNullable;

@Getter
@EqualsAndHashCode
@ToString
public class Lookup {

    private LookupId id;
    private String descriptionKey;
    private int orderNumber;
    private boolean active;

    public Lookup(LookupId id, String descriptionKey, int orderNumber, boolean active) {
        this.id = ofNullable(id)
            .filter(LookupId::isValid)
            .orElseThrow(() -> new IllegalArgumentException("ID cannot be null"));

        this.descriptionKey = ofNullable(descriptionKey)
            .filter(p -> !p.isBlank())
            .orElseThrow(() -> new IllegalArgumentException("descriptionKey cannot be null"));

        this.orderNumber = orderNumber;
        this.active = active;
    }

    public Mono<Boolean> exists(final LookupRepository lookupRepository) {
        return lookupRepository.existsById(this.id, null);
    }

    public Mono<Lookup> delete(final LookupRepository lookupRepository) {
        this.active = false;
        return lookupRepository.update(this);
    }

}
