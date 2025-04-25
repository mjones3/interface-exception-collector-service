package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.VerifyItemCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.VerifyCartonItemUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception.ProductCriteriaValidationException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception.ProductValidationException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryNotification;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.VerifyItemCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.InventoryService;
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

@ExtendWith(MockitoExtension.class)
class VerifyCartonItemUseCaseTest {


    @Mock
    private InventoryService inventoryService;
    @Mock
    private CartonItemRepository cartonItemRepository;
    @Mock
    private RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;
    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    @Mock
    private CartonRepository cartonRepository;
    @Mock
    private CartonOutputMapper cartonOutputMapper;

    @InjectMocks
    private VerifyCartonItemUseCase verifyCartonItemUseCase;


    @Test
    void shouldSuccessfullyVerifyCartonItem() {
        // Arrange
        Long cartonId = 1L;
        String unitNumber = "UNIT001";
        String productCode = "PROD001";
        String employeeId = "EMP001";
        String locationCode = "LOC001";

        VerifyItemCommandInput input = new VerifyItemCommandInput(cartonId, unitNumber,
            productCode, employeeId, locationCode);

        Carton carton = Mockito.mock(Carton.class);
        CartonItem cartonItem = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem.getCartonId()).thenReturn(cartonId);
        CartonOutput cartonOutput = Mockito.mock(CartonOutput.class);

        Mockito.when(carton.verifyItem(Mockito.any(VerifyItemCommand.class),Mockito.any(InventoryService.class), Mockito.any(CartonItemRepository.class)
            , Mockito.any(RecoveredPlasmaShipmentCriteriaRepository.class), Mockito.any(RecoveredPlasmaShippingRepository.class))).thenReturn(cartonItem);

