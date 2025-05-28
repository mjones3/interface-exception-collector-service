package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception.ProductCriteriaValidationException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception.ProductValidationException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Inventory;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.PackItemCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ProductType;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentCriteria;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShipmentCustomer;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.InventoryVolume;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.RecoveredPlasmaShipmentCriteriaItem;
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

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        Mockito.when(cartonMock.getId()).thenReturn(1L);

        var shipment = Mockito.mock(RecoveredPlasmaShipment.class);

        var mockCustomer = Mockito.mock(ShipmentCustomer.class);
        Mockito.when(mockCustomer.getCustomerCode()).thenReturn("CUSTOMER_CODE");
        Mockito.when(shipment.getShipmentCustomer()).thenReturn(mockCustomer);

        Mockito.when(shipment.getProductType()).thenReturn("PRODUCT_TYPE");
        var productType = Mockito.mock(ProductType.class);
        Mockito.when(productType.getProductType()).thenReturn("PRODUCT_TYPE");

        var inventoryValidation = Mockito.mock(InventoryValidation.class);
        var inventory = Mockito.mock(Inventory.class);
        Mockito.when(inventory.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(inventory.getProductCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(inventory.getVolumeByType("volume")).thenReturn(Optional.of(new InventoryVolume("volume",160,"ML")));
        Mockito.when(inventory.getWeight()).thenReturn(10);
        Mockito.when(inventory.getProductDescription()).thenReturn("DESCRIPTION");
        Mockito.when(inventory.getAboRh()).thenReturn("AP");
        Mockito.when(inventory.getExpirationDate()).thenReturn(LocalDateTime.now());
        Mockito.when(inventory.getCollectionDate()).thenReturn(ZonedDateTime.now());

        Mockito.when(inventoryValidation.getInventory()).thenReturn(inventory);

        var productCriteria = Mockito.mock(RecoveredPlasmaShipmentCriteria.class);

        var criteriaItemVolume = Mockito.mock(RecoveredPlasmaShipmentCriteriaItem.class);
        Mockito.when(criteriaItemVolume.getValue()).thenReturn("150");

        var criteriaItemMaxUnits = Mockito.mock(RecoveredPlasmaShipmentCriteriaItem.class);
        Mockito.when(criteriaItemMaxUnits.getValue()).thenReturn("10");

        Mockito.when(productCriteria.findCriteriaItemByType("MINIMUM_VOLUME")).thenReturn(Optional.of(criteriaItemVolume));
        Mockito.when(productCriteria.findCriteriaItemByType("MAXIMUM_UNITS_BY_CARTON")).thenReturn(Optional.of(criteriaItemMaxUnits));

        Mockito.when(cartonItemRepository.countByProduct(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));
        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipment));
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductTypeByProductCode(Mockito.anyString())).thenReturn(Mono.just(productType));
        Mockito.when(inventoryService.validateInventory(Mockito.any())).thenReturn(Mono.just(inventoryValidation));
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(productCriteria));



        var cartonItem = CartonItem.createNewCartonItem(packItemCommand, cartonMock , inventoryService, cartonItemRepository, recoveredPlasmaShippingRepository , recoveredPlasmaShipmentCriteriaRepository);

        Assertions.assertNotNull(cartonItem);
        Assertions.assertNull(cartonItem.getId());
        Assertions.assertEquals(1L, cartonItem.getCartonId());
        Assertions.assertEquals("UNIT_NUMBER", cartonItem.getUnitNumber());
        Assertions.assertEquals("PRODUCT_CODE", cartonItem.getProductCode());
        Assertions.assertEquals("DESCRIPTION", cartonItem.getProductDescription());
        Assertions.assertEquals("PRODUCT_TYPE", cartonItem.getProductType());
        Assertions.assertEquals(160, cartonItem.getVolume(), 0.001);
        Assertions.assertEquals(10, cartonItem.getWeight(), 0.001);
        Assertions.assertEquals("PACKED", cartonItem.getStatus());
        Assertions.assertEquals("EMPLOYEE_ID", cartonItem.getPackedByEmployeeId());
        Assertions.assertEquals("AP", cartonItem.getAboRh());
        Assertions.assertNotNull(cartonItem.getExpirationDate());
        Assertions.assertNotNull(cartonItem.getCollectionDate());
        Assertions.assertNotNull(cartonItem.getCreateDate());
        Assertions.assertNull(cartonItem.getModificationDate());

    }

    @Test
    public void shouldNotCreateCartonItemWhenItemIsAlreadyPacked(){

        var cartonMock = Mockito.mock(Carton.class);



        Mockito.when(cartonItemRepository.countByProduct(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(1));

        ProductValidationException exception = assertThrows(ProductValidationException.class,
            () -> CartonItem.createNewCartonItem(packItemCommand, cartonMock , inventoryService, cartonItemRepository, recoveredPlasmaShippingRepository , recoveredPlasmaShipmentCriteriaRepository));
        assertEquals("Product already added in a carton", exception.getMessage());

    }

    @Test
    public void shouldNotCreateCartonItemWhenShipmentNotFound(){

        var cartonMock = Mockito.mock(Carton.class);

        Mockito.when(cartonItemRepository.countByProduct(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> CartonItem.createNewCartonItem(packItemCommand, cartonMock , inventoryService, cartonItemRepository, recoveredPlasmaShippingRepository , recoveredPlasmaShipmentCriteriaRepository));
        assertEquals("Shipment is required", exception.getMessage());

    }

    @Test
    public void shouldNotCreateCartonItemWhenProductTypeDoesNotMatch(){

        var cartonMock = Mockito.mock(Carton.class);

        var shipment = Mockito.mock(RecoveredPlasmaShipment.class);

        Mockito.when(cartonItemRepository.countByProduct(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipment));

        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductTypeByProductCode(Mockito.anyString())).thenReturn(Mono.empty());

        ProductValidationException exception = assertThrows(ProductValidationException.class,
            () -> CartonItem.createNewCartonItem(packItemCommand, cartonMock , inventoryService, cartonItemRepository, recoveredPlasmaShippingRepository , recoveredPlasmaShipmentCriteriaRepository));
        assertEquals("Product Type does not match", exception.getMessage());

    }

    @Test
    public void shouldNotCreateCartonItemWhenInventoryValidationFails(){

        var cartonMock = Mockito.mock(Carton.class);

        var shipment = Mockito.mock(RecoveredPlasmaShipment.class);

        Mockito.when(shipment.getProductType()).thenReturn("PRODUCT_TYPE");
        var productType = Mockito.mock(ProductType.class);
        Mockito.when(productType.getProductType()).thenReturn("PRODUCT_TYPE");

        Mockito.when(cartonItemRepository.countByProduct(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipment));

        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductTypeByProductCode(Mockito.anyString())).thenReturn(Mono.just(productType));

        var inventoryValidation = Mockito.mock(InventoryValidation.class);

        Mockito.when(inventoryService.validateInventory(Mockito.any())).thenReturn(Mono.just(inventoryValidation));

        ProductValidationException exception = assertThrows(ProductValidationException.class,
            () -> CartonItem.createNewCartonItem(packItemCommand, cartonMock , inventoryService, cartonItemRepository, recoveredPlasmaShippingRepository , recoveredPlasmaShipmentCriteriaRepository));
        assertEquals("Inventory Validation failed", exception.getMessage());

    }

    @Test
    public void shouldNotCreateCartonItemWhenVolumeDoesNotMetCriteria(){

        var cartonMock = Mockito.mock(Carton.class);

        var shipment = Mockito.mock(RecoveredPlasmaShipment.class);

        var mockCustomer = Mockito.mock(ShipmentCustomer.class);
        Mockito.when(mockCustomer.getCustomerCode()).thenReturn("CUSTOMER_CODE");
        Mockito.when(shipment.getShipmentCustomer()).thenReturn(mockCustomer);

        Mockito.when(shipment.getProductType()).thenReturn("PRODUCT_TYPE");
        var productType = Mockito.mock(ProductType.class);
        Mockito.when(productType.getProductType()).thenReturn("PRODUCT_TYPE");

        Mockito.when(cartonItemRepository.countByProduct(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipment));


        var inventoryValidation = Mockito.mock(InventoryValidation.class);
        var inventory = Mockito.mock(Inventory.class);
        Mockito.when(inventory.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(inventory.getProductCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(inventory.getVolumeByType("volume")).thenReturn(Optional.of(new InventoryVolume("volume",160,"ML")));
        Mockito.when(inventory.getWeight()).thenReturn(10);
        Mockito.when(inventory.getProductDescription()).thenReturn("DESCRIPTION");
        Mockito.when(inventory.getAboRh()).thenReturn("AP");
        Mockito.when(inventory.getExpirationDate()).thenReturn(LocalDateTime.now());
        Mockito.when(inventory.getCollectionDate()).thenReturn(ZonedDateTime.now());

        Mockito.when(inventoryValidation.getInventory()).thenReturn(inventory);

        var productCriteria = Mockito.mock(RecoveredPlasmaShipmentCriteria.class);

        var criteriaItemVolume = Mockito.mock(RecoveredPlasmaShipmentCriteriaItem.class);
        Mockito.when(criteriaItemVolume.getValue()).thenReturn("200");
        Mockito.when(criteriaItemVolume.getMessage()).thenReturn("ERROR VOLUME");
        Mockito.when(criteriaItemVolume.getMessageType()).thenReturn("WARN");


        Mockito.when(productCriteria.findCriteriaItemByType("MINIMUM_VOLUME")).thenReturn(Optional.of(criteriaItemVolume));

        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductTypeByProductCode(Mockito.anyString())).thenReturn(Mono.just(productType));

        Mockito.when(inventoryService.validateInventory(Mockito.any())).thenReturn(Mono.just(inventoryValidation));

        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(productCriteria));

        ProductCriteriaValidationException exception = assertThrows(ProductCriteriaValidationException.class,
            () -> CartonItem.createNewCartonItem(packItemCommand, cartonMock , inventoryService, cartonItemRepository, recoveredPlasmaShippingRepository , recoveredPlasmaShipmentCriteriaRepository));
        assertEquals("ERROR VOLUME", exception.getMessage());
        assertEquals("WARN", exception.getErrorType());



    }

    @Test
    public void shouldNotCreateCartonItemWhenTotalProductsExceedsCriteria(){

        var cartonMock = Mockito.mock(Carton.class);
        Mockito.when(cartonMock.getTotalProducts()).thenReturn(10);

        var shipment = Mockito.mock(RecoveredPlasmaShipment.class);

        var mockCustomer = Mockito.mock(ShipmentCustomer.class);
        Mockito.when(mockCustomer.getCustomerCode()).thenReturn("CUSTOMER_CODE");
        Mockito.when(shipment.getShipmentCustomer()).thenReturn(mockCustomer);

        Mockito.when(shipment.getProductType()).thenReturn("PRODUCT_TYPE");
        var productType = Mockito.mock(ProductType.class);
        Mockito.when(productType.getProductType()).thenReturn("PRODUCT_TYPE");

        Mockito.when(cartonItemRepository.countByProduct(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipment));


        var inventoryValidation = Mockito.mock(InventoryValidation.class);
        var inventory = Mockito.mock(Inventory.class);
        Mockito.when(inventory.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(inventory.getProductCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(inventory.getVolumeByType("volume")).thenReturn(Optional.of(new InventoryVolume("volume",160,"ML")));
        Mockito.when(inventory.getWeight()).thenReturn(10);
        Mockito.when(inventory.getProductDescription()).thenReturn("DESCRIPTION");
        Mockito.when(inventory.getAboRh()).thenReturn("AP");
        Mockito.when(inventory.getExpirationDate()).thenReturn(LocalDateTime.now());
        Mockito.when(inventory.getCollectionDate()).thenReturn(ZonedDateTime.now());

        Mockito.when(inventoryValidation.getInventory()).thenReturn(inventory);

        var productCriteria = Mockito.mock(RecoveredPlasmaShipmentCriteria.class);

        var criteriaItemVolume = Mockito.mock(RecoveredPlasmaShipmentCriteriaItem.class);
        Mockito.when(criteriaItemVolume.getValue()).thenReturn("150");

        var criteriaItemMaxUnits = Mockito.mock(RecoveredPlasmaShipmentCriteriaItem.class);
        Mockito.when(criteriaItemMaxUnits.getValue()).thenReturn("10");
        Mockito.when(criteriaItemMaxUnits.getMessage()).thenReturn("ERROR MAX UNITS");
        Mockito.when(criteriaItemMaxUnits.getMessageType()).thenReturn("WARN");

        Mockito.when(productCriteria.findCriteriaItemByType("MINIMUM_VOLUME")).thenReturn(Optional.of(criteriaItemVolume));
        Mockito.when(productCriteria.findCriteriaItemByType("MAXIMUM_UNITS_BY_CARTON")).thenReturn(Optional.of(criteriaItemMaxUnits));

        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductTypeByProductCode(Mockito.anyString())).thenReturn(Mono.just(productType));

        Mockito.when(inventoryService.validateInventory(Mockito.any())).thenReturn(Mono.just(inventoryValidation));

        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(productCriteria));

        ProductCriteriaValidationException exception = assertThrows(ProductCriteriaValidationException.class,
            () -> CartonItem.createNewCartonItem(packItemCommand, cartonMock , inventoryService, cartonItemRepository, recoveredPlasmaShippingRepository , recoveredPlasmaShipmentCriteriaRepository));
        assertEquals("ERROR MAX UNITS", exception.getMessage());
        assertEquals("WARN", exception.getErrorType());



    }

    @Test
    public void shouldNotCreateCartonItemWhenCriteriaNotFound(){

        var cartonMock = Mockito.mock(Carton.class);
        Mockito.when(cartonMock.getTotalProducts()).thenReturn(10);

        var shipment = Mockito.mock(RecoveredPlasmaShipment.class);

        var mockCustomer = Mockito.mock(ShipmentCustomer.class);
        Mockito.when(mockCustomer.getCustomerCode()).thenReturn("CUSTOMER_CODE");
        Mockito.when(shipment.getShipmentCustomer()).thenReturn(mockCustomer);

        Mockito.when(shipment.getProductType()).thenReturn("PRODUCT_TYPE");
        var productType = Mockito.mock(ProductType.class);
        Mockito.when(productType.getProductType()).thenReturn("PRODUCT_TYPE");

        Mockito.when(cartonItemRepository.countByProduct(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipment));


        var inventoryValidation = Mockito.mock(InventoryValidation.class);
        var inventory = Mockito.mock(Inventory.class);
        Mockito.when(inventory.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(inventory.getProductCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(inventory.getVolumeByType("volume")).thenReturn(Optional.of(new InventoryVolume("volume",160,"ML")));
        Mockito.when(inventory.getWeight()).thenReturn(10);
        Mockito.when(inventory.getProductDescription()).thenReturn("DESCRIPTION");
        Mockito.when(inventory.getAboRh()).thenReturn("AP");
        Mockito.when(inventory.getExpirationDate()).thenReturn(LocalDateTime.now());
        Mockito.when(inventory.getCollectionDate()).thenReturn(ZonedDateTime.now());

        Mockito.when(inventoryValidation.getInventory()).thenReturn(inventory);

        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductTypeByProductCode(Mockito.anyString())).thenReturn(Mono.just(productType));

        Mockito.when(inventoryService.validateInventory(Mockito.any())).thenReturn(Mono.just(inventoryValidation));

        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> CartonItem.createNewCartonItem(packItemCommand, cartonMock , inventoryService, cartonItemRepository, recoveredPlasmaShippingRepository , recoveredPlasmaShipmentCriteriaRepository));
        assertEquals("Product Criteria not found", exception.getMessage());



    }

    @Test
    public void shouldCreateCartonItemFromRepository() {
        var cartonItem = CartonItem.fromRepository(1L, 1L, "UNIT_NUMBER", "PRODUCT_CODE", "DESCRIPTION",
            "PRODUCT_TYPE", 160, 10, "EMPLOYEE_ID", "AP", "PACKED",
            LocalDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now() , "verify-employee", ZonedDateTime.now());

        Assertions.assertNotNull(cartonItem);
        Assertions.assertEquals(1L, cartonItem.getId());
        Assertions.assertEquals(1L, cartonItem.getCartonId());
        Assertions.assertEquals("UNIT_NUMBER", cartonItem.getUnitNumber());
        Assertions.assertEquals("PRODUCT_CODE", cartonItem.getProductCode());
        Assertions.assertEquals("DESCRIPTION", cartonItem.getProductDescription());
        Assertions.assertEquals("PRODUCT_TYPE", cartonItem.getProductType());
        Assertions.assertEquals(160.0, cartonItem.getVolume(), 0.001);
        Assertions.assertEquals(10.0, cartonItem.getWeight(), 0.001);
        Assertions.assertEquals("PACKED", cartonItem.getStatus());
        Assertions.assertEquals("EMPLOYEE_ID", cartonItem.getPackedByEmployeeId());
        Assertions.assertEquals("AP", cartonItem.getAboRh());
        Assertions.assertNotNull(cartonItem.getExpirationDate());
        Assertions.assertNotNull(cartonItem.getCollectionDate());
        Assertions.assertNotNull(cartonItem.getCreateDate());
        Assertions.assertNotNull(cartonItem.getModificationDate());
        Assertions.assertEquals("verify-employee", cartonItem.getVerifiedByEmployeeId());
        Assertions.assertNotNull(cartonItem.getVerifyDate());
    }

    @Test
    public void shouldNotCreateCartonIfProductTypeNotFound() {
        var carton = Mockito.mock(Carton.class);
        var shipment = Mockito.mock(RecoveredPlasmaShipment.class);

        Mockito.when(cartonItemRepository.countByProduct(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));
        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipment));
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductTypeByProductCode(Mockito.anyString())).thenReturn(Mono.empty());

        var exception = assertThrows(ProductValidationException.class,
            () -> CartonItem.createNewCartonItem(packItemCommand, carton, inventoryService, cartonItemRepository, recoveredPlasmaShippingRepository , recoveredPlasmaShipmentCriteriaRepository));
        assertEquals("Product Type does not match", exception.getMessage());
        assertEquals("WARN", exception.getErrorType());
    }

    @Test
    public void shouldNotCreateCartonIfProductTypeThrowsSystemError() {
        var carton = Mockito.mock(Carton.class);
        var shipment = Mockito.mock(RecoveredPlasmaShipment.class);

        Mockito.when(cartonItemRepository.countByProduct(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));
        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipment));
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductTypeByProductCode(Mockito.anyString())).thenReturn(Mono.error(new Throwable("System error")));

        var exception = assertThrows(ProductValidationException.class,
            () -> CartonItem.createNewCartonItem(packItemCommand, carton, inventoryService, cartonItemRepository, recoveredPlasmaShippingRepository , recoveredPlasmaShipmentCriteriaRepository));
        assertEquals("System error", exception.getMessage());
        assertEquals("SYSTEM", exception.getErrorType());
    }

    @Test
    public void shouldResetVerification() {
        var cartonItem = CartonItem.fromRepository(1L, 1L, "UNIT_NUMBER", "PRODUCT_CODE", "DESCRIPTION",
            "PRODUCT_TYPE", 160, 10, "EMPLOYEE_ID", "AP", "VERIFIED",
            LocalDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), "verify-employee", ZonedDateTime.now());

        cartonItem.resetVerification();

        assertEquals("PACKED", cartonItem.getStatus());
        assertNull(cartonItem.getVerifiedByEmployeeId());
        assertNull(cartonItem.getVerifyDate());
    }

    @Test
    public void shouldNotAllowItemsWithDifferentProductCode(){

        var cartonMock = Mockito.mock(Carton.class);

        var cartonItem = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem.getProductCode()).thenReturn("PRODUCT_CODE_1");

        Mockito.when(cartonMock.getProducts()).thenReturn(List.of(cartonItem));

        var exception = assertThrows(ProductValidationException.class,
            () -> CartonItem.createNewCartonItem(packItemCommand, cartonMock , inventoryService, cartonItemRepository, recoveredPlasmaShippingRepository , recoveredPlasmaShipmentCriteriaRepository));
        assertEquals("The product code does not match the products in the carton", exception.getMessage());
        assertEquals("WARN", exception.getErrorType());

    }

    @Test
    public void shouldAllowItemsWithSameProductCode(){

        var cartonMock = Mockito.mock(Carton.class);
        Mockito.when(cartonMock.getId()).thenReturn(1L);

        var cartonItemSameProductCode = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItemSameProductCode.getProductCode()).thenReturn("PRODUCT_CODE");

        Mockito.when(cartonMock.getProducts()).thenReturn(List.of(cartonItemSameProductCode));

        var shipment = Mockito.mock(RecoveredPlasmaShipment.class);

        var mockCustomer = Mockito.mock(ShipmentCustomer.class);
        Mockito.when(mockCustomer.getCustomerCode()).thenReturn("CUSTOMER_CODE");
        Mockito.when(shipment.getShipmentCustomer()).thenReturn(mockCustomer);

        Mockito.when(shipment.getProductType()).thenReturn("PRODUCT_TYPE");
        var productType = Mockito.mock(ProductType.class);
        Mockito.when(productType.getProductType()).thenReturn("PRODUCT_TYPE");

        var inventoryValidation = Mockito.mock(InventoryValidation.class);
        var inventory = Mockito.mock(Inventory.class);
        Mockito.when(inventory.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(inventory.getProductCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(inventory.getVolumeByType("volume")).thenReturn(Optional.of(new InventoryVolume("volume",160,"ML")));
        Mockito.when(inventory.getWeight()).thenReturn(10);
        Mockito.when(inventory.getProductDescription()).thenReturn("DESCRIPTION");
        Mockito.when(inventory.getAboRh()).thenReturn("AP");
        Mockito.when(inventory.getExpirationDate()).thenReturn(LocalDateTime.now());
        Mockito.when(inventory.getCollectionDate()).thenReturn(ZonedDateTime.now());

        Mockito.when(inventoryValidation.getInventory()).thenReturn(inventory);

        var productCriteria = Mockito.mock(RecoveredPlasmaShipmentCriteria.class);

        var criteriaItemVolume = Mockito.mock(RecoveredPlasmaShipmentCriteriaItem.class);
        Mockito.when(criteriaItemVolume.getValue()).thenReturn("150");

        var criteriaItemMaxUnits = Mockito.mock(RecoveredPlasmaShipmentCriteriaItem.class);
        Mockito.when(criteriaItemMaxUnits.getValue()).thenReturn("10");

        Mockito.when(productCriteria.findCriteriaItemByType("MINIMUM_VOLUME")).thenReturn(Optional.of(criteriaItemVolume));
        Mockito.when(productCriteria.findCriteriaItemByType("MAXIMUM_UNITS_BY_CARTON")).thenReturn(Optional.of(criteriaItemMaxUnits));

        Mockito.when(cartonItemRepository.countByProduct(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));
        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipment));
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductTypeByProductCode(Mockito.anyString())).thenReturn(Mono.just(productType));
        Mockito.when(inventoryService.validateInventory(Mockito.any())).thenReturn(Mono.just(inventoryValidation));
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(productCriteria));



        var response = CartonItem.createNewCartonItem(packItemCommand, cartonMock , inventoryService, cartonItemRepository, recoveredPlasmaShippingRepository , recoveredPlasmaShipmentCriteriaRepository);

        Assertions.assertNotNull(response);
        Assertions.assertNull(response.getId());
        Assertions.assertEquals(1L, response.getCartonId());
        Assertions.assertEquals("UNIT_NUMBER", response.getUnitNumber());
        Assertions.assertEquals("PRODUCT_CODE", response.getProductCode());
        Assertions.assertEquals("DESCRIPTION", response.getProductDescription());
        Assertions.assertEquals("PRODUCT_TYPE", response.getProductType());
        Assertions.assertEquals(160, response.getVolume(), 0.001);
        Assertions.assertEquals(10, response.getWeight(), 0.001);
        Assertions.assertEquals("PACKED", response.getStatus());
        Assertions.assertEquals("EMPLOYEE_ID", response.getPackedByEmployeeId());
        Assertions.assertEquals("AP", response.getAboRh());
        Assertions.assertNotNull(response.getExpirationDate());
        Assertions.assertNotNull(response.getCollectionDate());
        Assertions.assertNotNull(response.getCreateDate());
        Assertions.assertNull(response.getModificationDate());

    }

}
