package com.arcone.biopro.distribution.receiving.unit.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.EnterShippingInformationCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.mapper.ShippingInformationOutputMapper;
import com.arcone.biopro.distribution.receiving.application.usecase.EnterShippingInformationUseCase;
import com.arcone.biopro.distribution.receiving.domain.model.Lookup;
import com.arcone.biopro.distribution.receiving.domain.model.ProductConsequence;
import com.arcone.biopro.distribution.receiving.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

        when(productConsequenceRepository.findAllByProductCategory(anyString())).thenReturn(Flux.just(Mockito.mock(ProductConsequence.class)));
        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(anyString(),anyString())).thenReturn(Flux.just(Mockito.mock(ProductConsequence.class)));

        List<Lookup> transitTimeZones = Arrays.asList(
            Lookup.fromRepository(1L,"TIME_ZONE","TZ1","Transit Zone 1", 1,true),
            Lookup.fromRepository(2L,"TIME_ZONE","TZ2","Transit Zone 2", 1,true)

        );

        List<Lookup> visualInspections = Arrays.asList(
            Lookup.fromRepository(1L,"TIME_ZONE","VI1","Visual Inspection 1", 1,true),
            Lookup.fromRepository(1L,"TIME_ZONE","VI2","Visual Inspection 2", 1,true)
        );

        when(lookupRepository.findAllByType(anyString())).thenReturn(Flux.fromIterable(transitTimeZones));

            // When/Then
            // Act
            StepVerifier.create(enterShippingInformationUseCase.enterShippingInformation(input))
                // Assert
                .assertNext(output -> {
                    assertNotNull(output);
                    assertTrue(output.notifications().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleErrorWhenEnteringShippingInformation() {
        // Arrange
        EnterShippingInformationCommandInput input = new EnterShippingInformationCommandInput(
            "CATEGORY_1",
            "EMP123",
            "LOC456"
        );

        when(productConsequenceRepository.findAllByProductCategory(anyString())).thenReturn(Flux.error(new IllegalArgumentException("TEST")));

        // Act
        StepVerifier.create(enterShippingInformationUseCase.enterShippingInformation(input))
            // Assert
            .assertNext(output -> {
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
            })
            .verifyComplete();

        verify(shippingInformationOutputMapper, never()).mapToOutput(any());
    }
}

