package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ModifyShipmentCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.ModifyShipmentInputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.ModifyShipmentUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ModifyShipmentCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShipmentHistory;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifyShipmentUseCaseTest {

    @Mock
    private ModifyShipmentInputMapper modifyShipmentInputMapper;

    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;

    @Mock
    private RecoveredPlasmaShipmentOutputMapper recoveredPlasmaShipmentOutputMapper;

    @InjectMocks
    private ModifyShipmentUseCase modifyShipmentUseCase;

    private ModifyShipmentCommandInput commandInput;
    private ModifyShipmentCommand modifyCommand;
    private RecoveredPlasmaShipment existingShipment;
    private RecoveredPlasmaShipment modifiedShipment;
    private RecoveredPlasmaShipmentOutput shipmentOutput;
    private ShipmentHistory shipmentHistory;

    @BeforeEach
    void setUp() {
        commandInput = ModifyShipmentCommandInput.builder().build();
        modifyCommand = Mockito.mock(ModifyShipmentCommand.class);
        existingShipment = Mockito.mock(RecoveredPlasmaShipment.class);

    }

    @Test
    void modifyShipment_WhenSuccessful_ShouldReturnModifiedShipment() {
        // Arrange
        Mockito.when(modifyCommand.getShipmentId()).thenReturn(1L);
        modifiedShipment = Mockito.mock(RecoveredPlasmaShipment.class);
        shipmentOutput = RecoveredPlasmaShipmentOutput.builder().build();
        shipmentHistory = Mockito.mock(ShipmentHistory.class);
        Mockito.when(modifiedShipment.getShipmentHistory()).thenReturn(shipmentHistory);



        when(modifyShipmentInputMapper.toModifyCommand(commandInput)).thenReturn(modifyCommand);

        when(recoveredPlasmaShippingRepository.findOneById(anyLong())).thenReturn(Mono.just(existingShipment));

        Mockito.when(existingShipment.modifyShipment(Mockito.any(),Mockito.any(CustomerService.class), Mockito.any(RecoveredPlasmaShipmentCriteriaRepository.class))).thenReturn(modifiedShipment);

        when(recoveredPlasmaShippingRepository.createShipmentHistory(any(ShipmentHistory.class))).thenReturn(Mono.just(Mockito.mock(ShipmentHistory.class)));

        when(recoveredPlasmaShippingRepository.update(any(RecoveredPlasmaShipment.class))).thenReturn(Mono.just(modifiedShipment));

        when(recoveredPlasmaShipmentOutputMapper.toRecoveredPlasmaShipmentOutput(modifiedShipment)).thenReturn(shipmentOutput);

        // Act & Assert
        StepVerifier.create(modifyShipmentUseCase.modifyShipment(commandInput))
            .assertNext(useCaseOutput -> {
                assertNotNull(useCaseOutput);
                assertNotNull(useCaseOutput.data());
                assertEquals(1, useCaseOutput.notifications().size());
                assertEquals(UseCaseMessageType.SHIPMENT_MODIFIED_SUCCESS.getMessage(),
                    useCaseOutput.notifications().get(0).useCaseMessage().message());
            })
            .verifyComplete();

        verify(modifyShipmentInputMapper).toModifyCommand(Mockito.any(ModifyShipmentCommandInput.class));
        verify(recoveredPlasmaShippingRepository).findOneById(modifyCommand.getShipmentId());
        verify(recoveredPlasmaShippingRepository).createShipmentHistory(any(ShipmentHistory.class));
        verify(recoveredPlasmaShippingRepository).update(any(RecoveredPlasmaShipment.class));
        verify(recoveredPlasmaShipmentOutputMapper).toRecoveredPlasmaShipmentOutput(modifiedShipment);
    }

    @Test
    void modifyShipment_WhenShipmentNotFound_ShouldReturnError() {
        // Arrange
        when(modifyShipmentInputMapper.toModifyCommand(commandInput))
            .thenReturn(modifyCommand);

        when(recoveredPlasmaShippingRepository.findOneById(modifyCommand.getShipmentId()))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(modifyShipmentUseCase.modifyShipment(commandInput))
            .assertNext(useCaseOutput -> {
                assertNotNull(useCaseOutput);
                assertNull(useCaseOutput.data());
                assertEquals(1, useCaseOutput.notifications().size());
                assertEquals(UseCaseNotificationType.WARN,
                    useCaseOutput.notifications().get(0).useCaseMessage().type());
                assertEquals(26,
                    useCaseOutput.notifications().get(0).useCaseMessage().code());
            })
            .verifyComplete();

        verify(modifyShipmentInputMapper).toModifyCommand(commandInput);
        verify(recoveredPlasmaShippingRepository).findOneById(modifyCommand.getShipmentId());
        verifyNoMoreInteractions(recoveredPlasmaShippingRepository);
        verifyNoInteractions(recoveredPlasmaShipmentOutputMapper);
    }

    @Test
    void modifyShipment_WhenUpdateFails_ShouldReturnError() {
        // Arrange
        Mockito.when(modifyCommand.getShipmentId()).thenReturn(1L);
        modifiedShipment = Mockito.mock(RecoveredPlasmaShipment.class);
        shipmentOutput = RecoveredPlasmaShipmentOutput.builder().build();
        shipmentHistory = Mockito.mock(ShipmentHistory.class);
        Mockito.when(modifiedShipment.getShipmentHistory()).thenReturn(shipmentHistory);

        RuntimeException updateException = new RuntimeException("Update failed");

        when(modifyShipmentInputMapper.toModifyCommand(Mockito.any())).thenReturn(modifyCommand);

        when(recoveredPlasmaShippingRepository.findOneById(anyLong())).thenReturn(Mono.just(existingShipment));

        Mockito.when(existingShipment.modifyShipment(Mockito.any(),Mockito.any(CustomerService.class), Mockito.any(RecoveredPlasmaShipmentCriteriaRepository.class))).thenReturn(modifiedShipment);

        when(recoveredPlasmaShippingRepository.createShipmentHistory(any(ShipmentHistory.class))).thenReturn(Mono.just(Mockito.mock(ShipmentHistory.class)));

        when(recoveredPlasmaShippingRepository.update(any(RecoveredPlasmaShipment.class))).thenReturn(Mono.error(updateException));

        // Act & Assert
        StepVerifier.create(modifyShipmentUseCase.modifyShipment(commandInput))
            .assertNext(useCaseOutput -> {
                assertNotNull(useCaseOutput);
                assertNull(useCaseOutput.data());
                assertEquals(1, useCaseOutput.notifications().size());
                assertEquals(UseCaseNotificationType.WARN,
                    useCaseOutput.notifications().get(0).useCaseMessage().type());
                assertEquals("Update failed",
                    useCaseOutput.notifications().get(0).useCaseMessage().message());
            })
            .verifyComplete();

        verify(modifyShipmentInputMapper).toModifyCommand(commandInput);
        verify(recoveredPlasmaShippingRepository).findOneById(modifyCommand.getShipmentId());
        verify(recoveredPlasmaShippingRepository).createShipmentHistory(any(ShipmentHistory.class));
        verify(recoveredPlasmaShippingRepository).update(any(RecoveredPlasmaShipment.class));
        verifyNoInteractions(recoveredPlasmaShipmentOutputMapper);
    }

    @Test
    void modifyShipment_WhenMapperThrowsException_ShouldReturnError() {
        // Arrange
        RuntimeException mapperException = new RuntimeException("Mapping failed");

        when(modifyShipmentInputMapper.toModifyCommand(commandInput))
            .thenThrow(mapperException);

        // Act & Assert
        StepVerifier.create(modifyShipmentUseCase.modifyShipment(commandInput))
            .assertNext(useCaseOutput -> {
                assertNotNull(useCaseOutput);
                assertNull(useCaseOutput.data());
                assertEquals(1, useCaseOutput.notifications().size());
                assertEquals(UseCaseNotificationType.WARN,
                    useCaseOutput.notifications().get(0).useCaseMessage().type());
                assertEquals("Mapping failed",
                    useCaseOutput.notifications().get(0).useCaseMessage().message());
            })
            .verifyComplete();

        verify(modifyShipmentInputMapper).toModifyCommand(commandInput);
        verifyNoInteractions(recoveredPlasmaShippingRepository, recoveredPlasmaShipmentOutputMapper);
    }
}
