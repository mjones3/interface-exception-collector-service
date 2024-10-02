package com.arcone.biopro.distribution.shipping.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.shipping.adapter.in.web.controller.UnitNumberWithCheckDigitController;
import com.arcone.biopro.distribution.shipping.application.mapper.UnitNumberWithCheckDigitMapper;
import com.arcone.biopro.distribution.shipping.domain.model.vo.UnitNumberWithCheckDigit;
import com.arcone.biopro.distribution.shipping.domain.service.UnitNumberWithCheckDigitService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;

@SpringJUnitConfig(classes = { UnitNumberWithCheckDigitController.class, UnitNumberWithCheckDigitMapper.class })
class UnitNumberWithCheckDigitControllerTest {

    private static final String SAMPLE_UNIT_NUMBER = "W036824884023";
    private static final String SAMPLE_UNIT_NUMBER_CHECK_DIGIT = "Q";

    @Autowired
    UnitNumberWithCheckDigitController unitNumberWithCheckDigitController;

    @Autowired
    UnitNumberWithCheckDigitMapper unitNumberWithCheckDigitMapper;

    @MockBean
    UnitNumberWithCheckDigitService unitNumberWithCheckDigitService;

    @ParameterizedTest
    @ValueSource(strings = { SAMPLE_UNIT_NUMBER_CHECK_DIGIT, "A", "Z" })
    void verifyCheckDigitValid(String inputCheckDigit) {
        var unitNumberWithCheckDigit = new UnitNumberWithCheckDigit(SAMPLE_UNIT_NUMBER, inputCheckDigit);

        given(this.unitNumberWithCheckDigitService.verifyCheckDigit(SAMPLE_UNIT_NUMBER, inputCheckDigit))
            .willReturn(Mono.just(unitNumberWithCheckDigit));

        // Act
        var response = requireNonNull(this.unitNumberWithCheckDigitController.verifyCheckDigit(SAMPLE_UNIT_NUMBER, inputCheckDigit).block());

        // Assert
        if (response.isValid()) {
            assertNull(response.message());
        } else {
            assertNotNull(response.message());
        }
    }

}
