package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ShipmentHistoryOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.ShipmentHistoryOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.ShipmentHistoryUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShipmentHistory;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentHistoryUseCaseTest {

    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;

    @Mock
    private ShipmentHistoryOutputMapper shipmentHistoryOutputMapper;

    @InjectMocks
    private ShipmentHistoryUseCase shipmentHistoryUseCase;

    @Test
    void findAllByShipmentId_WhenShipmentExists_ShouldReturnMappedResults() {
        // Arrange
        Long shipmentId = 1L;
        ShipmentHistory shipping1 = Mockito.mock(ShipmentHistory.class);
        ShipmentHistory shipping2 = Mockito.mock(ShipmentHistory.class);

        ShipmentHistoryOutput output1 = ShipmentHistoryOutput.builder().build();
        ShipmentHistoryOutput output2 =  ShipmentHistoryOutput.builder().build();

        when(recoveredPlasmaShippingRepository.findAllByShipmentId(shipmentId))
            .thenReturn(Flux.just(shipping1, shipping2));

        when(shipmentHistoryOutputMapper.toOutput(shipping1)).thenReturn(output1);
        when(shipmentHistoryOutputMapper.toOutput(shipping2)).thenReturn(output2);

        // Act & Assert
        StepVerifier.create(shipmentHistoryUseCase.findAllByShipmentId(shipmentId))
            .expectNext(output1)
            .expectNext(output2)
            .verifyComplete();

        verify(recoveredPlasmaShippingRepository).findAllByShipmentId(shipmentId);
        verify(shipmentHistoryOutputMapper).toOutput(shipping1);
        verify(shipmentHistoryOutputMapper).toOutput(shipping2);
    }

    @Test
    void findAllByShipmentId_WhenNoShipmentExists_ShouldThrowNoResultsFoundException() {
        // Arrange
        Long shipmentId = 1L;

        when(recoveredPlasmaShippingRepository.findAllByShipmentId(shipmentId))
            .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(shipmentHistoryUseCase.findAllByShipmentId(shipmentId))
            .expectError(NoResultsFoundException.class)
            .verify();

        verify(recoveredPlasmaShippingRepository).findAllByShipmentId(shipmentId);
        verifyNoInteractions(shipmentHistoryOutputMapper);
    }

    @Test
    void findAllByShipmentId_WhenRepositoryThrowsError_ShouldPropagateError() {
        // Arrange
        Long shipmentId = 1L;
        RuntimeException expectedError = new RuntimeException("Database error");

        when(recoveredPlasmaShippingRepository.findAllByShipmentId(shipmentId))
            .thenReturn(Flux.error(expectedError));

        // Act & Assert
        StepVerifier.create(shipmentHistoryUseCase.findAllByShipmentId(shipmentId))
            .expectError(RuntimeException.class)
            .verify();

        verify(recoveredPlasmaShippingRepository).findAllByShipmentId(shipmentId);
        verifyNoInteractions(shipmentHistoryOutputMapper);
    }
}

