package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.FindCartonUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindCartonUseCaseTest {

    @Mock
    private CartonRepository cartonRepository;

    @Mock
    private CartonOutputMapper cartonOutputMapper;

    @InjectMocks
    private FindCartonUseCase findCartonUseCase;

    private Carton mockCarton;
    private CartonOutput mockCartonOutput;
    private final Long CARTON_ID = 1L;

    @BeforeEach
    void setUp() {
        mockCarton = Mockito.mock(Carton.class);

        mockCartonOutput = CartonOutput.builder()
            .id(CARTON_ID)
            .cartonNumber("CTN001")
            .build();
    }

    @Test
    void shouldFindCartonById_WhenCartonExists() {
        // Given
        when(cartonRepository.findOneById(CARTON_ID)).thenReturn(Mono.just(mockCarton));
        when(cartonOutputMapper.toOutput(mockCarton)).thenReturn(mockCartonOutput);

        // When
        Mono<UseCaseOutput<CartonOutput>> result = findCartonUseCase.findOneById(CARTON_ID);

        // Then
        StepVerifier.create(result)
            .expectNext(new UseCaseOutput<>(Collections.emptyList(), mockCartonOutput, null))
            .verifyComplete();

        verify(cartonRepository).findOneById(CARTON_ID);
        verify(cartonOutputMapper).toOutput(mockCarton);
    }

    @Test
    void shouldReturnError_WhenCartonNotFound() {
        // Given
        when(cartonRepository.findOneById(CARTON_ID))
            .thenReturn(Mono.empty());

        // When
        Mono<UseCaseOutput<CartonOutput>> result = findCartonUseCase.findOneById(CARTON_ID);

        // Then
        StepVerifier.create(result)
            .expectNextMatches(useCaseOutput -> {
                UseCaseNotificationOutput notification = useCaseOutput.notifications().get(0);
                return useCaseOutput.data() == null &&
                    useCaseOutput.notifications().size() == 1 &&
                    notification.useCaseMessage().getType() == UseCaseNotificationType.WARN &&
                    notification.useCaseMessage().getCode() == 10 &&
                    notification.useCaseMessage().getMessage().contains(CARTON_ID.toString());
            })
            .verifyComplete();

        verify(cartonRepository).findOneById(CARTON_ID);
        verify(cartonOutputMapper, never()).toOutput(any());
    }

    @Test
    void shouldHandleRepositoryError() {
        // Given
        RuntimeException mockException = new RuntimeException("Database error");
        when(cartonRepository.findOneById(CARTON_ID))
            .thenReturn(Mono.error(mockException));

        // When
        Mono<UseCaseOutput<CartonOutput>> result = findCartonUseCase.findOneById(CARTON_ID);

        // Then
        StepVerifier.create(result)
            .expectNextMatches(useCaseOutput -> {
                UseCaseNotificationOutput notification = useCaseOutput.notifications().get(0);
                return useCaseOutput.data() == null &&
                    useCaseOutput.notifications().size() == 1 &&
                    notification.useCaseMessage().getType() == UseCaseNotificationType.WARN &&
                    notification.useCaseMessage().getCode() == 10 &&
                    notification.useCaseMessage().getMessage().equals("Database error");
            })
            .verifyComplete();

        verify(cartonRepository).findOneById(CARTON_ID);
        verify(cartonOutputMapper, never()).toOutput(any());
    }

    @Test
    void shouldHandleMapperError() {
        // Given
        RuntimeException mockException = new RuntimeException("Mapping error");
        when(cartonRepository.findOneById(CARTON_ID)).thenReturn(Mono.just(mockCarton));
        when(cartonOutputMapper.toOutput(mockCarton)).thenThrow(mockException);

        // When
        Mono<UseCaseOutput<CartonOutput>> result = findCartonUseCase.findOneById(CARTON_ID);

        // Then
        StepVerifier.create(result)
            .expectNextMatches(useCaseOutput -> {
                UseCaseNotificationOutput notification = useCaseOutput.notifications().get(0);
                return useCaseOutput.data() == null &&
                    useCaseOutput.notifications().size() == 1 &&
                    notification.useCaseMessage().getType() == UseCaseNotificationType.WARN &&
                    notification.useCaseMessage().getCode() == 10 &&
                    notification.useCaseMessage().getMessage().equals("Mapping error");
            })
            .verifyComplete();

        verify(cartonRepository).findOneById(CARTON_ID);
        verify(cartonOutputMapper).toOutput(mockCarton);
    }
}

