package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CloseCartonCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.CloseCartonUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaCartonClosedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CloseCartonCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseCartonUseCaseTest {

    @Mock
    private CartonRepository cartonRepository;

    @Mock
    private CartonOutputMapper cartonOutputMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private CloseCartonUseCase closeCartonUseCase;

    @BeforeEach
    void setUp() {
        closeCartonUseCase = new CloseCartonUseCase(cartonRepository, cartonOutputMapper, applicationEventPublisher);
    }

    @Test
    @DisplayName("Should successfully close carton when all conditions are met")
    void shouldSuccessfullyCloseCarton() {
        // Given
        Long cartonId = 1L;
        String employeeId = "EMP123";
        String locationCode = "LOC456";
        Long shipmentId = 2L;

        CloseCartonCommandInput commandInput = new CloseCartonCommandInput(cartonId, employeeId, locationCode);
        Carton carton = mock(Carton.class);
        Carton closedCarton = mock(Carton.class);
        CartonOutput cartonOutput = mock(CartonOutput.class);

        when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.just(carton));
        when(carton.close(any(CloseCartonCommand.class))).thenReturn(closedCarton);
        when(cartonRepository.update(closedCarton)).thenReturn(Mono.just(closedCarton));
        when(closedCarton.getShipmentId()).thenReturn(shipmentId);
        when(cartonOutputMapper.toOutput(closedCarton)).thenReturn(cartonOutput);

        // When
        Mono<UseCaseOutput<CartonOutput>> result = closeCartonUseCase.closeCarton(commandInput);

        // Then
        StepVerifier.create(result)
            .assertNext(output -> {
                assert output.notifications().size() == 1;
                assert output.notifications().get(0).useCaseMessage().type() == UseCaseMessageType.CARTON_CLOSED_SUCCESS.getType();
                assert output.data() == cartonOutput;
                assert output._links().get("next").equals(String.format("/recovered-plasma/%s/shipment-details", shipmentId));
            })
            .verifyComplete();

        verify(applicationEventPublisher).publishEvent(any(RecoveredPlasmaCartonClosedEvent.class));
    }

    @Test
    @DisplayName("Should return error when carton is not found")
    void shouldReturnErrorWhenCartonNotFound() {
        // Given
        Long cartonId = 1L;
        String employeeId = "EMP123";
        String locationCode = "LOC456";

        CloseCartonCommandInput commandInput = new CloseCartonCommandInput(cartonId, employeeId, locationCode);
        when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.empty());

        // When
        Mono<UseCaseOutput<CartonOutput>> result = closeCartonUseCase.closeCarton(commandInput);

        // Then
        StepVerifier.create(result)
            .assertNext(output -> {
                assert output.notifications().size() == 1;
                assert output.notifications().get(0).useCaseMessage().type() == UseCaseMessageType.CARTON_CLOSED_ERROR.getType();
                assert output.data() == null;
                assert output._links() == null;
            })
            .verifyComplete();

        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should handle error when closing carton fails")
    void shouldHandleErrorWhenClosingCartonFails() {
        // Given
        Long cartonId = 1L;
        String employeeId = "EMP123";
        String locationCode = "LOC456";

        CloseCartonCommandInput commandInput = new CloseCartonCommandInput(cartonId, employeeId, locationCode);
        Carton carton = mock(Carton.class);

        when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.just(carton));
        when(carton.close(any(CloseCartonCommand.class))).thenThrow(new IllegalStateException("Cannot close carton"));

        // When
        Mono<UseCaseOutput<CartonOutput>> result = closeCartonUseCase.closeCarton(commandInput);

        // Then
        StepVerifier.create(result)
            .assertNext(output -> {
                assert output.notifications().size() == 1;
                assert output.notifications().get(0).useCaseMessage().type() == UseCaseMessageType.CARTON_CLOSED_ERROR.getType();
                assert output.data() == null;
                assert output._links() == null;
            })
            .verifyComplete();

        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should handle error when updating carton fails")
    void shouldHandleErrorWhenUpdatingCartonFails() {
        // Given
        Long cartonId = 1L;
        String employeeId = "EMP123";
        String locationCode = "LOC456";

        CloseCartonCommandInput commandInput = new CloseCartonCommandInput(cartonId, employeeId, locationCode);
        Carton carton = mock(Carton.class);
        Carton closedCarton = mock(Carton.class);

        when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.just(carton));
        when(carton.close(any(CloseCartonCommand.class))).thenReturn(closedCarton);
        when(cartonRepository.update(closedCarton)).thenReturn(Mono.error(new RuntimeException("Update failed")));

        // When
        Mono<UseCaseOutput<CartonOutput>> result = closeCartonUseCase.closeCarton(commandInput);

        // Then
        StepVerifier.create(result)
            .assertNext(output -> {
                assert output.notifications().size() == 1;
                assert output.notifications().get(0).useCaseMessage().type() == UseCaseMessageType.CARTON_CLOSED_ERROR.getType();
                assert output.data() == null;
                assert output._links() == null;
            })
            .verifyComplete();

        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}
