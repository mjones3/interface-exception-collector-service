package com.arcone.biopro.distribution.irradiation.unit.application.usecase;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.CheckDigitResponseDTO;
import com.arcone.biopro.distribution.irradiation.application.usecase.CheckDigitUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;

@ExtendWith(MockitoExtension.class)
class CheckDigitUseCaseTest {

    @InjectMocks
    private CheckDigitUseCase checkDigitUseCase;

    @BeforeEach
    void setUp() {
        checkDigitUseCase = new CheckDigitUseCase();
    }

    @Test
    void checkDigit_ValidCheckDigit_ReturnsTrue() throws Exception {
        String unitNumber = "W777725001001";
        String checkDigit = calculateCheckDigit(unitNumber);
        Mono<CheckDigitResponseDTO> result = checkDigitUseCase.checkDigit(unitNumber, checkDigit);
        StepVerifier.create(result)
            .expectNextMatches(response -> response.isValid())
            .verifyComplete();
    }

    @Test
    void checkDigit_InvalidCheckDigit_ReturnsFalse() {
        String unitNumber = "W777725001001";
        String checkDigit = "X";
        Mono<CheckDigitResponseDTO> result = checkDigitUseCase.checkDigit(unitNumber, checkDigit);
        StepVerifier.create(result)
            .expectNextMatches(response -> !response.isValid())
            .verifyComplete();
    }

    @Test
    void checkDigit_EmptyUnitNumber_ReturnsResult() {
        String unitNumber = "";
        String checkDigit = "0";
        Mono<CheckDigitResponseDTO> result = checkDigitUseCase.checkDigit(unitNumber, checkDigit);
        StepVerifier.create(result)
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void checkDigit_AlphanumericUnitNumber_ReturnsResult() throws Exception {
        String unitNumber = "ABC123XYZ";
        String correctCheckDigit = calculateCheckDigit(unitNumber);
        Mono<CheckDigitResponseDTO> result = checkDigitUseCase.checkDigit(unitNumber, correctCheckDigit);
        StepVerifier.create(result)
            .expectNextMatches(response -> response.isValid())
            .verifyComplete();
    }

    /**
     * Helper method to calculate the check digit using reflection to access the private method
     */
    private String calculateCheckDigit(String unitNumber) throws Exception {
        Method method = CheckDigitUseCase.class.getDeclaredMethod("calculateDigitCheck", String.class);
        method.setAccessible(true);
        return (String) method.invoke(checkDigitUseCase, unitNumber);
    }
}
