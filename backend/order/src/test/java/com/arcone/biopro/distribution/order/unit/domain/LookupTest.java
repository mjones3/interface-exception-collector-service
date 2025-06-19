package com.arcone.biopro.distribution.order.unit.domain;

import com.arcone.biopro.distribution.order.domain.model.Lookup;
import com.arcone.biopro.distribution.order.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.order.domain.repository.LookupRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = { LookupRepository.class })
class LookupTest {

    @MockBean
    LookupRepository lookupRepository;

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Lookup(null, null, 1, true));
        assertThrows(IllegalArgumentException.class, () -> new Lookup(null, "description", 1, true));
        assertThrows(IllegalArgumentException.class, () -> new Lookup(new LookupId("", "optionValue"), "description", 1, true));
        assertThrows(IllegalArgumentException.class, () -> new Lookup(new LookupId("type", ""), "description", 1, true));
        assertThrows(IllegalArgumentException.class, () -> new Lookup(new LookupId("type", "optionValue"), "", 1, true));
        assertDoesNotThrow(() -> new Lookup(new LookupId("type", "optionValue"), "description", 1, true));
    }

    @Test
    void testExists() {
        var lookup = new Lookup(new LookupId("type", "optionValue"), "description", 1, true);

        when(lookupRepository.existsById(lookup.getId(), TRUE)).thenReturn(Mono.just(true));

        StepVerifier.create(lookup.exists(lookupRepository))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void testDelete() {
        var lookup = new Lookup(new LookupId("type", "optionValue"), "description", 1, true);

        var captor = ArgumentCaptor.forClass(Lookup.class);
        when(lookupRepository.update(captor.capture())).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(lookup.delete(lookupRepository))
            .expectNext(captor.getValue())
            .verifyComplete();
    }

}
