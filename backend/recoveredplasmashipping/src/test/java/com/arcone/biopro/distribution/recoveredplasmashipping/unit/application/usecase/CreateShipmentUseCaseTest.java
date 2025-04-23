package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CreateShipmentInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CustomerOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CreateShipmentInputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.CreateShipmentUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentCreatedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CreateShipmentCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.LocationProperty;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentCriteria;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateShipmentUseCaseTest {

    @Mock
    private CreateShipmentInputMapper createShipmentInputMapper;

    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;

    @Mock
    private RecoveredPlasmaShipmentOutputMapper recoveredPlasmaShipmentOutputMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private CreateShipmentUseCase createShipmentUseCase;



    @Test
    void createShipment_WhenSuccessful_ShouldReturnSuccessOutput() {

        // Given

        var locationMock = Mockito.mock(Location.class);
        Mockito.when(locationMock.getCode()).thenReturn("CODE");
        Mockito.when(locationMock.findProperty(Mockito.eq("RPS_USE_PARTNER_PREFIX"))).thenReturn(Optional.of(new LocationProperty(1L, "RPS_USE_PARTNER_PREFIX", "Y")));
        Mockito.when(locationMock.findProperty(Mockito.eq("RPS_PARTNER_PREFIX"))).thenReturn(Optional.of(new LocationProperty(1L, "RPS_PARTNER_PREFIX", "BPM")));
        Mockito.when(locationMock.findProperty(Mockito.eq("RPS_LOCATION_SHIPMENT_CODE"))).thenReturn(Optional.of(new LocationProperty(1L, "RPS_LOCATION_SHIPMENT_CODE", "ABC")));



        Mockito.when(recoveredPlasmaShippingRepository.getNextShipmentId()).thenReturn(Mono.just(1L));
        Mockito.when(locationRepository.findOneByCode(Mockito.any())).thenReturn(Mono.just(locationMock));

        var productTypeMock = Mockito.mock(RecoveredPlasmaShipmentCriteria.class);
        Mockito.when(productTypeMock.getProductType()).thenReturn("productType");
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(Mockito.any(), Mockito.any())).thenReturn(Mono.just(productTypeMock));
        Mockito.when(customerService.findByCode(Mockito.any())).thenReturn(Mono.just(CustomerOutput.builder()
            .code("123")
            .name("name")
            .build()));



        CreateShipmentInput input = Mockito.mock(CreateShipmentInput.class);

        var createCommand = new CreateShipmentCommand("customerCoode", "locationCode", "productType"
            , "createEmployeeId", "123", LocalDate.now().plusDays(1), BigDecimal.TEN);


        RecoveredPlasmaShipment shipment = Mockito.mock(RecoveredPlasmaShipment.class);

        RecoveredPlasmaShipmentOutput shipmentOutput = Mockito.mock(RecoveredPlasmaShipmentOutput.class);

        when(createShipmentInputMapper.toCreateCommand(input)).thenReturn(createCommand);
        when(recoveredPlasmaShippingRepository.create(any(RecoveredPlasmaShipment.class)))
            .thenReturn(Mono.just(shipment));
        when(recoveredPlasmaShipmentOutputMapper.toRecoveredPlasmaShipmentOutput(shipment))
            .thenReturn(shipmentOutput);
        doNothing().when(applicationEventPublisher)
            .publishEvent(any(RecoveredPlasmaShipmentCreatedEvent.class));

        // When
        Mono<UseCaseOutput<RecoveredPlasmaShipmentOutput>> result = createShipmentUseCase
            .createShipment(input);

        // Then
        StepVerifier.create(result)
            .assertNext(output -> {
                assertNotNull(output);
                assertEquals(shipmentOutput, output.data());
                assertEquals("/recovered-plasma/0/shipment-details",
                    output._links().get("next"));

                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(UseCaseMessageType.SHIPMENT_CREATED_SUCCESS.getMessage(),
                    notification.useCaseMessage().message());
                assertEquals(UseCaseMessageType.SHIPMENT_CREATED_SUCCESS.getType(),
                    notification.useCaseMessage().type());
            })
            .verifyComplete();

        verify(createShipmentInputMapper).toCreateCommand(input);
        verify(recoveredPlasmaShippingRepository).create(any(RecoveredPlasmaShipment.class));
        verify(recoveredPlasmaShipmentOutputMapper).toRecoveredPlasmaShipmentOutput(shipment);
        verify(applicationEventPublisher).publishEvent(any(RecoveredPlasmaShipmentCreatedEvent.class));
    }

    @Test
    void shouldNotCreateShipment_WhenFails_ShouldReturnErrorOutput() {

        // Given
        Mockito.when(locationRepository.findOneByCode(Mockito.any())).thenReturn(Mono.empty());

        CreateShipmentInput input = Mockito.mock(CreateShipmentInput.class);

        var createCommand = new CreateShipmentCommand("customerCoode", "locationCode", "productType"
            , "createEmployeeId", "123", LocalDate.now().plusDays(1), BigDecimal.TEN);

        when(createShipmentInputMapper.toCreateCommand(input)).thenReturn(createCommand);

        // When
        Mono<UseCaseOutput<RecoveredPlasmaShipmentOutput>> result = createShipmentUseCase
            .createShipment(input);


        // Then
        StepVerifier.create(result)
            .assertNext(output -> {
                assertNotNull(output);
                assertNull(output.data());
                assertNull(output._links());
                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(UseCaseNotificationType.WARN,
                    notification.useCaseMessage().type());
                assertEquals("Location is required", notification.useCaseMessage().message());
                assertEquals(3, notification.useCaseMessage().code());
            })
            .verifyComplete();

        verify(createShipmentInputMapper).toCreateCommand(input);
        verify(recoveredPlasmaShippingRepository, Mockito.never()).create(any());
        verify(recoveredPlasmaShipmentOutputMapper, Mockito.never())
            .toRecoveredPlasmaShipmentOutput(any());
        verify(applicationEventPublisher, Mockito.never())
            .publishEvent(any(RecoveredPlasmaShipmentCreatedEvent.class));
    }
}
