package com.arcone.biopro.distribution.orderservice.unit.application.usecase;

import com.arcone.biopro.distribution.orderservice.application.usecase.LookupUseCase;
import com.arcone.biopro.distribution.orderservice.domain.model.Lookup;
import com.arcone.biopro.distribution.orderservice.domain.model.LookupId;
import com.arcone.biopro.distribution.orderservice.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = { LookupUseCase.class, LookupRepository.class })
class LookupUseCaseTest {

    @Autowired
    LookupService lookupUseCase;

    @MockBean
    LookupRepository lookupRepository;

    @Test
    void testFindAllByType() {
        var type = "type";

        when(this.lookupRepository.findAllByType(anyString())).thenReturn(Flux.just(
            new Lookup(new LookupId(type, "optionValue1"), "description1", 1, true),
            new Lookup(new LookupId(type, "optionValue2"), "description2", 2, true),
            new Lookup(new LookupId(type, "optionValue3"), "description3", 3, true)
        ));

        StepVerifier.create(lookupUseCase.findAllByType(type))
            .expectNextCount(3)
            .verifyComplete();
    }

    @Test
    void testInsert() {
        var mockLookup = mock(Lookup.class);
        var realLookup = new Lookup(new LookupId("type", "optionValue"), "description", 1, true);

        when(mockLookup.exists(lookupRepository)).thenReturn(Mono.just(false));
        when(lookupRepository.insert(mockLookup)).thenReturn(Mono.just(realLookup));

        StepVerifier.create(lookupUseCase.insert(mockLookup))
            .expectNext(realLookup)
            .verifyComplete();
    }

    @Test
    void testUpdate() {
        var mockLookup = mock(Lookup.class);
        var realLookup = new Lookup(new LookupId("type", "optionValue"), "description", 1, true);

        when(mockLookup.exists(lookupRepository)).thenReturn(Mono.just(true));
        when(lookupRepository.update(mockLookup)).thenReturn(Mono.just(realLookup));

        StepVerifier.create(lookupUseCase.update(mockLookup))
            .expectNext(realLookup)
            .verifyComplete();
    }

    @Test
    void testDelete() {
        var mockLookup = mock(Lookup.class);
        var realLookup = new Lookup(new LookupId("type", "optionValue"), "description", 1, false);

        when(mockLookup.exists(lookupRepository)).thenReturn(Mono.just(true));
        when(mockLookup.delete(lookupRepository)).thenReturn(Mono.just(realLookup));

        StepVerifier.create(lookupUseCase.delete(mockLookup))
            .expectNext(realLookup)
            .verifyComplete();
    }

}
