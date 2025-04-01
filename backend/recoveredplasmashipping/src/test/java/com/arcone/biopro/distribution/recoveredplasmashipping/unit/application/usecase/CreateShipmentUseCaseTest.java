package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CreateShipmentInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CreateShipmentInputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.CreateShipmentUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentCreatedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CreateShipmentCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
/*
    @Test
    void createShipment_WhenSuccessful_ShouldReturnSuccessOutput() {

        // Given
        CreateShipmentInput input = Mockito.mock(CreateShipmentInput.class);

        CreateShipmentCommand command = Mockito.mock(CreateShipmentCommand.class);

        RecoveredPlasmaShipment shipment = Mockito.mock(RecoveredPlasmaShipment.class);

        RecoveredPlasmaShipmentOutput shipmentOutput = Mockito.mock(RecoveredPlasmaShipmentOutput.class);

        when(createShipmentInputMapper.toCreateCommand(input)).thenReturn(command);
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
                assertEquals("/recovered-plasma/:123/shipment-details",
                    output._links().get("next"));

                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(UseCaseMessageType.SHIPMENT_CREATED_SUCCESS.getMessage(),
                    notification.useCaseMessage().getMessage());
                assertEquals(UseCaseMessageType.SHIPMENT_CREATED_SUCCESS.getType(),
                    notification.useCaseMessage().getType());
            })
            .verifyComplete();

        verify(createShipmentInputMapper).toCreateCommand(input);
        verify(recoveredPlasmaShippingRepository).create(any(RecoveredPlasmaShipment.class));
        verify(recoveredPlasmaShipmentOutputMapper).toRecoveredPlasmaShipmentOutput(shipment);
        verify(applicationEventPublisher).publishEvent(any(RecoveredPlasmaShipmentCreatedEvent.class));
    }*/

    /*@Test
    void createShipment_WhenMappingFails_ShouldReturnErrorOutput() {
        // Given
        CreateShipmentInput input = new CreateShipmentInput();
        RuntimeException mappingException = new RuntimeException("Mapping failed");

        when(createShipmentInputMapper.toCreateCommand(input))
            .thenThrow(mappingException);

        // When
        Mono<UseCaseOutput<RecoveredPlasmaShipmentOutput>> result = createShipmentUseCase
            .createShipment(input);

        // Then
        StepVerifier.create(result)
            .assertNext(output -> {
                assertNotNull(output);
                assertNull(output.getOutput());
                assertNull(output.getLinks());

                UseCaseNotificationOutput notification = output.getNotifications().get(0);
                assertEquals(UseCaseNotificationType.WARN,
                    notification.getUseCaseMessage().getNotificationType());
                assertEquals(mappingException.getMessage(),
                    notification.getUseCaseMessage().getMessage());
                assertEquals(3, notification.getUseCaseMessage().getCode());
            })
            .verifyComplete();

        verify(createShipmentInputMapper).toCreateCommand(input);
        verify(recoveredPlasmaShippingRepository, never()).create(any());
        verify(recoveredPlasmaShipmentOutputMapper, never())
            .toRecoveredPlasmaShipmentOutput(any());
        verify(applicationEventPublisher, never())
            .publishEvent(any(RecoveredPlasmaShipmentCreatedEvent.class));
    }

    @Test
    void createShipment_WhenRepositoryFails_ShouldReturnErrorOutput() {
        // Given
        CreateShipmentInput input = new CreateShipmentInput();
        CreateShipmentCommand command = new CreateShipmentCommand();
        RuntimeException repositoryException = new RuntimeException("Repository error");

        when(createShipmentInputMapper.toCreateCommand(input)).thenReturn(command);
        when(recoveredPlasmaShippingRepository.create(any(RecoveredPlasmaShipment.class)))
            .thenReturn(Mono.error(repositoryException));

        // When
        Mono<UseCaseOutput<RecoveredPlasmaShipmentOutput>> result = createShipmentUseCase
            .createShipment(input);

        // Then
        StepVerifier.create(result)
            .assertNext(output -> {
                assertNotNull(output);
                assertNull(output.getOutput());
                assertNull(output.getLinks());

                UseCaseNotificationOutput notification = output.getNotifications().get(0);
                assertEquals(UseCaseNotificationType.WARN,
                    notification.getUseCaseMessage().getNotificationType());
                assertEquals(repositoryException.getMessage(),
                    notification.getUseCaseMessage().getMessage());
            })
            .verifyComplete();

        verify(createShipmentInputMapper).toCreateCommand(input);
        verify(recoveredPlasmaShippingRepository).create(any());
        verify(recoveredPlasmaShipmentOutputMapper, never())
            .toRecoveredPlasmaShipmentOutput(any());
        verify(applicationEventPublisher, never())
            .publishEvent(any(RecoveredPlasmaShipmentCreatedEvent.class));
    }*/
}
