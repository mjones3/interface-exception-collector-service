package com.arcone.biopro.distribution.orderservice.domain.model;

import com.arcone.biopro.distribution.orderservice.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.orderservice.domain.repository.LookupRepository;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Mono;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;

@Getter
@EqualsAndHashCode
@ToString
public class Lookup implements Validatable {

    private LookupId id;
    private String descriptionKey;
    private int orderNumber;
    private boolean active;

    public Lookup(LookupId id, String descriptionKey, int orderNumber, boolean active) {
        this.id = id;
        this.descriptionKey = descriptionKey;
        this.orderNumber = orderNumber;
        this.active = active;

        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (this.id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        if (this.descriptionKey == null || this.descriptionKey.isBlank()) {
            throw new IllegalArgumentException("descriptionKey cannot be null or blank");
        }
    }

    public Mono<Boolean> exists(final LookupRepository lookupRepository) {
        return ofNullable(this.id)
            .map(id -> lookupRepository.existsById(id, TRUE))
            .orElseGet(() -> Mono.just(FALSE));
    }

    public Mono<Lookup> delete(final LookupRepository lookupRepository) {
        this.active = false;
        return lookupRepository.update(this);
    }

}
