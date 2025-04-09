package com.arcone.biopro.distribution.recoveredplasmashipping.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.controller.LookupController;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.LookupDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.LookupDTOMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.LookupOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.LookupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = LookupController.class)
class LookupControllerTest {

    @Autowired
    LookupController lookupController;
    @MockBean
    LookupService lookupService;
    @MockBean
    LookupDTOMapper lookupDTOMapper;

    @Test
    void shouldFindAllLookupsByType() {
        var lookupOutput = LookupOutput.builder()
            .id(1L)
            .type("type")
            .optionValue("optionValue")
            .descriptionKey("descriptionKey")
            .orderNumber(1)
            .active(true)
            .build();

        var lookupDTO = LookupDTO.builder()
            .id(1L)
            .type("type")
            .optionValue("optionValue")
            .descriptionKey("descriptionKey")
            .orderNumber(1)
            .active(true)
            .build();

        when(lookupService.findAllByType(eq("type")))
            .thenReturn(Flux.just(lookupOutput));

        when(lookupDTOMapper.mapToDTO(eq(lookupOutput)))
            .thenReturn(lookupDTO);

        var result = this.lookupController.findAllLookupsByType("type");

        StepVerifier.create(result)
            .expectNext(lookupDTO)
            .verifyComplete();
    }

}
