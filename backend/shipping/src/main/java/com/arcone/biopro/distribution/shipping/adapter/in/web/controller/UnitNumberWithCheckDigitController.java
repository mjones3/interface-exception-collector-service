package com.arcone.biopro.distribution.shipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.UnitNumberWithCheckDigitDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.UnitNumberWithCheckDigitMapper;
import com.arcone.biopro.distribution.shipping.domain.service.UnitNumberWithCheckDigitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UnitNumberWithCheckDigitController {

    private final UnitNumberWithCheckDigitService unitNumberWithCheckDigitService;
    private final UnitNumberWithCheckDigitMapper unitNumberWithCheckDigitMapper;

    @QueryMapping
    public Mono<UnitNumberWithCheckDigitDTO> verifyCheckDigit(
        @Argument String unitNumber,
        @Argument String checkDigit
    ) {
        return unitNumberWithCheckDigitService.verifyCheckDigit(unitNumber, checkDigit)
            .map(unitNumberWithCheckDigitMapper::mapToDTO)
            .flatMap(Mono::just);
    }

}
