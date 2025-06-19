package com.arcone.biopro.distribution.receiving.unit.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateTemperatureCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidationResultOutput;
import com.arcone.biopro.distribution.receiving.application.mapper.ValidationResultOutputMapper;
import com.arcone.biopro.distribution.receiving.application.usecase.ValidateTemperatureUseCase;
import com.arcone.biopro.distribution.receiving.domain.model.ProductConsequence;
import com.arcone.biopro.distribution.receiving.domain.model.TemperatureValidator;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ValidationResult;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateTemperatureUseCaseTest {

    @Mock
    private ProductConsequenceRepository productConsequenceRepository;

    @Mock
    private ValidationResultOutputMapper validationResultOutputMapper;

    private ValidateTemperatureUseCase validateTemperatureUseCase;

    @BeforeEach
    void setUp() {
        validateTemperatureUseCase = new ValidateTemperatureUseCase(
            productConsequenceRepository,
            validationResultOutputMapper
        );
    }

   @Test
    void validateTemperature_WhenValidationSucceeds_ReturnsSuccessOutput() {

       ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
       Mockito.when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(),any())).thenReturn(Flux.just(consequence));

        try (MockedStatic<TemperatureValidator> utilities = Mockito.mockStatic(TemperatureValidator.class)) {

            // Arrange
            ValidateTemperatureCommandInput input = new ValidateTemperatureCommandInput(
                new BigDecimal("-18.0"),
                "FROZEN"
            );

            ValidationResult validationResult = ValidationResult.builder()
                .valid(true)
                .build();

            ValidationResultOutput validationResultOutput = new ValidationResultOutput(true, null,null, null);


            when(validationResultOutputMapper.toOutput(validationResult))
                .thenReturn(validationResultOutput);

            utilities.when(() -> TemperatureValidator.validateTemperature(any(), any())).thenReturn(validationResult);

            // Act & Assert
            StepVerifier.create(validateTemperatureUseCase.validateTemperature(input))
                .assertNext(output -> {
                        Assertions.assertTrue(output.notifications().isEmpty());
                    Assertions.assertEquals(output.data(),validationResultOutput);
                    })
                .verifyComplete();
        }
    }

   @Test
    void validateTemperature_WhenValidationFails_ReturnsCautionOutput() {

       ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
       Mockito.when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(),any())).thenReturn(Flux.just(consequence));

       try (MockedStatic<TemperatureValidator> utilities = Mockito.mockStatic(TemperatureValidator.class)) {

        // Arrange
        ValidateTemperatureCommandInput input = new ValidateTemperatureCommandInput(
            new BigDecimal("-15.0"),
            "FROZEN"
        );

        ValidationResult validationResult = ValidationResult.builder()
            .valid(false)
            .message("Temperature does not meet thresholds")
            .build();

        ValidationResultOutput validationResultOutput = new ValidationResultOutput(false, "Temperature does not meet thresholds","", "");

        when(validationResultOutputMapper.toOutput(validationResult)).thenReturn(validationResultOutput);

           utilities.when(() -> TemperatureValidator.validateTemperature(any(), any())).thenReturn(validationResult);

           // Act & Assert
           StepVerifier.create(validateTemperatureUseCase.validateTemperature(input))
               .expectNextMatches(output ->
                   output.notifications().size() == 1 &&
                       output.notifications().get(0).useCaseMessage().code() == 5 &&
                       output.notifications().get(0).useCaseMessage().type() == UseCaseNotificationType.CAUTION &&
                       output.data().equals(validationResultOutput)
               )
               .verifyComplete();
       }
    }

    @Test
    void validateTemperature_WhenExceptionOccurs_ReturnsSystemError() {

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(),any())).thenReturn(Flux.just(consequence));

        try (MockedStatic<TemperatureValidator> utilities = Mockito.mockStatic(TemperatureValidator.class)) {

            // Arrange
            ValidateTemperatureCommandInput input = new ValidateTemperatureCommandInput(
                new BigDecimal("-15.0"),
                "FROZEN"
            );

            utilities.when(() -> TemperatureValidator.validateTemperature(any(), any())).thenThrow(new RuntimeException("Unexpected error"));

            // Act & Assert
            StepVerifier.create(validateTemperatureUseCase.validateTemperature(input))
                .expectNextMatches(output ->
                    output.notifications().size() == 1 &&
                        output.notifications().get(0).useCaseMessage().message()
                            .equals(UseCaseMessageType.VALIDATE_TEMPERATURE_SYSTEM_ERROR.getMessage()) &&
                        output.notifications().get(0).useCaseMessage().code()
                            .equals(UseCaseMessageType.VALIDATE_TEMPERATURE_SYSTEM_ERROR.getCode()) &&
                        output.notifications().get(0).useCaseMessage().type()
                            .equals(UseCaseMessageType.VALIDATE_TEMPERATURE_SYSTEM_ERROR.getType()) &&
                        output.data() == null
                )
                .verifyComplete();

        }

    }

    @Test
    void validateTemperature_WhenNullInput_ReturnsSystemError() {
        // Act & Assert
        StepVerifier.create(validateTemperatureUseCase.validateTemperature(null))
            .expectNextMatches(output ->
                output.notifications().size() == 1 &&
                    output.notifications().get(0).useCaseMessage().type()
                        .equals(UseCaseMessageType.VALIDATE_TEMPERATURE_SYSTEM_ERROR.getType()) &&
                    output.data() == null
            )
            .verifyComplete();
    }
}
