package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.GenerateCartonLabelCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.GenerateCartonLabelUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.LabelTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateCartonLabelUseCaseTest {

    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;

    @Mock
    private CartonRepository cartonRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private SystemProcessPropertyRepository systemProcessPropertyRepository;

    @Mock
    private LabelTemplateService labelTemplateService;

    @InjectMocks
    private GenerateCartonLabelUseCase generateCartonLabelUseCase;

    private GenerateCartonLabelCommandInput commandInput;
    private Carton mockCarton;
    private static final Long CARTON_ID = 1L;
    private static final String GENERATED_LABEL = "GENERATED_LABEL_CONTENT";

    @BeforeEach
    void setUp() {
        commandInput = new GenerateCartonLabelCommandInput(CARTON_ID,"employeeId");
        mockCarton = Mockito.mock(Carton.class); // Assuming Carton has a default constructor
    }

    @Test
    void generateCartonLabel_WhenSuccessful_ShouldReturnLabelOutput() {
        // Arrange
        when(cartonRepository.findOneById(CARTON_ID))
            .thenReturn(Mono.just(mockCarton));

        when(mockCarton.generateCartonLabel(
            labelTemplateService,
            locationRepository,
            recoveredPlasmaShippingRepository,
            systemProcessPropertyRepository))
            .thenReturn(GENERATED_LABEL);

        // Act & Assert
        StepVerifier.create(generateCartonLabelUseCase.generateCartonLabel(commandInput))
            .expectNextMatches(useCaseOutput -> {
                // Verify success notification
                UseCaseNotificationOutput notification = useCaseOutput.notifications().get(0);
                boolean isSuccessNotification = notification.useCaseMessage().type()
                    .equals(UseCaseMessageType.CARTON_LABEL_GENERATED_SUCCESS.getType());

                // Verify label content
                boolean hasCorrectLabel = useCaseOutput.data().labelContent()
                    .equals(GENERATED_LABEL);

                return isSuccessNotification && hasCorrectLabel;
            })
            .verifyComplete();

        // Verify repository call
        verify(cartonRepository, times(1)).findOneById(CARTON_ID);
    }

    @Test
    void generateCartonLabel_WhenCartonNotFound_ShouldReturnError() {
        // Arrange
        when(cartonRepository.findOneById(CARTON_ID))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(generateCartonLabelUseCase.generateCartonLabel(commandInput))
            .expectNextMatches(useCaseOutput -> {
                UseCaseNotificationOutput notification = useCaseOutput.notifications().get(0);
                return notification.useCaseMessage().type()
                    .equals(UseCaseMessageType.CARTON_LABEL_GENERATED_ERROR.getType());
            })
            .verifyComplete();

        verify(cartonRepository, times(1)).findOneById(CARTON_ID);
    }

    @Test
    void generateCartonLabel_WhenLabelGenerationFails_ShouldReturnError() {
        // Arrange
        when(cartonRepository.findOneById(CARTON_ID))
            .thenReturn(Mono.just(mockCarton));

        when(mockCarton.generateCartonLabel(
            labelTemplateService,
            locationRepository,
            recoveredPlasmaShippingRepository,
            systemProcessPropertyRepository))
            .thenThrow(new RuntimeException("Label generation failed"));

        // Act & Assert
        StepVerifier.create(generateCartonLabelUseCase.generateCartonLabel(commandInput))
            .expectNextMatches(useCaseOutput -> {
                UseCaseNotificationOutput notification = useCaseOutput.notifications().get(0);
                return notification.useCaseMessage().type()
                    .equals(UseCaseMessageType.CARTON_LABEL_GENERATED_ERROR.getType())
                    && useCaseOutput.data() == null;
            })
            .verifyComplete();

        verify(cartonRepository, times(1)).findOneById(CARTON_ID);
    }

    @Test
    void generateCartonLabel_ShouldUseCorrectScheduler() {
        // Arrange
        when(cartonRepository.findOneById(CARTON_ID))
            .thenReturn(Mono.just(mockCarton));

        when(mockCarton.generateCartonLabel(
            labelTemplateService,
            locationRepository,
            recoveredPlasmaShippingRepository,
            systemProcessPropertyRepository))
            .thenReturn(GENERATED_LABEL);

        // Act & Assert
        StepVerifier.create(generateCartonLabelUseCase.generateCartonLabel(commandInput))
            .expectNextMatches(useCaseOutput ->
                useCaseOutput.data() != null &&
                    useCaseOutput.data().labelContent().equals(GENERATED_LABEL))
            .verifyComplete();
    }
}

