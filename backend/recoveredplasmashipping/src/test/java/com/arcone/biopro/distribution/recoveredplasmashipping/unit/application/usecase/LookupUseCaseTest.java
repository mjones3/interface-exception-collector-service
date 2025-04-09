package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.LookupOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.LookupOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.LookupUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Lookup;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.LookupEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = { LookupUseCase.class })
class LookupUseCaseTest {

    @Autowired
    LookupUseCase lookupUseCase;
    @MockBean
    LookupRepository lookupRepository;
    @MockBean
    LookupOutputMapper lookupOutputMapper;

    @Test
    void shouldFindAllByType() {
        var lookup = Lookup.fromRepository(
            LookupEntity.builder()
                .id(1L)
                .type("type")
                .optionValue("optionValue")
                .descriptionKey("descriptionKey")
                .orderNumber(1)
                .active(true)
                .build()
        );
        var lookupOutput = LookupOutput.builder()
            .id(1L)
            .type("type")
            .optionValue("optionValue")
            .descriptionKey("descriptionKey")
            .orderNumber(1)
            .active(true)
            .build();

        when(lookupRepository.findAllByType(eq("type"))).thenReturn(Flux.just(lookup));
        when(lookupOutputMapper.mapToOutput(eq(lookup))).thenReturn(lookupOutput);

        var result = lookupUseCase.findAllByType("type");

        StepVerifier.create(result)
            .expectNext(lookupOutput)
            .verifyComplete();
    }

    @Test
    void shouldNotFindAllByType() {
        when(lookupRepository.findAllByType(anyString())).thenReturn(Flux.empty());

        var result = lookupUseCase.findAllByType("type");

        StepVerifier.create(result).verifyError();
    }

}
