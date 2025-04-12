package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CreateCartonCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.CreateCartonUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.LocationProperty;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCartonUseCaseTest {

    @Mock
    private  CartonRepository cartonRepository;

    @Mock
    private  RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;

    private  CartonOutputMapper cartonOutputMapper;

    @Mock
    private  LocationRepository locationRepository;

    private CreateCartonUseCase createCartonUseCase;

    @BeforeEach
    public void setUp(){
        cartonOutputMapper = Mappers.getMapper(CartonOutputMapper.class);
        createCartonUseCase = new CreateCartonUseCase(cartonRepository,recoveredPlasmaShippingRepository,cartonOutputMapper,locationRepository);
    }

    @Test
    void createCarton_WhenSuccessful_ShouldReturnSuccessOutput() {

        // Given

        var mockCarton = Mockito.mock(Carton.class);
        Mockito.when(mockCarton.getCartonNumber()).thenReturn("123");
        Mockito.when(mockCarton.getId()).thenReturn(1L);

        RecoveredPlasmaShipment recoveredPlasmaShipment = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(recoveredPlasmaShipment.getId()).thenReturn(1L);
        Mockito.when(recoveredPlasmaShipment.getLocationCode()).thenReturn("locationCode");

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(recoveredPlasmaShipment));

        Mockito.when(cartonRepository.countByShipment(Mockito.anyLong())).thenReturn(Mono.just(0));

        Location location = mock(Location.class);

        when(locationRepository.findOneByCode(anyString())).thenReturn(Mono.just(location));
        when(cartonRepository.getNextCartonId()).thenReturn(Mono.just( 123L));

        when(location.findProperty("RPS_CARTON_PARTNER_PREFIX")).thenReturn(Optional.of(new LocationProperty(1L,"RPS_CARTON_PARTNER_PREFIX","BPM")));
        when(location.findProperty("RPS_LOCATION_CARTON_CODE")).thenReturn(Optional.of(new LocationProperty(1L,"RPS_LOCATION_CARTON_CODE","MH1")));
        when(locationRepository.findOneByCode(anyString())).thenReturn(Mono.just(location));
        when(cartonRepository.getNextCartonId()).thenReturn(Mono.just(1L));


            Mockito.when(cartonRepository.create(Mockito.any(Carton.class))).thenReturn(Mono.just(mockCarton));

            // Then
            StepVerifier.create(createCartonUseCase.createCarton(CreateCartonCommandInput
                    .builder()
                    .shipmentId(1L)
                    .employeeId("test")
                    .build()))
                .assertNext(output -> {
                    assertNotNull(output);
                    assertEquals("123", output.data().cartonNumber());
                    assertEquals("/recovered-plasma/1/carton-details",
                        output._links().get("next"));

                    UseCaseNotificationOutput notification = output.notifications().get(0);
                    assertEquals(UseCaseMessageType.CARTON_CREATED_SUCCESS.getMessage(),notification.useCaseMessage().message());
                    assertEquals(UseCaseMessageType.CARTON_CREATED_SUCCESS.getType(),notification.useCaseMessage().type());
                })
                .verifyComplete();


    }

    @Test
    void shouldNotCreateCarton_WhenFails_ShouldReturnErrorOutput() {

        // Given

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.error(new RuntimeException("TEST")));

        // Then
        StepVerifier.create(createCartonUseCase.createCarton(CreateCartonCommandInput
                .builder()
                .shipmentId(1L)
                .employeeId("test")
                .build()))
            .assertNext(output -> {
                assertNotNull(output);
                assertNull(output.data());
                assertNull(output._links());
                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(UseCaseNotificationType.SYSTEM,
                    notification.useCaseMessage().type());
                assertEquals("Carton generation error. Contact Support.", notification.useCaseMessage().message());
                assertEquals(6, notification.useCaseMessage().code());
            })
            .verifyComplete();
    }

}
