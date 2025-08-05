package com.arcone.biopro.distribution.receiving.unit.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.ShippingInformationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateTransferOrderNumberCommandInput;
import com.arcone.biopro.distribution.receiving.application.mapper.ShippingInformationOutputMapper;
import com.arcone.biopro.distribution.receiving.application.usecase.ValidateTransferOrderNumberUseCase;
import com.arcone.biopro.distribution.receiving.domain.model.InternalTransfer;
import com.arcone.biopro.distribution.receiving.domain.model.Location;
import com.arcone.biopro.distribution.receiving.domain.model.Lookup;
import com.arcone.biopro.distribution.receiving.domain.model.ProductConsequence;
import com.arcone.biopro.distribution.receiving.domain.model.ShippingInformation;
import com.arcone.biopro.distribution.receiving.domain.repository.InternalTransferRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateTransferOrderNumberUseCaseTest {

    @Mock
    private ProductConsequenceRepository productConsequenceRepository;

    @Mock
    private LookupRepository lookupRepository;

    @Mock
    private ShippingInformationOutputMapper shippingInformationOutputMapper;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private InternalTransferRepository internalTransferRepository;

    private ValidateTransferOrderNumberUseCase validateTransferOrderNumberUseCase;


    @BeforeEach
    void setUp() {
        validateTransferOrderNumberUseCase = new ValidateTransferOrderNumberUseCase(
            productConsequenceRepository,
            lookupRepository,
            shippingInformationOutputMapper,
            locationRepository,
            internalTransferRepository
        );

    }

    @Test
    void validateTransferOrderNumber_WhenInternalTransferExists_ShouldReturnSuccessOutput() {

            // Arrange
            ValidateTransferOrderNumberCommandInput input = ValidateTransferOrderNumberCommandInput.builder()
                .orderNumber(123L)
                .employeeId("emp123")
                .locationCode("LOC123")
                .build();

            InternalTransfer internalTransfer = mock(InternalTransfer.class);
            when(internalTransfer.getTemperatureCategory()).thenReturn("FROZEN");

            ShippingInformation shippingInformation = Mockito.mock(ShippingInformation.class);
            ShippingInformationOutput output = mock(ShippingInformationOutput.class);

            when(internalTransferRepository.findOneByOrderNumber(123L)).thenReturn(Mono.just(internalTransfer));
            when(shippingInformationOutputMapper.mapToOutput(any(ShippingInformation.class))).thenReturn(output);

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


        var location = Mockito.mock(Location.class);
        when(location.getTimeZone()).thenReturn("America/New_York");

        when(locationRepository.findOneByCode("LOC123")).thenReturn(Mono.just(location));

            // Act & Assert
            StepVerifier.create(validateTransferOrderNumberUseCase.validateTransferOrderNumber(input))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(output, result.data());
                    assertTrue(result.notifications().isEmpty());
                    assertNull(result._links());
                })
                .verifyComplete();

            verify(internalTransferRepository).findOneByOrderNumber(123L);
            verify(shippingInformationOutputMapper).mapToOutput(any(ShippingInformation.class));

    }

    @Test
    void validateTransferOrderNumber_WhenInternalTransferNotFound_ShouldReturnNotFoundError() {
        // Arrange
        ValidateTransferOrderNumberCommandInput input = ValidateTransferOrderNumberCommandInput.builder()
            .orderNumber(999L)
            .employeeId("emp123")
            .locationCode("LOC123")
            .build();

        when(internalTransferRepository.findOneByOrderNumber(999L)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(validateTransferOrderNumberUseCase.validateTransferOrderNumber(input))
            .assertNext(result -> {
                assertNotNull(result);
                assertNull(result.data());
                assertNull(result._links());
                assertEquals(1, result.notifications().size());
                assertEquals(UseCaseMessageType.INTERNAL_TRANSFER_NOT_FOUND_ERROR.getMessage(),
                    result.notifications().get(0).useCaseMessage().message());
                assertEquals(UseCaseMessageType.INTERNAL_TRANSFER_NOT_FOUND_ERROR.getCode(),
                    result.notifications().get(0).useCaseMessage().code());
                assertEquals(UseCaseMessageType.INTERNAL_TRANSFER_NOT_FOUND_ERROR.getType(),
                    result.notifications().get(0).useCaseMessage().type());
            })
            .verifyComplete();

        verify(internalTransferRepository).findOneByOrderNumber(999L);
        verify(shippingInformationOutputMapper, never()).mapToOutput(any());
    }

    @Test
    void validateTransferOrderNumber_WhenShippingInformationCreationFails_ShouldReturnGenericError() {
        // Arrange
        ValidateTransferOrderNumberCommandInput input = ValidateTransferOrderNumberCommandInput.builder()
            .orderNumber(123L)
            .employeeId("emp123")
            .locationCode("LOC123")
            .build();

        InternalTransfer internalTransfer = mock(InternalTransfer.class);
        when(internalTransfer.getTemperatureCategory()).thenReturn("FROZEN");

        when(internalTransferRepository.findOneByOrderNumber(123L)).thenReturn(Mono.just(internalTransfer));

        // Act & Assert
        StepVerifier.create(validateTransferOrderNumberUseCase.validateTransferOrderNumber(input))
            .assertNext(result -> {
                assertNotNull(result);
                assertNull(result.data());
                assertNull(result._links());
                assertEquals(1, result.notifications().size());
                assertEquals(UseCaseMessageType.ENTER_SHIPPING_INFORMATION_ERROR.getMessage(),
                    result.notifications().get(0).useCaseMessage().message());
                assertEquals(UseCaseMessageType.ENTER_SHIPPING_INFORMATION_ERROR.getCode(),
                    result.notifications().get(0).useCaseMessage().code());
                assertEquals(UseCaseMessageType.ENTER_SHIPPING_INFORMATION_ERROR.getType(),
                    result.notifications().get(0).useCaseMessage().type());
            })
            .verifyComplete();

        verify(internalTransferRepository).findOneByOrderNumber(123L);
        verify(shippingInformationOutputMapper, never()).mapToOutput(any());
    }
}
