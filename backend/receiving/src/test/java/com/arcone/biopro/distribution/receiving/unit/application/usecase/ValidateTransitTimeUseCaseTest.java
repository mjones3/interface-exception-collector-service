package com.arcone.biopro.distribution.receiving.unit.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateTransitTimeCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidationResultOutput;
import com.arcone.biopro.distribution.receiving.application.mapper.ValidationResultOutputMapper;
import com.arcone.biopro.distribution.receiving.application.usecase.ValidateTransitTimeUseCase;
import com.arcone.biopro.distribution.receiving.domain.model.ProductConsequence;
import com.arcone.biopro.distribution.receiving.domain.model.TransitTimeValidator;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ValidationResult;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateTransitTimeUseCaseTest {

    @Mock
    private ProductConsequenceRepository productConsequenceRepository;

    @Mock
    private ValidationResultOutputMapper validationResultOutputMapper;

    @InjectMocks
    private ValidateTransitTimeUseCase validateTransitTimeUseCase;

    private ValidateTransitTimeCommandInput validCommandInput;
    private ValidationResult validationResult;
    private ValidationResultOutput validationResultOutput;
    private ProductConsequence productConsequence;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        validCommandInput = new ValidateTransitTimeCommandInput(
            "FROZEN",
            now,
            "UTC",
            now.plusHours(2),
            "UTC"
        );

        validationResult = ValidationResult.builder()
            .valid(true)
            .result("2")
            .build();

        validationResultOutput = new ValidationResultOutput(true, "2", null, null);
    }

    @Test
    void validateTransitTime_ValidInput_SuccessfulValidation() {
        // Arrange

        try (MockedStatic<TransitTimeValidator> utilities = Mockito.mockStatic(TransitTimeValidator.class)) {

            ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
            Mockito.when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(),any())).thenReturn(Flux.just(consequence));

            when(validationResultOutputMapper.toOutput(any(ValidationResult.class))).thenReturn(validationResultOutput);

            utilities.when(() -> TransitTimeValidator.validateTransitTime(any(), any())).thenReturn(validationResult);

            // Act & Assert
            StepVerifier.create(validateTransitTimeUseCase.validateTransitTime(validCommandInput))
                .assertNext(output -> {
                    Assertions.assertTrue(output.notifications().isEmpty());
                    Assertions.assertEquals(output.data(),validationResultOutput);
                })
                .verifyComplete();
        }


    }

    @Test
    void validateTransitTime_InvalidInput_ReturnsNotification() {
        // Arrange

        try (MockedStatic<TransitTimeValidator> utilities = Mockito.mockStatic(TransitTimeValidator.class)) {

            ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
            Mockito.when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(), any())).thenReturn(Flux.just(consequence));

            ValidationResultOutput invalidOutput = new ValidationResultOutput(false, null,
                null, "Temperature does not meet thresholds all products will be quarantined");

            when(validationResultOutputMapper.toOutput(any(ValidationResult.class)))
                .thenReturn(invalidOutput);

            validationResult =  ValidationResult.builder()
                .valid(false)
                .build();

            utilities.when(() -> TransitTimeValidator.validateTransitTime(any(), any())).thenReturn(validationResult);

            // Act & Assert
            StepVerifier.create(validateTransitTimeUseCase.validateTransitTime(validCommandInput))
                .expectNextMatches(output ->
                    !output.notifications().isEmpty() &&
                        output.notifications().get(0).useCaseMessage().code() == 6 &&
                        output.notifications().get(0).useCaseMessage().type() == UseCaseNotificationType.CAUTION &&
                        !output.data().valid())
                .verifyComplete();
        }
    }

    @Test
    void validateTransitTime_RepositoryError_ReturnsSystemError() {
        // Arrange
        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(
            any(), any()))
            .thenReturn(Flux.error(new RuntimeException("Database error")));

        // Act & Assert
        StepVerifier.create(validateTransitTimeUseCase.validateTransitTime(validCommandInput))
            .expectNextMatches(output ->
                output.notifications().size() == 1 &&
                    output.notifications().get(0).useCaseMessage().type() ==
                        UseCaseMessageType.VALIDATE_TRANSIT_TIME_SYSTEM_ERROR.getType() &&
                    output.data() == null)
            .verifyComplete();
    }

    @Test
    void validateTransitTime_InvalidCommand_ReturnsSystemError() {
        // Arrange
        ValidateTransitTimeCommandInput invalidInput = new ValidateTransitTimeCommandInput(
            null, null, null, null, null
        );

        // Act & Assert
        StepVerifier.create(validateTransitTimeUseCase.validateTransitTime(invalidInput))
            .expectNextMatches(output ->
                output.notifications().size() == 1 &&
                    output.notifications().get(0).useCaseMessage().type() ==
                        UseCaseMessageType.VALIDATE_TRANSIT_TIME_SYSTEM_ERROR.getType() &&
                    output.data() == null)
            .verifyComplete();
    }

    @Test
    void validateTransitTime_EmptyProductConsequences_ReturnsSystemError() {
        // Arrange
        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(
            any(), any()))
            .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(validateTransitTimeUseCase.validateTransitTime(validCommandInput))
            .expectNextMatches(output ->
                output.notifications().size() == 1 &&
                    output.notifications().get(0).useCaseMessage().type() ==
                        UseCaseMessageType.VALIDATE_TRANSIT_TIME_SYSTEM_ERROR.getType() &&
                    output.data() == null)
            .verifyComplete();
    }

    @Test
    void validateTransitTime_MapperError_ReturnsSystemError() {
        // Arrange

        try (MockedStatic<TransitTimeValidator> utilities = Mockito.mockStatic(TransitTimeValidator.class)) {

            ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
            Mockito.when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(), any())).thenReturn(Flux.just(consequence));


            when(validationResultOutputMapper.toOutput(any()))
                .thenThrow(new RuntimeException("Mapping error"));

            validationResult =  ValidationResult.builder()
                .valid(false)
                .build();

            utilities.when(() -> TransitTimeValidator.validateTransitTime(any(), any())).thenReturn(validationResult);

            // Act & Assert
            StepVerifier.create(validateTransitTimeUseCase.validateTransitTime(validCommandInput))
                .expectNextMatches(output ->
                    output.notifications().size() == 1 &&
                        output.notifications().get(0).useCaseMessage().type() ==
                            UseCaseMessageType.VALIDATE_TRANSIT_TIME_SYSTEM_ERROR.getType() &&
                        output.data() == null)
                .verifyComplete();

        }
    }
}

