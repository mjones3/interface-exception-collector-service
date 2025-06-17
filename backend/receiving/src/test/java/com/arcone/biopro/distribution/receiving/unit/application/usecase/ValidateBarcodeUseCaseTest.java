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
import com.arcone.biopro.distribution.receiving.domain.model.BarcodePattern;
import com.arcone.biopro.distribution.receiving.domain.service.ConfigurationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
        var pattern = Mockito.mock(BarcodePattern.class);

        when(pattern.getPattern()).thenReturn("(\\&\\>)(\\d{3}\\d{3}\\d{2}\\d{2})");
        when(pattern.getMatchGroups()).thenReturn(2);

        when(configurationService.findByParseType(any())).thenReturn(Mono.just(pattern));


        ValidateBarcodeCommandInput input = new ValidateBarcodeCommandInput("&>0260422359", "BARCODE_EXPIRATION_DATE", "FROZEN");

        ValidationResultOutput validationResultOutput = ValidationResultOutput.builder()
            .valid(true)
            .message("Valid barcode")
            .build();


        when(validationResultOutputMapper.toOutput(any())).thenReturn(validationResultOutput);

        // Act
        StepVerifier.create(validateBarcodeUseCase.validateBarcode(input))
            // Assert
            .assertNext(response -> {
                Assertions.assertNotNull(response);
                Assertions.assertEquals(response.data().message(),"Valid barcode");
                Assertions.assertTrue(response.data().valid());
            })
            .verifyComplete();

    }

    @Test
    void validateBarcode_WhenInvalidBarcode_ShouldReturnWarningOutput() {
        // Arrange

        var pattern = Mockito.mock(BarcodePattern.class);

        when(pattern.getPattern()).thenReturn("(\\=\\%)(\\w{4})");
        when(pattern.getMatchGroups()).thenReturn(2);

        when(configurationService.findByParseType(any())).thenReturn(Mono.just(pattern));

        ValidateBarcodeCommandInput input = new ValidateBarcodeCommandInput("invalid", "BARCODE_UNIT_NUMBER", "FROZEN");
        UseCaseNotificationOutput expectedNotification = UseCaseNotificationOutput.builder()
            .useCaseMessage(
                UseCaseMessage.builder()
                    .message("Invalid Unit Number")
                    .code(6)
                    .type(UseCaseNotificationType.WARN)
                    .build())
            .build();

        // Act
        StepVerifier.create(validateBarcodeUseCase.validateBarcode(input))
            // Assert
            .assertNext(response -> {
                Assertions.assertNotNull(response);
                Assertions.assertEquals(expectedNotification, response.notifications().getFirst());
            })
            .verifyComplete();


    }

    @Test
    void validateBarcode_WhenSystemError_ShouldReturnErrorOutput() {
        // Arrange

            ValidateBarcodeCommandInput input = new ValidateBarcodeCommandInput("123456", "BARCODE_UNIT_NUMBER", "FROZEN");
            RuntimeException exception = new RuntimeException("System error");

            when(configurationService.findByParseType(any())).thenThrow(exception);

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
