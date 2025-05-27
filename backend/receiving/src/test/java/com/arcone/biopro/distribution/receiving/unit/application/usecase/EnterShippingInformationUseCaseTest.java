package com.arcone.biopro.distribution.receiving.unit.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.EnterShippingInformationCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ShippingInformationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.mapper.ShippingInformationOutputMapper;
import com.arcone.biopro.distribution.receiving.application.usecase.EnterShippingInformationUseCase;
import com.arcone.biopro.distribution.receiving.domain.model.ShippingInformation;
import com.arcone.biopro.distribution.receiving.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnterShippingInformationUseCaseTest {

    @Mock
    private ProductConsequenceRepository productConsequenceRepository;

    @Mock
    private LookupRepository lookupRepository;

    @Mock
    private ShippingInformationOutputMapper shippingInformationOutputMapper;

    @InjectMocks
    private EnterShippingInformationUseCase enterShippingInformationUseCase;

    @Test
    void shouldSuccessfullyEnterShippingInformation() {
        // Arrange
        EnterShippingInformationCommandInput input = new EnterShippingInformationCommandInput(
            "CATEGORY_1",
            "EMP123",
            "LOC456"
        );

        ShippingInformation shippingInformation = mock(ShippingInformation.class);
        ShippingInformationOutput expectedOutput = mock(ShippingInformationOutput.class);

        when(ShippingInformation.fromNewImportBatch(any(), any(), any())).thenReturn(shippingInformation);
        when(shippingInformationOutputMapper.mapToOutput(shippingInformation)).thenReturn(expectedOutput);

        // Act
        StepVerifier.create(enterShippingInformationUseCase.enterShippingInformation(input))
            // Assert
            .expectNextMatches(output -> {
                assertNotNull(output);
                assertEquals(expectedOutput, output.data());
                assertEquals(1, output.notifications().size());

                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(UseCaseMessageType.ENTER_SHIPPING_INFORMATION_SUCCESS.getMessage(),
                    notification.useCaseMessage().message());
                assertEquals(UseCaseMessageType.ENTER_SHIPPING_INFORMATION_SUCCESS.getCode(),
                    notification.useCaseMessage().code());
                assertEquals(UseCaseMessageType.ENTER_SHIPPING_INFORMATION_SUCCESS.getType(),
                    notification.useCaseMessage().type());

                return true;
            })
            .verifyComplete();

        verify(shippingInformationOutputMapper).mapToOutput(shippingInformation);
    }

    @Test
    void shouldHandleErrorWhenEnteringShippingInformation() {
        // Arrange
        EnterShippingInformationCommandInput input = new EnterShippingInformationCommandInput(
            "CATEGORY_1",
            "EMP123",
            "LOC456"
        );

        when(ShippingInformation.fromNewImportBatch(any(), any(), any()))
            .thenThrow(new RuntimeException("Test error"));

        // Act
        StepVerifier.create(enterShippingInformationUseCase.enterShippingInformation(input))
            // Assert
            .expectNextMatches(output -> {
                assertNotNull(output);
                assertNull(output.data());
                assertEquals(1, output.notifications().size());

                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(UseCaseMessageType.ENTER_SHIPPING_INFORMATION_ERROR.getMessage(),
                    notification.useCaseMessage().message());
                assertEquals(UseCaseMessageType.ENTER_SHIPPING_INFORMATION_ERROR.getCode(),
                    notification.useCaseMessage().code());
                assertEquals(UseCaseMessageType.ENTER_SHIPPING_INFORMATION_ERROR.getType(),
                    notification.useCaseMessage().type());

                return true;
            })
            .verifyComplete();

        verify(shippingInformationOutputMapper, never()).mapToOutput(any());
    }
}

