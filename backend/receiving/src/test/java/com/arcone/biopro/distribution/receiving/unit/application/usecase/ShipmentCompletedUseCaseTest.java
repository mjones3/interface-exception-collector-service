package com.arcone.biopro.distribution.receiving.unit.application.usecase;

import com.arcone.biopro.distribution.receiving.application.mapper.ShipmentCompletedMessageMapper;
import com.arcone.biopro.distribution.receiving.application.usecase.ShipmentCompletedUseCase;
import com.arcone.biopro.distribution.receiving.domain.model.InternalTransfer;
import com.arcone.biopro.distribution.receiving.domain.repository.InternalTransferRepository;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.ShipmentCompletedMessage;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.ShipmentCompletedPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentCompletedUseCaseTest {

    @Mock
    private InternalTransferRepository internalTransferRepository;

    @Mock
    private ShipmentCompletedMessageMapper shipmentCompletedMessageMapper;

    private ShipmentCompletedUseCase shipmentCompletedUseCase;

    @BeforeEach
    void setUp() {
        shipmentCompletedUseCase = new ShipmentCompletedUseCase(
            internalTransferRepository,
            shipmentCompletedMessageMapper
        );
    }

    @Test
    void processShipmentCompletedMessage_WhenInternalTransferType_ShouldCreateInternalTransfer() {
        // Arrange
        ShipmentCompletedPayload payload = ShipmentCompletedPayload.builder()
            .shipmentType("INTERNAL_TRANSFER")
            .orderNumber(123L)
            .externalOrderId("EXT123")
            .build();

        ShipmentCompletedMessage message = Mockito.mock(ShipmentCompletedMessage.class);
        when(message.getPayload()).thenReturn(payload);

        InternalTransfer mappedTransfer = mock(InternalTransfer.class);
        InternalTransfer createdTransfer = mock(InternalTransfer.class);

        when(shipmentCompletedMessageMapper.toModel(message)).thenReturn(mappedTransfer);
        when(internalTransferRepository.create(mappedTransfer)).thenReturn(Mono.just(createdTransfer));

        // Act & Assert
        StepVerifier.create(shipmentCompletedUseCase.processShipmentCompletedMessage(message))
            .expectNext(createdTransfer)
            .verifyComplete();

        verify(shipmentCompletedMessageMapper).toModel(message);
        verify(internalTransferRepository).create(mappedTransfer);
    }

    @Test
    void processShipmentCompletedMessage_WhenNotInternalTransferType_ShouldReturnEmpty() {
        // Arrange
        ShipmentCompletedPayload payload = ShipmentCompletedPayload.builder()
            .shipmentType("OTHER_TYPE")
            .build();

        ShipmentCompletedMessage message = Mockito.mock(ShipmentCompletedMessage.class);
        when(message.getPayload()).thenReturn(payload);

        // Act & Assert
        StepVerifier.create(shipmentCompletedUseCase.processShipmentCompletedMessage(message))
            .verifyComplete();

        verify(shipmentCompletedMessageMapper, never()).toModel(any());
        verify(internalTransferRepository, never()).create(any());
    }

    @Test
    void processShipmentCompletedMessage_WhenRepositoryError_ShouldPropagateError() {
        // Arrange
        ShipmentCompletedPayload payload = ShipmentCompletedPayload.builder()
            .shipmentType("INTERNAL_TRANSFER")
            .build();

        ShipmentCompletedMessage message = Mockito.mock(ShipmentCompletedMessage.class);
        when(message.getPayload()).thenReturn(payload);

        InternalTransfer mappedTransfer = mock(InternalTransfer.class);
        RuntimeException error = new RuntimeException("Repository error");

        when(shipmentCompletedMessageMapper.toModel(message)).thenReturn(mappedTransfer);
        when(internalTransferRepository.create(mappedTransfer)).thenReturn(Mono.error(error));

        // Act & Assert
        StepVerifier.create(shipmentCompletedUseCase.processShipmentCompletedMessage(message))
            .expectError(RuntimeException.class)
            .verify();

        verify(shipmentCompletedMessageMapper).toModel(message);
        verify(internalTransferRepository).create(mappedTransfer);
    }
}
