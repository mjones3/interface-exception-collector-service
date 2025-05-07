package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonPackingSlipOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.GenerateCartonPackingSlipCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.GenerateCartonPackingSlipUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonPackingSlip;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateCartonPackingSlipUseCaseTest {

    @Mock
    private RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;

    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;

    @Mock
    private CartonRepository cartonRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private SystemProcessPropertyRepository systemProcessPropertyRepository;

    @Mock
    private CartonOutputMapper cartonOutputMapper;

    @InjectMocks
    private GenerateCartonPackingSlipUseCase generateCartonPackingSlipUseCase;

    private GenerateCartonPackingSlipCommandInput commandInput;
    private Carton carton;
    private CartonPackingSlip cartonPackingSlip;
    private CartonPackingSlipOutput cartonPackingSlipOutput;

    @BeforeEach
    void setUp() {
        commandInput = GenerateCartonPackingSlipCommandInput
            .builder()
            .cartonId(1L)
            .employeeId("employee-id")
            .locationCode("location-id")
            .build();
        carton = Mockito.mock(Carton.class);
        cartonPackingSlip = Mockito.mock(CartonPackingSlip.class);
        cartonPackingSlipOutput = Mockito.mock(CartonPackingSlipOutput.class);
    }

    @Test
    void generateCartonPackingSlip_Success() {
        // Given
        when(cartonRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(carton));
        when(carton.generatePackingSlip(locationRepository, systemProcessPropertyRepository,
            recoveredPlasmaShippingRepository, recoveredPlasmaShipmentCriteriaRepository))
            .thenReturn(cartonPackingSlip);
        when(cartonOutputMapper.toOutPut(cartonPackingSlip)).thenReturn(cartonPackingSlipOutput);

        // When
        Mono<UseCaseOutput<CartonPackingSlipOutput>> result =
            generateCartonPackingSlipUseCase.generateCartonPackingSlip(commandInput);

        // Then
        StepVerifier.create(result)
            .assertNext(output -> {
                // Assert
                assertNotNull(output);
                assertEquals(1, output.notifications().size());
                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(UseCaseMessageType.CARTON_PACKING_SLIP_GENERATED_SUCCESS.getCode(),
                    notification.useCaseMessage().code());
                assertEquals(UseCaseMessageType.CARTON_PACKING_SLIP_GENERATED_SUCCESS.getType(),
                    notification.useCaseMessage().type());

                assertEquals(UseCaseMessageType.CARTON_PACKING_SLIP_GENERATED_SUCCESS.getMessage(),
                    notification.useCaseMessage().message());

                assertEquals(cartonPackingSlipOutput, output.data());
                assertNull(output._links());



            })
            .verifyComplete();
    }

    @Test
    void generateCartonPackingSlip_WhenCartonNotFound() {
        // Given
        when(cartonRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.empty());

        // When
        Mono<UseCaseOutput<CartonPackingSlipOutput>> result =
            generateCartonPackingSlipUseCase.generateCartonPackingSlip(commandInput);

        // Then
        StepVerifier.create(result)
            .assertNext(output -> {
                // Assert
                assertNotNull(output);
                assertEquals(1, output.notifications().size());
                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(UseCaseMessageType.CARTON_PACKING_SLIP_GENERATED_ERROR.getCode(),
                    notification.useCaseMessage().code());
                assertEquals(UseCaseMessageType.CARTON_PACKING_SLIP_GENERATED_ERROR.getType(),
                    notification.useCaseMessage().type());

                assertEquals(UseCaseMessageType.CARTON_PACKING_SLIP_GENERATED_ERROR.getMessage(),
                    notification.useCaseMessage().message());

                assertNull(output.data());
                assertNull(output._links());
            })
            .verifyComplete();
    }

    @Test
    void generateCartonPackingSlip_WhenGeneratePackingSlipThrowsException() {
        // Given
        when(cartonRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(carton));
        when(carton.generatePackingSlip(locationRepository, systemProcessPropertyRepository,
            recoveredPlasmaShippingRepository, recoveredPlasmaShipmentCriteriaRepository))
            .thenThrow(new RuntimeException("Error generating packing slip"));

        // When
        Mono<UseCaseOutput<CartonPackingSlipOutput>> result =
            generateCartonPackingSlipUseCase.generateCartonPackingSlip(commandInput);

        // Then
        StepVerifier.create(result)
            .assertNext(output -> {
                // Assert
                assertNotNull(output);
                assertEquals(1, output.notifications().size());
                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(UseCaseMessageType.CARTON_PACKING_SLIP_GENERATED_ERROR.getCode(),
                    notification.useCaseMessage().code());
                assertEquals(UseCaseMessageType.CARTON_PACKING_SLIP_GENERATED_ERROR.getType(),
                    notification.useCaseMessage().type());

                assertEquals(UseCaseMessageType.CARTON_PACKING_SLIP_GENERATED_ERROR.getMessage(),
                    notification.useCaseMessage().message());

                assertNull(output.data());
                assertNull(output._links());
            })
            .verifyComplete();
    }
}

