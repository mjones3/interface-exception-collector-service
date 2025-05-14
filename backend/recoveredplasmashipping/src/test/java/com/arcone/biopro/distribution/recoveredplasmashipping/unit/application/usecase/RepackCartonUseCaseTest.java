package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RepackCartonCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.RepackCartonUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaCartonUnpackedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RepackCartonCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepackCartonUseCaseTest {

    @Mock
    private CartonRepository cartonRepository;

    @Mock
    private CartonOutputMapper cartonOutputMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private CartonItemRepository cartonItemRepository;

    @InjectMocks
    private RepackCartonUseCase repackCartonUseCase;

    @Test
    void repackCarton_Success() {
        // Arrange
        Long cartonId = 123L;
        String employeeId = "emp123";
        String locationCode = "LOC1";
        String comments = "Test comments";

        RepackCartonCommandInput commandInput = new RepackCartonCommandInput(
            cartonId, employeeId, locationCode, comments
        );

        Carton originalCarton = mock(Carton.class);
        Mockito.when(originalCarton.getId()).thenReturn(cartonId);
        Carton reopenedCarton = mock(Carton.class);
        Mockito.when(reopenedCarton.getId()).thenReturn(cartonId);

        CartonOutput cartonOutput = mock(CartonOutput.class);

        when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.just(originalCarton));
        when(originalCarton.markAsReopen(any(RepackCartonCommand.class))).thenReturn(reopenedCarton);
        when(cartonRepository.update(reopenedCarton)).thenReturn(Mono.just(reopenedCarton));
        when(cartonItemRepository.deleteAllByCartonId(cartonId)).thenReturn(Mono.empty());
        when(reopenedCarton.getId()).thenReturn(cartonId);
        when(cartonOutputMapper.toOutput(Mockito.any())).thenReturn(cartonOutput);

        // Act
        Mono<UseCaseOutput<CartonOutput>> result = repackCartonUseCase.repackCarton(commandInput);

        // Assert
        StepVerifier.create(result)
            .assertNext(output -> {
                assertNotNull(output);
                assertEquals(1, output.notifications().size());
                assertEquals(
                    UseCaseMessageType.CARTON_REPACKED_SUCCESS.getMessage(),
                    output.notifications().get(0).useCaseMessage().message()
                );
                assertEquals("/recovered-plasma/123/carton-details",output._links().get("next")
                );

                verify(applicationEventPublisher).publishEvent(any(RecoveredPlasmaCartonUnpackedEvent.class));
            })
            .verifyComplete();
    }

    @Test
    void repackCarton_WhenCartonNotFound_ReturnsError() {
        // Arrange
        Long cartonId = 1L;
        RepackCartonCommandInput commandInput = new RepackCartonCommandInput(
            cartonId, "emp123", "LOC1", "comments"
        );

        when(cartonRepository.findOneById(cartonId))
            .thenReturn(Mono.empty());

        // Act
        Mono<UseCaseOutput<CartonOutput>> result = repackCartonUseCase.repackCarton(commandInput);

        // Assert
        StepVerifier.create(result)
            .assertNext(output -> {
                assertNotNull(output);
                assertNull(output.data());
                assertEquals(1, output.notifications().size());
                assertEquals(20, output.notifications().get(0).useCaseMessage().code());
                assertEquals(
                    UseCaseNotificationType.WARN,
                    output.notifications().get(0).useCaseMessage().type()
                );
            })
            .verifyComplete();
    }

    @Test
    void repackCarton_WhenUpdateFails_ReturnsError() {
        // Arrange
        Long cartonId = 123L;
        RepackCartonCommandInput commandInput = new RepackCartonCommandInput(
            cartonId, "emp123", "LOC1", "comments"
        );

        Carton originalCarton = mock(Carton.class);
        Carton reopenedCarton = mock(Carton.class);

        when(cartonRepository.findOneById(cartonId))
            .thenReturn(Mono.just(originalCarton));
        when(originalCarton.markAsReopen(any(RepackCartonCommand.class)))
            .thenReturn(reopenedCarton);
        when(cartonRepository.update(reopenedCarton))
            .thenReturn(Mono.error(new RuntimeException("Update failed")));

        // Act
        Mono<UseCaseOutput<CartonOutput>> result = repackCartonUseCase.repackCarton(commandInput);

        // Assert
        StepVerifier.create(result)
            .assertNext(output -> {
                assertNotNull(output);
                assertNull(output.data());
                assertEquals(1, output.notifications().size());
                assertEquals("Update failed", output.notifications().get(0).useCaseMessage().message());
                assertEquals(20, output.notifications().get(0).useCaseMessage().code());
                assertEquals(
                    UseCaseNotificationType.WARN,
                    output.notifications().get(0).useCaseMessage().type()
                );
            })
            .verifyComplete();
    }
}

