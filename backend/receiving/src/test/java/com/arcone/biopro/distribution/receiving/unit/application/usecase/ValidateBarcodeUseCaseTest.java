package com.arcone.biopro.distribution.receiving.unit.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateBarcodeCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidationResultOutput;
import com.arcone.biopro.distribution.receiving.application.mapper.ValidationResultOutputMapper;
import com.arcone.biopro.distribution.receiving.application.usecase.ValidateBarcodeUseCase;
import com.arcone.biopro.distribution.receiving.domain.model.BarcodeValidator;
import com.arcone.biopro.distribution.receiving.domain.model.ValidateBarcodeCommand;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ValidationResult;
import com.arcone.biopro.distribution.receiving.domain.service.ConfigurationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateBarcodeUseCaseTest {

    @Mock
    private ValidationResultOutputMapper validationResultOutputMapper;

    @Mock
    private ConfigurationService configurationService;

    @InjectMocks
    private ValidateBarcodeUseCase validateBarcodeUseCase;

    @Test
    void validateBarcode_WhenValidBarcode_ShouldReturnSuccessOutput() {
        // Arrange

        try (MockedStatic<BarcodeValidator> utilities = Mockito.mockStatic(BarcodeValidator.class)) {
            ValidateBarcodeCommandInput input = new ValidateBarcodeCommandInput("123456", "BARCODE_UNIT_NUMBER", "FROZEN");
            ValidationResult validationResult = ValidationResult.builder()
                .valid(true)
                .message("Valid barcode")
                .build();
            ValidationResultOutput validationResultOutput = ValidationResultOutput.builder()
                .valid(true)
                .message("Valid barcode")
                .build();

            utilities.when(() -> BarcodeValidator.validateBarcode(any(ValidateBarcodeCommand.class), any(ConfigurationService.class))).thenReturn(validationResult);

            when(validationResultOutputMapper.toOutput(validationResult))
                .thenReturn(validationResultOutput);

            // Act
            StepVerifier.create(validateBarcodeUseCase.validateBarcode(input))
                // Assert
                .expectNext(new UseCaseOutput<>(Collections.emptyList(), validationResultOutput, null))
                .verifyComplete();
        }


    }

    @Test
    void validateBarcode_WhenInvalidBarcode_ShouldReturnWarningOutput() {
        // Arrange
        try (MockedStatic<BarcodeValidator> utilities = Mockito.mockStatic(BarcodeValidator.class)) {

            ValidateBarcodeCommandInput input = new ValidateBarcodeCommandInput("invalid", "BARCODE_UNIT_NUMBER", "FROZEN");
            ValidationResult validationResult = ValidationResult.builder()
                .valid(false)
                .message("Invalid barcode format")
                .build();

            ValidationResultOutput validationResultOutput = new ValidationResultOutput(false, "Invalid barcode format",null,null);

            utilities.when(() -> BarcodeValidator.validateBarcode(any(ValidateBarcodeCommand.class), eq(configurationService)))
                .thenReturn(validationResult);
            when(validationResultOutputMapper.toOutput(validationResult))
                .thenReturn(validationResultOutput);

            UseCaseNotificationOutput expectedNotification = UseCaseNotificationOutput.builder()
                .useCaseMessage(
                    UseCaseMessage.builder()
                        .message("Invalid barcode format")
                        .code(6)
                        .type(UseCaseNotificationType.WARN)
                        .build())
                .build();

            // Act
            StepVerifier.create(validateBarcodeUseCase.validateBarcode(input))
                // Assert
                .expectNext(new UseCaseOutput<>(List.of(expectedNotification), validationResultOutput, null))
                .verifyComplete();
        }

    }

    @Test
    void validateBarcode_WhenSystemError_ShouldReturnErrorOutput() {
        // Arrange
        try (MockedStatic<BarcodeValidator> utilities = Mockito.mockStatic(BarcodeValidator.class)) {

            ValidateBarcodeCommandInput input = new ValidateBarcodeCommandInput("123456", "BARCODE_UNIT_NUMBER", "FROZEN");
            RuntimeException exception = new RuntimeException("System error");

            utilities.when(() -> BarcodeValidator.validateBarcode(any(ValidateBarcodeCommand.class), eq(configurationService)))
                .thenThrow(exception);

            UseCaseNotificationOutput expectedNotification = UseCaseNotificationOutput.builder()
                .useCaseMessage(
                    UseCaseMessage.builder()
                        .message(UseCaseMessageType.VALIDATE_BARCODE_SYSTEM_ERROR.getMessage())
                        .code(UseCaseMessageType.VALIDATE_BARCODE_SYSTEM_ERROR.getCode())
                        .type(UseCaseMessageType.VALIDATE_BARCODE_SYSTEM_ERROR.getType())
                        .build())
                .build();

            // Act
            StepVerifier.create(validateBarcodeUseCase.validateBarcode(input))
                // Assert
                .expectNext(new UseCaseOutput<>(List.of(expectedNotification), null, null))
                .verifyComplete();
        }

    }

    @Test
    void validateBarcode_WhenNullInput_ShouldReturnErrorOutput() {
        // Arrange
        ValidateBarcodeCommandInput input = new ValidateBarcodeCommandInput(null, null, null);

        UseCaseNotificationOutput expectedNotification = UseCaseNotificationOutput.builder()
            .useCaseMessage(
                UseCaseMessage.builder()
                    .message(UseCaseMessageType.VALIDATE_BARCODE_SYSTEM_ERROR.getMessage())
                    .code(UseCaseMessageType.VALIDATE_BARCODE_SYSTEM_ERROR.getCode())
                    .type(UseCaseMessageType.VALIDATE_BARCODE_SYSTEM_ERROR.getType())
                    .build())
            .build();

        // Act
        StepVerifier.create(validateBarcodeUseCase.validateBarcode(input))
            // Assert
            .expectNext(new UseCaseOutput<>(List.of(expectedNotification), null, null))
            .verifyComplete();
    }
}
