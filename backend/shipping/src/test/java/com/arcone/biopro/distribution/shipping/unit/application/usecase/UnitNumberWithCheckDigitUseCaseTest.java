package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.mapper.UnitNumberWithCheckDigitMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.UnitNumberWithCheckDigitUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = { UnitNumberWithCheckDigitUseCase.class })
class UnitNumberWithCheckDigitUseCaseTest {

    private static final String SAMPLE_UNIT_NUMBER = "W036824944450";
    private static final String SAMPLE_UNIT_NUMBER_CHECK_DIGIT = "F";

    @Autowired
    UnitNumberWithCheckDigitUseCase unitNumberWithCheckDigitUseCase;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    UnitNumberWithCheckDigitMapper unitNumberWithCheckDigitMapper;

    @Test
    void shouldVerifyCheckDigitIfValid() {
        var result = unitNumberWithCheckDigitUseCase.verifyCheckDigit(SAMPLE_UNIT_NUMBER, SAMPLE_UNIT_NUMBER_CHECK_DIGIT);

        StepVerifier.create(result)
            .consumeNextWith(detail -> {
                assertEquals(HttpStatus.OK, detail.ruleCode());
                assertNotNull(detail.results().get("data").getFirst());
            })
            .verifyComplete();
    }

    @Test
    void shouldVerifyCheckDigitIfInvalid() {
        var result = unitNumberWithCheckDigitUseCase.verifyCheckDigit(SAMPLE_UNIT_NUMBER, "A");

        StepVerifier.create(result)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertNull(detail.results());
                assertEquals("INVALID_CHECK_DIGIT", firstNotification.name());
                assertEquals("Check Digit is Invalid", firstNotification.message());
                assertEquals(NotificationType.WARN.name(), firstNotification.notificationType());
            })
            .verifyComplete();
    }

}
