package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PackCartonItemCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.PackCartonItemUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception.ProductCriteriaValidationException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception.ProductValidationException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryNotification;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.PackItemCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.InventoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackCartonItemUseCaseTest {

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

    private CartonOutputMapper cartonOutputMapper = Mappers.getMapper(CartonOutputMapper.class);


    private PackCartonItemUseCase packCartonItemUseCase;

    @BeforeEach
    public void setUp() {
        packCartonItemUseCase = new PackCartonItemUseCase(inventoryService, cartonItemRepository, recoveredPlasmaShipmentCriteriaRepository, recoveredPlasmaShippingRepository, cartonRepository, cartonOutputMapper);
    }

    @Test
    void shouldSuccessfullyPackCartonItem() {
        // Arrange
        Long cartonId = 123L;
        String unitNumber = "UNIT001";
        String productCode = "PROD001";
        String employeeId = "EMP001";
        String locationCode = "LOC001";

        PackCartonItemCommandInput input = new PackCartonItemCommandInput(
            cartonId, unitNumber, productCode, employeeId, locationCode
        );

        Carton carton = Mockito.mock(Carton.class);
        CartonItem cartonItem = Mockito.mock(CartonItem.class);


        Mockito.when(cartonRepository.findOneById(any())).thenReturn(Mono.just(carton));
        when(cartonItemRepository.save(any(CartonItem.class))).thenReturn(Mono.just(cartonItem));

        when(carton.packItem(any(PackItemCommand.class),Mockito.any(InventoryService.class),Mockito.any(CartonItemRepository.class),Mockito.any(RecoveredPlasmaShipmentCriteriaRepository.class),Mockito.any(RecoveredPlasmaShippingRepository.class))).thenReturn(cartonItem);



        // When
        StepVerifier.create(packCartonItemUseCase.packItem(input))
            // Assert
            .assertNext(output -> {
                Assertions.assertNotNull(output);
                assertNotNull(output.data());
                assertEquals(1, output.notifications().size());
                assertEquals(UseCaseMessageType.CARTON_ITEM_PACKED_SUCCESS.getCode(), output.notifications().get(0).useCaseMessage().code());
                assertEquals(UseCaseMessageType.CARTON_ITEM_PACKED_SUCCESS.getMessage(), output.notifications().get(0).useCaseMessage().message());
            })
            .verifyComplete();


    }

    @Test
    void shouldHandleCartonNotFoundError() {
        // Arrange
        Long cartonId = 123L;
        PackCartonItemCommandInput input = new PackCartonItemCommandInput(
            cartonId, "UNIT001", "PROD001", "EMP001", "LOC001"
        );



        when(cartonRepository.findOneById(any())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(packCartonItemUseCase.packItem(input))
            .assertNext(output -> {
                assertNull(output.data());
                assertNotNull(output.notifications());
                assertEquals(1, output.notifications().size());
                assertEquals(10, output.notifications().get(0).useCaseMessage().code());
                assertEquals("Carton Item packed error. Contact Support.", output.notifications().get(0).useCaseMessage().message());
                assertEquals(UseCaseNotificationType.SYSTEM,output.notifications().get(0).useCaseMessage().type());
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleProductValidationException() {
        // Arrange
        Long cartonId = 123L;
        PackCartonItemCommandInput input = new PackCartonItemCommandInput(
            cartonId, "UNIT001", "PROD001", "EMP001", "LOC001"
        );

        Carton carton = Mockito.mock(Carton.class);
        InventoryValidation inventoryValidation = Mockito.mock(InventoryValidation.class);

        var notification = Mockito.mock(InventoryNotification.class);

        Mockito.when(inventoryValidation.getFirstNotification()).thenReturn(notification);
        Mockito.when(notification.getAction()).thenReturn("ACTION");
        Mockito.when(notification.getReason()).thenReturn("REASON");
        Mockito.when(notification.getErrorMessage()).thenReturn("ERROR_MESSAGE");
        Mockito.when(notification.getErrorType()).thenReturn("INFO");
        Mockito.when(notification.getDetails()).thenReturn(List.of("ERROR1","ERROR_2"));


        ProductValidationException productValidationException = new ProductValidationException("Validation failed", inventoryValidation,"WARN");

        when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.just(carton));
        when(carton.packItem(any(PackItemCommand.class),Mockito.any(InventoryService.class),Mockito.any(CartonItemRepository.class),Mockito.any(RecoveredPlasmaShipmentCriteriaRepository.class)
            ,Mockito.any(RecoveredPlasmaShippingRepository.class))).thenThrow(productValidationException);


        // Act & Assert
        StepVerifier.create(packCartonItemUseCase.packItem(input))
            .assertNext(output -> {
                assertNotNull(output);
                assertNotNull(output.notifications());
                assertEquals(1, output.notifications().size());
                assertEquals(6, output.notifications().get(0).useCaseMessage().code());
                assertEquals("ACTION", output.notifications().get(0).useCaseMessage().action());
                assertEquals("REASON", output.notifications().get(0).useCaseMessage().reason());
                assertEquals("ERROR_MESSAGE", output.notifications().get(0).useCaseMessage().message());
                assertEquals(List.of("ERROR1","ERROR_2"), output.notifications().get(0).useCaseMessage().details());
                assertEquals(UseCaseNotificationType.INFO, output.notifications().get(0).useCaseMessage().type());
            })
            .verifyComplete();
    }


    @Test
    void shouldHandleProductCriteriaValidationException() {
        // Arrange
        Long cartonId = 123L;
        PackCartonItemCommandInput input = new PackCartonItemCommandInput(
            cartonId, "UNIT001", "PROD001", "EMP001", "LOC001"
        );

        Carton carton = Mockito.mock(Carton.class);
        ProductCriteriaValidationException exception =
            new ProductCriteriaValidationException("Criteria validation failed","WARN","ERROR_TYPE");

        when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.just(carton));

        when(carton.packItem(any(PackItemCommand.class),Mockito.any(InventoryService.class),Mockito.any(CartonItemRepository.class),Mockito.any(RecoveredPlasmaShipmentCriteriaRepository.class)
            ,Mockito.any(RecoveredPlasmaShippingRepository.class))).thenThrow(exception);


        // Act & Assert
        StepVerifier.create(packCartonItemUseCase.packItem(input))
            .assertNext(output -> {
                assertNotNull(output);
                assertNotNull(output.notifications());
                assertEquals(1, output.notifications().size());
                assertEquals(7, output.notifications().get(0).useCaseMessage().code());
                assertEquals(UseCaseNotificationType.WARN, output.notifications().get(0).useCaseMessage().type());
                assertEquals(exception.getMessage(), output.notifications().get(0).useCaseMessage().message());
                assertEquals(exception.getErrorName(), output.notifications().get(0).useCaseMessage().name());
            })
            .verifyComplete();
    }


}

