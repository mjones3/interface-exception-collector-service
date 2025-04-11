package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Inventory;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.PackItemCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ProductType;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentCriteria;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.InventoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class CartonItemTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private CartonItemRepository cartonItemRepository;

    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;

    @Mock
    private RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;

    private PackItemCommand packItemCommand;

    @BeforeEach
    public void setUp(){
        packItemCommand = new PackItemCommand(1L,"UNIT_NUMBER","PRODUCT_CODE","EMPLOYEE_ID","LOCATION_CODE");
    }

    @Test
    public void shouldCreateCartonItem(){

        var cartonMock = Mockito.mock(Carton.class);
        var shipment = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(shipment.getProductType()).thenReturn("PRODUCT_TYPE");
        var productType = Mockito.mock(ProductType.class);
        Mockito.when(productType.getProductType()).thenReturn("PRODUCT_TYPE");

        var inventoryValidation = Mockito.mock(InventoryValidation.class);
        var inventory = Mockito.mock(Inventory.class);
        Mockito.when(inventoryValidation.getInventory()).thenReturn(inventory);

        var productCriteria = Mockito.mock(RecoveredPlasmaShipmentCriteria.class);

        Mockito.when(cartonItemRepository.countByProduct(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));
        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipment));
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductTypeByProductCode(Mockito.anyString())).thenReturn(Mono.just(productType));
        Mockito.when(inventoryService.validateInventory(Mockito.any())).thenReturn(Mono.just(inventoryValidation));
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(productCriteria));



        var cartonItem = CartonItem.createNewCartonItem(packItemCommand, cartonMock , inventoryService, cartonItemRepository, recoveredPlasmaShippingRepository , recoveredPlasmaShipmentCriteriaRepository);

        Assertions.assertNotNull(cartonItem);

    }



}