        Mockito.when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.just(carton));
        Mockito.when(cartonItemRepository.save(Mockito.any(CartonItem.class))).thenReturn(Mono.just(cartonItem));
        Mockito.when(cartonOutputMapper.toOutput(Mockito.any(Carton.class))).thenReturn(cartonOutput);

        // Act
        StepVerifier.create(verifyCartonItemUseCase.verifyCartonItem(input))
            .assertNext(output -> {
                // Assert
                assertNotNull(output);
                assertEquals(1, output.notifications().size());
                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(UseCaseMessageType.VERIFY_CARTON_ITEM_SUCCESS.getCode(),
                    notification.useCaseMessage().code());
                assertEquals(cartonOutput, output.data());


            })
            .verifyComplete();
    }

    @Test
    void shouldHandleCartonNotFound() {
        // Arrange
        Long cartonId = 1L;
        VerifyItemCommandInput input = new VerifyItemCommandInput(cartonId, "UNIT001",
            "PROD001", "EMP001", "LOC001");

        Mockito.when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.empty());
        Mockito.when(cartonItemRepository.deleteAllByCartonId(Mockito.anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(verifyCartonItemUseCase.verifyCartonItem(input))
            .assertNext(output -> {
                // Assert
                assertNotNull(output);
                assertEquals(1, output.notifications().size());
                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(9, notification.useCaseMessage().code());
                assertEquals(UseCaseNotificationType.SYSTEM,
                    notification.useCaseMessage().type());
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleProductValidationException() {

        // Arrange
        Long cartonId = 1L;
        String unitNumber = "UNIT001";
        String productCode = "PROD001";
        String employeeId = "EMP001";
        String locationCode = "LOC001";

        VerifyItemCommandInput input = new VerifyItemCommandInput(cartonId, unitNumber,
            productCode, employeeId, locationCode);

        Carton carton = Mockito.mock(Carton.class);

        ProductValidationException validationException = new ProductValidationException("Validation failed","WARN");

        Mockito.when(carton.verifyItem(Mockito.any(VerifyItemCommand.class),Mockito.any(InventoryService.class), Mockito.any(CartonItemRepository.class)
            , Mockito.any(RecoveredPlasmaShipmentCriteriaRepository.class), Mockito.any(RecoveredPlasmaShippingRepository.class))).thenThrow(validationException);

        Mockito.when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.just(carton));

        Mockito.when(cartonItemRepository.deleteAllByCartonId(Mockito.anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(verifyCartonItemUseCase.verifyCartonItem(input))
            .assertNext(output -> {
                // Assert
                assertNotNull(output);
                assertEquals(1, output.notifications().size());
                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(11, notification.useCaseMessage().code());
                assertEquals("Validation failed", notification.useCaseMessage().message());
                assertEquals(UseCaseNotificationType.WARN,notification.useCaseMessage().type());
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleProductValidationExceptionWhenInventoryNotification() {

        // Arrange
        Long cartonId = 1L;
        String unitNumber = "UNIT001";
        String productCode = "PROD001";
        String employeeId = "EMP001";
        String locationCode = "LOC001";

        VerifyItemCommandInput input = new VerifyItemCommandInput(cartonId, unitNumber,
            productCode, employeeId, locationCode);

        Carton carton = Mockito.mock(Carton.class);

        InventoryValidation inventoryValidation = Mockito.mock(InventoryValidation.class);

        ProductValidationException validationException = Mockito.mock(ProductValidationException.class);
        Mockito.when(validationException.getInventoryValidation()).thenReturn(inventoryValidation);
        Mockito.when(validationException.getMessage()).thenReturn("Inventory Validation failed");
        Mockito.when(validationException.getErrorType()).thenReturn("INFO");

        Mockito.when(carton.verifyItem(Mockito.any(VerifyItemCommand.class),Mockito.any(InventoryService.class), Mockito.any(CartonItemRepository.class)
            , Mockito.any(RecoveredPlasmaShipmentCriteriaRepository.class), Mockito.any(RecoveredPlasmaShippingRepository.class))).thenThrow(validationException);

        Mockito.when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.just(carton));

        Mockito.when(cartonItemRepository.deleteAllByCartonId(Mockito.anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(verifyCartonItemUseCase.verifyCartonItem(input))
            .assertNext(output -> {
                // Assert
                assertNotNull(output);
                assertEquals(1, output.notifications().size());
                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(11, notification.useCaseMessage().code());
                assertEquals("Inventory Validation failed", notification.useCaseMessage().message());
                assertEquals(UseCaseNotificationType.INFO,notification.useCaseMessage().type());
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleProductCriteriaValidationException() {

        // Arrange
        Long cartonId = 1L;
        String unitNumber = "UNIT001";
        String productCode = "PROD001";
        String employeeId = "EMP001";
        String locationCode = "LOC001";

        VerifyItemCommandInput input = new VerifyItemCommandInput(cartonId, unitNumber,
            productCode, employeeId, locationCode);

        Carton carton = Mockito.mock(Carton.class);

        ProductCriteriaValidationException validationException = new ProductCriteriaValidationException("Validation criteria failed","WARN","NAME");

        Mockito.when(carton.verifyItem(Mockito.any(VerifyItemCommand.class),Mockito.any(InventoryService.class), Mockito.any(CartonItemRepository.class)
            , Mockito.any(RecoveredPlasmaShipmentCriteriaRepository.class), Mockito.any(RecoveredPlasmaShippingRepository.class))).thenThrow(validationException);

        Mockito.when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.just(carton));

        Mockito.when(cartonItemRepository.deleteAllByCartonId(Mockito.anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(verifyCartonItemUseCase.verifyCartonItem(input))
            .assertNext(output -> {
                // Assert
                assertNotNull(output);
                assertEquals(1, output.notifications().size());
                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(12, notification.useCaseMessage().code());
                assertEquals("Validation criteria failed", notification.useCaseMessage().message());
                assertEquals("NAME", notification.useCaseMessage().name());
                assertEquals(UseCaseNotificationType.WARN,notification.useCaseMessage().type());
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleGenericException() {

        // Arrange
        Long cartonId = 1L;
        String unitNumber = "UNIT001";
        String productCode = "PROD001";
        String employeeId = "EMP001";
        String locationCode = "LOC001";

        VerifyItemCommandInput input = new VerifyItemCommandInput(cartonId, unitNumber,
            productCode, employeeId, locationCode);

        Carton carton = Mockito.mock(Carton.class);

        IllegalArgumentException validationException = new IllegalArgumentException("IllegalArgumentException");

        Mockito.when(carton.verifyItem(Mockito.any(VerifyItemCommand.class),Mockito.any(InventoryService.class), Mockito.any(CartonItemRepository.class)
            , Mockito.any(RecoveredPlasmaShipmentCriteriaRepository.class), Mockito.any(RecoveredPlasmaShippingRepository.class))).thenThrow(validationException);

        Mockito.when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.just(carton));

        Mockito.when(cartonItemRepository.deleteAllByCartonId(Mockito.anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(verifyCartonItemUseCase.verifyCartonItem(input))
            .assertNext(output -> {
                // Assert
                assertNotNull(output);
                assertEquals(1, output.notifications().size());
                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(13, notification.useCaseMessage().code());
                assertEquals("IllegalArgumentException", notification.useCaseMessage().message());
                assertEquals(UseCaseNotificationType.SYSTEM,notification.useCaseMessage().type());
            })
            .verifyComplete();
    }


}
