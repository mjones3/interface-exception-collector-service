package com.arcone.biopro.distribution.eventbridge.unit.application.usecase;

import com.arcone.biopro.distribution.eventbridge.application.dto.RecoveredPlasmaShipmentClosedPayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.RecoveredPlasmaShipmentClosedMapper;
import com.arcone.biopro.distribution.eventbridge.application.usecase.RecoveredPlasmaShipmentClosedUseCase;
import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedOutbound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class RecoveredPlasmaShipmentClosedUseCaseTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private RecoveredPlasmaShipmentClosedMapper recoveredPlasmaShipmentClosedMapper;

    private RecoveredPlasmaShipmentClosedUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RecoveredPlasmaShipmentClosedUseCase(applicationEventPublisher, recoveredPlasmaShipmentClosedMapper);
    }

    @Test
    void processClosedShipmentEvent_ShouldMapAndPublishEvent() {
        // Arrange
        RecoveredPlasmaShipmentClosedPayload payload = Mockito.mock(RecoveredPlasmaShipmentClosedPayload.class);
        RecoveredPlasmaShipmentClosedOutbound outbound = Mockito.mock(RecoveredPlasmaShipmentClosedOutbound.class);

        when(recoveredPlasmaShipmentClosedMapper.toDomain(Mockito.any(RecoveredPlasmaShipmentClosedPayload.class))).thenReturn(outbound);

        // Act & Assert
        StepVerifier.create(useCase.processClosedShipmentEvent(payload))
            .verifyComplete();

        // Verify interactions
        verify(recoveredPlasmaShipmentClosedMapper).toDomain(Mockito.any(RecoveredPlasmaShipmentClosedPayload.class));
        verify(applicationEventPublisher).publishEvent(Mockito.any(com.arcone.biopro.distribution.eventbridge.domain.event.RecoveredPlasmaShipmentClosedOutboundEvent.class));

    }

    @Test
    void processClosedShipmentEvent_WhenMapperThrowsException_ShouldPropagateError() {
        // Arrange
        RecoveredPlasmaShipmentClosedPayload payload = Mockito.mock(RecoveredPlasmaShipmentClosedPayload.class);
        RuntimeException expectedException = new RuntimeException("Mapping error");

        when(recoveredPlasmaShipmentClosedMapper.toDomain(payload))
            .thenThrow(expectedException);

        // Act & Assert
        StepVerifier.create(useCase.processClosedShipmentEvent(payload))
            .expectError()
            .verify();


        verify(recoveredPlasmaShipmentClosedMapper).toDomain(payload);
        verifyNoInteractions(applicationEventPublisher);
    }
}
