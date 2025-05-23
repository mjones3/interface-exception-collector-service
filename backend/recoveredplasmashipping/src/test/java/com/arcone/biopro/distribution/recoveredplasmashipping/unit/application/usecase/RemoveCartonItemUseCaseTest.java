package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RemoveCartonItemCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.RemoveCartonItemUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RemoveCartonItemCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveCartonItemUseCaseTest {

    @Mock
    private CartonRepository cartonRepository;

    @Mock
    private CartonOutputMapper cartonOutputMapper;

    @Mock
    private CartonItemRepository cartonItemRepository;

    @InjectMocks
    private RemoveCartonItemUseCase removeCartonItemUseCase;

    @Test
    void shouldSuccessfullyRemoveCartonItem() {
        // Arrange
        Long cartonId = 123L;
        String employeeId = "emp123";
        List<Long> cartonItemIds = List.of(1L, 2L);

        RemoveCartonItemCommandInput commandInput = new RemoveCartonItemCommandInput(cartonId, employeeId, cartonItemIds);

        Carton carton = mock(Carton.class);
        CartonItem cartonItem1 = mock(CartonItem.class);
        CartonItem cartonItem2 = mock(CartonItem.class);
        List<CartonItem> cartonItems = List.of(cartonItem1, cartonItem2);
        CartonOutput cartonOutput = mock(CartonOutput.class);

        // Mock behaviors
        when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.just(carton));
        when(carton.removeCartonItem(any(RemoveCartonItemCommand.class))).thenReturn(cartonItems);
        when(carton.getVerifiedProducts()).thenReturn(cartonItems);
        when(cartonItem1.getId()).thenReturn(1L);
        when(cartonItem2.getId()).thenReturn(2L);
        when(cartonItem1.resetVerification()).thenReturn(cartonItem1);
        when(cartonItem2.resetVerification()).thenReturn(cartonItem2);
        when(cartonItemRepository.deleteOneById(Mockito.anyLong())).thenReturn(Mono.empty());
        when(cartonItemRepository.save(any(CartonItem.class))).thenReturn(Mono.just(mock(CartonItem.class)));
        when(cartonOutputMapper.toOutput(carton)).thenReturn(cartonOutput);

        // Act
        StepVerifier.create(removeCartonItemUseCase.removeCartonItem(commandInput))
            .assertNext(result -> {
                // Assert
                assertNotNull(result);
                assertEquals(cartonOutput, result.data());
                assertEquals(1, result.notifications().size());
                assertEquals(UseCaseMessageType.CARTON_ITEM_REMOVED_SUCCESS.getCode(),
                    result.notifications().get(0).useCaseMessage().code());
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenCartonNotFound() {
        // Arrange
        Long cartonId = 123L;
        String employeeId = "emp123";
        List<Long> cartonItemIds = List.of(1L, 2L);

        RemoveCartonItemCommandInput commandInput = new RemoveCartonItemCommandInput(cartonId, employeeId, cartonItemIds);

        when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(removeCartonItemUseCase.removeCartonItem(commandInput))
            .assertNext(result -> {
                assertNotNull(result);
                assertNull(result.data());
                assertEquals(1, result.notifications().size());
                assertEquals(UseCaseMessageType.CARTON_ITEM_REMOVED_ERROR.getCode(),
                    result.notifications().get(0).useCaseMessage().code());
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleErrorDuringRemoval() {
        // Arrange
        Long cartonId = 123L;
        String employeeId = "emp123";
        List<Long> cartonItemIds = List.of(1L);

        RemoveCartonItemCommandInput commandInput = new RemoveCartonItemCommandInput(cartonId, employeeId, cartonItemIds);

        Carton carton = mock(Carton.class);

        when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.just(carton));
        when(carton.removeCartonItem(any(RemoveCartonItemCommand.class)))
            .thenThrow(new RuntimeException("Error removing item"));

        // Act & Assert
        StepVerifier.create(removeCartonItemUseCase.removeCartonItem(commandInput))
            .assertNext(result -> {
                assertNotNull(result);
                assertNull(result.data());
                assertEquals(1, result.notifications().size());
                assertEquals(UseCaseMessageType.CARTON_ITEM_REMOVED_ERROR.getCode(),
                    result.notifications().get(0).useCaseMessage().code());
            })
            .verifyComplete();
    }
}

