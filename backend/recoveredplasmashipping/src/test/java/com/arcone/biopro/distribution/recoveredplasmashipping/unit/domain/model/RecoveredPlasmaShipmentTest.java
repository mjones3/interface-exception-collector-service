package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CustomerOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CreateShipmentCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.FindShipmentCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.LocationProperty;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentCriteria;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CustomerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class RecoveredPlasmaShipmentTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;


    @Test
    public void shouldNotCreateNewShipmentWhenLocationIsInvalid() {

        //given
        var createCommand = new CreateShipmentCommand("customerCoode", "locationCode", "productType"
            , "createEmployeeId", "", LocalDate.now().plusDays(1), BigDecimal.TEN);

        Mockito.when(locationRepository.findOneByCode(Mockito.any())).thenReturn(Mono.empty());

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RecoveredPlasmaShipment.createNewShipment(createCommand, customerService, recoveredPlasmaShippingRepository, locationRepository, recoveredPlasmaShipmentCriteriaRepository));

        //then

        assertEquals("Location is required", exception.getMessage());
    }

    @Test
    public void shouldNotCreateNewShipmentWhenProductTypeIsInvalid() {

        //given
        var createCommand = new CreateShipmentCommand("customerCoode", "locationCode", "productType"
            , "createEmployeeId", "", LocalDate.now().plusDays(1), BigDecimal.TEN);

        Mockito.when(locationRepository.findOneByCode(Mockito.any())).thenReturn(Mono.just(Mockito.mock(Location.class)));
        Mockito.when(customerService.findByCode(Mockito.any())).thenReturn(Mono.just(CustomerOutput.builder()
            .code("123")
            .name("name")
            .build()));
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(Mockito.any(), Mockito.any())).thenReturn(Mono.empty());


        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RecoveredPlasmaShipment.createNewShipment(createCommand, customerService, recoveredPlasmaShippingRepository, locationRepository, recoveredPlasmaShipmentCriteriaRepository));

        //then

        assertEquals("Product type is required", exception.getMessage());
    }


    @Test
    public void shouldNotCreateNewShipmentWhenMissingConfigurationToGenerateShipmentNumber() {

        //given
        var createCommand = new CreateShipmentCommand("customerCoode", "locationCode", "productType"
            , "createEmployeeId", "", LocalDate.now().plusDays(1), BigDecimal.TEN);

        Mockito.when(recoveredPlasmaShippingRepository.getNextShipmentId()).thenReturn(Mono.just(1L));
        Mockito.when(locationRepository.findOneByCode(Mockito.any())).thenReturn(Mono.just(Mockito.mock(Location.class)));
        Mockito.when(customerService.findByCode(Mockito.any())).thenReturn(Mono.just(CustomerOutput.builder()
            .code("123")
            .name("name")
            .build()));
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(Mockito.any(), Mockito.any())).thenReturn(Mono.just(Mockito.mock(RecoveredPlasmaShipmentCriteria.class)));

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RecoveredPlasmaShipment.createNewShipment(createCommand, customerService, recoveredPlasmaShippingRepository, locationRepository, recoveredPlasmaShipmentCriteriaRepository));

        //then

        assertEquals("Location configuration is missing the setup for  RPS_USE_PARTNER_PREFIX property", exception.getMessage());
    }

    @Test
    public void shouldNotCreateNewShipmentWhenCustomerIsInvalid() {

        //given
        var createCommand = new CreateShipmentCommand("customerCoode", "locationCode", "productType"
            , "createEmployeeId", "", LocalDate.now().plusDays(1), BigDecimal.TEN);


        var locationMock = Mockito.mock(Location.class);

        Mockito.when(locationRepository.findOneByCode(Mockito.any())).thenReturn(Mono.just(locationMock));
        Mockito.when(customerService.findByCode(Mockito.any())).thenReturn(Mono.empty());


        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RecoveredPlasmaShipment.createNewShipment(createCommand, customerService, recoveredPlasmaShippingRepository, locationRepository, recoveredPlasmaShipmentCriteriaRepository));

        //then

        assertEquals("Customer not found for code: customerCoode", exception.getMessage());
    }

    @Test
    public void shouldNotCreateNewShipmentWhenMissingShipmentPrefix() {

        //given
        var createCommand = new CreateShipmentCommand("customerCoode", "locationCode", "productType"
            , "createEmployeeId", "", LocalDate.now().plusDays(1), BigDecimal.TEN);


        var locationMock = Mockito.mock(Location.class);
        Mockito.when(locationMock.findProperty(Mockito.eq("RPS_USE_PARTNER_PREFIX"))).thenReturn(Optional.of(new LocationProperty(1L, "RPS_USE_PARTNER_PREFIX", "Y")));


        Mockito.when(recoveredPlasmaShippingRepository.getNextShipmentId()).thenReturn(Mono.just(1L));
        Mockito.when(locationRepository.findOneByCode(Mockito.any())).thenReturn(Mono.just(locationMock));
        Mockito.when(customerService.findByCode(Mockito.any())).thenReturn(Mono.just(CustomerOutput.builder()
            .code("123")
            .name("name")
            .build()));
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(Mockito.any(), Mockito.any())).thenReturn(Mono.just(Mockito.mock(RecoveredPlasmaShipmentCriteria.class)));

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RecoveredPlasmaShipment.createNewShipment(createCommand, customerService, recoveredPlasmaShippingRepository, locationRepository, recoveredPlasmaShipmentCriteriaRepository));

        //then

        assertEquals("Location configuration is missing the setup for  RPS_PARTNER_PREFIX property", exception.getMessage());
    }

    @Test
    public void shouldNotCreateNewShipmentWhenMissingShipmentLocationCode() {

        //given
        var createCommand = new CreateShipmentCommand("customerCoode", "locationCode", "productType"
            , "createEmployeeId", "", LocalDate.now().plusDays(1), BigDecimal.TEN);


        var locationMock = Mockito.mock(Location.class);
        Mockito.when(locationMock.findProperty(Mockito.eq("RPS_USE_PARTNER_PREFIX"))).thenReturn(Optional.of(new LocationProperty(1L, "RPS_USE_PARTNER_PREFIX", "Y")));
        Mockito.when(locationMock.findProperty(Mockito.eq("RPS_PARTNER_PREFIX"))).thenReturn(Optional.of(new LocationProperty(1L, "RPS_PARTNER_PREFIX", "ABC")));


        Mockito.when(recoveredPlasmaShippingRepository.getNextShipmentId()).thenReturn(Mono.just(1L));
        Mockito.when(locationRepository.findOneByCode(Mockito.any())).thenReturn(Mono.just(locationMock));
        Mockito.when(customerService.findByCode(Mockito.any())).thenReturn(Mono.just(CustomerOutput.builder()
            .code("123")
            .name("name")
            .build()));
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(Mockito.any(), Mockito.any())).thenReturn(Mono.just(Mockito.mock(RecoveredPlasmaShipmentCriteria.class)));

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RecoveredPlasmaShipment.createNewShipment(createCommand, customerService, recoveredPlasmaShippingRepository, locationRepository, recoveredPlasmaShipmentCriteriaRepository));

        //then

        assertEquals("Location configuration is missing the setup for  RPS_LOCATION_SHIPMENT_CODE property", exception.getMessage());
    }


    @Test
    public void shouldCreateNewShipment() {

        //given
        var createCommand = new CreateShipmentCommand("customerCoode", "locationCode", "productType"
            , "createEmployeeId", "123", LocalDate.now().plusDays(1), BigDecimal.TEN);


        var locationMock = Mockito.mock(Location.class);
        Mockito.when(locationMock.getCode()).thenReturn("CODE");
        Mockito.when(locationMock.findProperty(Mockito.eq("RPS_USE_PARTNER_PREFIX"))).thenReturn(Optional.of(new LocationProperty(1L, "RPS_USE_PARTNER_PREFIX", "Y")));
        Mockito.when(locationMock.findProperty(Mockito.eq("RPS_PARTNER_PREFIX"))).thenReturn(Optional.of(new LocationProperty(1L, "RPS_PARTNER_PREFIX", "BPM")));
        Mockito.when(locationMock.findProperty(Mockito.eq("RPS_LOCATION_SHIPMENT_CODE"))).thenReturn(Optional.of(new LocationProperty(1L, "RPS_LOCATION_SHIPMENT_CODE", "2765")));



        Mockito.when(recoveredPlasmaShippingRepository.getNextShipmentId()).thenReturn(Mono.just(1L));
        Mockito.when(locationRepository.findOneByCode(Mockito.any())).thenReturn(Mono.just(locationMock));

        var productTypeMock = Mockito.mock(RecoveredPlasmaShipmentCriteria.class);
        Mockito.when(productTypeMock.getProductType()).thenReturn("productType");
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(Mockito.any(), Mockito.any())).thenReturn(Mono.just(productTypeMock));
        Mockito.when(customerService.findByCode(Mockito.any())).thenReturn(Mono.just(CustomerOutput.builder()
            .code("123")
            .name("name")
            .build()));


        //when
        var result = RecoveredPlasmaShipment.createNewShipment(createCommand, customerService, recoveredPlasmaShippingRepository, locationRepository, recoveredPlasmaShipmentCriteriaRepository);

        //then

        Assertions.assertNotNull(result);
        Assertions.assertEquals("BPM27651", result.getShipmentNumber());
        Assertions.assertEquals("OPEN", result.getStatus());
        Assertions.assertEquals(LocalDate.now().plusDays(1), result.getShipmentDate());
        Assertions.assertEquals(BigDecimal.TEN, result.getCartonTareWeight());
        Assertions.assertEquals("123", result.getTransportationReferenceNumber());
        Assertions.assertEquals("createEmployeeId", result.getCreateEmployeeId());
        Assertions.assertTrue(result.getCreateDate().isBefore(ZonedDateTime.now()));

    }

    @Test
    public void shouldCreateNewShipmentWithoutPrefixNumber() {

        //given
        var createCommand = new CreateShipmentCommand("customerCoode", "locationCode", "productType"
            , "createEmployeeId", "123", LocalDate.now().plusDays(1), BigDecimal.TEN);


        var locationMock = Mockito.mock(Location.class);
        Mockito.when(locationMock.getCode()).thenReturn("CODE");
        Mockito.when(locationMock.findProperty(Mockito.eq("RPS_USE_PARTNER_PREFIX"))).thenReturn(Optional.of(new LocationProperty(1L, "RPS_USE_PARTNER_PREFIX", "N")));
        Mockito.when(locationMock.findProperty(Mockito.eq("RPS_LOCATION_SHIPMENT_CODE"))).thenReturn(Optional.of(new LocationProperty(1L, "RPS_LOCATION_SHIPMENT_CODE", "2765")));

        Mockito.when(recoveredPlasmaShippingRepository.getNextShipmentId()).thenReturn(Mono.just(1L));
        Mockito.when(locationRepository.findOneByCode(Mockito.any())).thenReturn(Mono.just(locationMock));

        var productTypeMock = Mockito.mock(RecoveredPlasmaShipmentCriteria.class);
        Mockito.when(productTypeMock.getProductType()).thenReturn("productType");
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(Mockito.any(), Mockito.any())).thenReturn(Mono.just(productTypeMock));
        Mockito.when(customerService.findByCode(Mockito.any())).thenReturn(Mono.just(CustomerOutput.builder()
            .code("123")
            .name("name")
            .build()));


        //when
        var result = RecoveredPlasmaShipment.createNewShipment(createCommand, customerService, recoveredPlasmaShippingRepository, locationRepository, recoveredPlasmaShipmentCriteriaRepository);

        //then

        Assertions.assertNotNull(result);
        Assertions.assertEquals("27651", result.getShipmentNumber());

    }


    /**
     * Test case for the fromRepository method to ensure it correctly creates a RecoveredPlasmaShipment
     * object with the provided parameters.
     */
    @Test
    public void test_fromRepository_createsShipmentWithCorrectValues() {
        // Arrange
        Long id = 1L;
        String locationCode = "LOC001";
        String productType = "PLASMA";
        String shipmentNumber = "SHP001";
        String status = "OPEN";
        String createEmployeeId = "EMP001";
        String closeEmployeeId = "EMP002";
        ZonedDateTime closeDate = ZonedDateTime.now();
        String transportationReferenceNumber = "TRN001";
        LocalDate shipmentDate = LocalDate.now();
        BigDecimal cartonTareWeight = BigDecimal.valueOf(10.5);
        String unsuitableUnitReportDocumentStatus = "COMPLETED";
        String customerCode = "CUST001";
        String customerName = "Test Customer";
        String customerState = "Test State";
        String customerPostalCode = "12345";
        String customerCountry = "Test Country";
        String customerCountryCode = "TC";
        String customerCity = "Test City";
        String customerDistrict = "Test District";
        String customerAddressLine1 = "123 Test St";
        String customerAddressLine2 = "Apt 4";
        String customerAddressContactName = "John Doe";
        String customerAddressPhoneNumber = "1234567890";
        String customerAddressDepartmentName = "Test Department";
        ZonedDateTime createDate = ZonedDateTime.now();
        ZonedDateTime modificationDate = ZonedDateTime.now();

        // Act
        RecoveredPlasmaShipment shipment = RecoveredPlasmaShipment.fromRepository(
            id, locationCode, productType, shipmentNumber, status, createEmployeeId,
            closeEmployeeId, closeDate, transportationReferenceNumber,shipmentDate, cartonTareWeight, unsuitableUnitReportDocumentStatus,
            customerCode, customerName, customerState, customerPostalCode, customerCountry,
            customerCountryCode, customerCity, customerDistrict, customerAddressLine1,
            customerAddressLine2, customerAddressContactName, customerAddressPhoneNumber,
            customerAddressDepartmentName, createDate, modificationDate
        );

        // Assert
        assertNotNull(shipment);
        assertEquals(id, shipment.getId());
        assertEquals(locationCode, shipment.getLocationCode());
        assertEquals(productType, shipment.getProductType());
        assertEquals(shipmentNumber, shipment.getShipmentNumber());
        assertEquals(status, shipment.getStatus());
        assertEquals(createEmployeeId, shipment.getCreateEmployeeId());
        assertEquals(closeEmployeeId, shipment.getCloseEmployeeId());
        assertEquals(closeDate, shipment.getCloseDate());
        assertEquals(transportationReferenceNumber, shipment.getTransportationReferenceNumber());
        assertEquals(shipmentDate, shipment.getShipmentDate());
        assertEquals(cartonTareWeight, shipment.getCartonTareWeight());
        assertEquals(unsuitableUnitReportDocumentStatus, shipment.getUnsuitableUnitReportDocumentStatus());
        assertEquals(createDate, shipment.getCreateDate());
        assertEquals(modificationDate, shipment.getModificationDate());

        assertNotNull(shipment.getShipmentCustomer());
        assertEquals(customerCode, shipment.getShipmentCustomer().getCustomerCode());
        assertEquals(customerName, shipment.getShipmentCustomer().getCustomerName());
        assertEquals(customerState, shipment.getShipmentCustomer().getCustomerState());
        assertEquals(customerPostalCode, shipment.getShipmentCustomer().getCustomerPostalCode());
        assertEquals(customerCountry, shipment.getShipmentCustomer().getCustomerCountry());
        assertEquals(customerCountryCode, shipment.getShipmentCustomer().getCustomerCountryCode());
        assertEquals(customerCity, shipment.getShipmentCustomer().getCustomerCity());
        assertEquals(customerDistrict, shipment.getShipmentCustomer().getCustomerDistrict());
        assertEquals(customerAddressLine1, shipment.getShipmentCustomer().getCustomerAddressLine1());
        assertEquals(customerAddressLine2, shipment.getShipmentCustomer().getCustomerAddressLine2());
        assertEquals(customerAddressContactName, shipment.getShipmentCustomer().getCustomerAddressContactName());
        assertEquals(customerAddressPhoneNumber, shipment.getShipmentCustomer().getCustomerAddressPhoneNumber());
        assertEquals(customerAddressDepartmentName, shipment.getShipmentCustomer().getCustomerAddressDepartmentName());
    }

    /**
     * Tests that the fromRepository method throws an IllegalArgumentException when the shipment number is null.
     * This test verifies that the method properly validates the shipment number as required by the checkValid method.
     */
    @Test
    public void test_fromRepository_nullShipmentNumber() {
        assertThrows(IllegalArgumentException.class, () -> {
            RecoveredPlasmaShipment.fromRepository(
                1L, "locationCode", "productType", null, "status", "createEmployeeId",
                "closeEmployeeId", ZonedDateTime.now(), "transportationReferenceNumber",
                LocalDate.now(), BigDecimal.ONE, "unsuitableUnitReportDocumentStatus",
                "customerCode", "customerName", "customerState", "customerPostalCode", "customerCountry",
                "customerCountryCode", "customerCity", "customerDistrict", "customerAddressLine1",
                "customerAddressLine2", "customerAddressContactName", "customerAddressPhoneNumber",
                "customerAddressDepartmentName", ZonedDateTime.now(), ZonedDateTime.now()
            );
        });
    }


    @Test
    void checkValid_WithValidShipment_ShouldNotThrowException() {
        assertDoesNotThrow(() -> RecoveredPlasmaShipment.fromRepository(
            1L, "locationCode", "productType", "123", "status", "createEmployeeId",
            "closeEmployeeId", ZonedDateTime.now(), "transportationReferenceNumber",
            LocalDate.now(), BigDecimal.ONE, "unsuitableUnitReportDocumentStatus",
            "customerCode", "customerName", "customerState", "customerPostalCode", "customerCountry",
            "customerCountryCode", "customerCity", "customerDistrict", "customerAddressLine1",
            "customerAddressLine2", "customerAddressContactName", "customerAddressPhoneNumber",
            "customerAddressDepartmentName", ZonedDateTime.now(), ZonedDateTime.now()
        ));
    }

    @Test
    void checkValid_WithNullShipmentNumber_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> RecoveredPlasmaShipment.fromRepository(
                1L, "locationCode", "productType", null, "status", "createEmployeeId",
                "closeEmployeeId", ZonedDateTime.now(), "transportationReferenceNumber",
                LocalDate.now(), BigDecimal.ONE, "unsuitableUnitReportDocumentStatus",
                "customerCode", "customerName", "customerState", "customerPostalCode", "customerCountry",
                "customerCountryCode", "customerCity", "customerDistrict", "customerAddressLine1",
                "customerAddressLine2", "customerAddressContactName", "customerAddressPhoneNumber",
                "customerAddressDepartmentName", ZonedDateTime.now(), ZonedDateTime.now()
            ));
        assertEquals("Shipment Number is required", exception.getMessage());
    }

    @Test
    void checkValid_WithBlankShipmentNumber_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> RecoveredPlasmaShipment.fromRepository(
                1L, "locationCode", "productType", " ", "status", "createEmployeeId",
                "closeEmployeeId", ZonedDateTime.now(), "transportationReferenceNumber",
                LocalDate.now(), BigDecimal.ONE, "unsuitableUnitReportDocumentStatus",
                "customerCode", "customerName", "customerState", "customerPostalCode", "customerCountry",
                "customerCountryCode", "customerCity", "customerDistrict", "customerAddressLine1",
                "customerAddressLine2", "customerAddressContactName", "customerAddressPhoneNumber",
                "customerAddressDepartmentName", ZonedDateTime.now(), ZonedDateTime.now()
            ));
        assertEquals("Shipment Number is required", exception.getMessage());
    }

    @Test
    void checkValid_WithNullShipmentCustomer_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> RecoveredPlasmaShipment.fromRepository(
                1L, "locationCode", "productType", "123", "status", "createEmployeeId",
                "closeEmployeeId", ZonedDateTime.now(), "transportationReferenceNumber",
                LocalDate.now(),  BigDecimal.ONE, "unsuitableUnitReportDocumentStatus",
                null, "customerName", "customerState", "customerPostalCode", "customerCountry",
                "customerCountryCode", "customerCity", "customerDistrict", "customerAddressLine1",
                "customerAddressLine2", "customerAddressContactName", "customerAddressPhoneNumber",
                "customerAddressDepartmentName", ZonedDateTime.now(), ZonedDateTime.now()
            ));
        assertEquals("Customer code cannot be null or blank", exception.getMessage());
    }

    @Test
    void checkValid_WithNullLocationCode_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> RecoveredPlasmaShipment.fromRepository(
                1L, null, "productType", "123", "status", "createEmployeeId",
                "closeEmployeeId", ZonedDateTime.now(), "transportationReferenceNumber",
                LocalDate.now(), BigDecimal.ONE, "unsuitableUnitReportDocumentStatus",
                "customerCode", "customerName", "customerState", "customerPostalCode", "customerCountry",
                "customerCountryCode", "customerCity", "customerDistrict", "customerAddressLine1",
                "customerAddressLine2", "customerAddressContactName", "customerAddressPhoneNumber",
                "customerAddressDepartmentName", ZonedDateTime.now(), ZonedDateTime.now()
            ));
        assertEquals("Location code is required", exception.getMessage());
    }

    @Test
    void checkValid_WithBlankLocationCode_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> RecoveredPlasmaShipment.fromRepository(
                1L, " ", "productType", "123", "status", "createEmployeeId",
                "closeEmployeeId", ZonedDateTime.now(), "transportationReferenceNumber",
                LocalDate.now(), BigDecimal.ONE, "unsuitableUnitReportDocumentStatus",
                "customerCode", "customerName", "customerState", "customerPostalCode", "customerCountry",
                "customerCountryCode", "customerCity", "customerDistrict", "customerAddressLine1",
                "customerAddressLine2", "customerAddressContactName", "customerAddressPhoneNumber",
                "customerAddressDepartmentName", ZonedDateTime.now(), ZonedDateTime.now()
            ));
        assertEquals("Location code is required", exception.getMessage());
    }

    @Test
    void checkValid_WithNullProductType_ShouldThrowException() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> RecoveredPlasmaShipment.fromRepository(
                1L, "locationCode", null, "123", "status", "createEmployeeId",
                "closeEmployeeId", ZonedDateTime.now(), "transportationReferenceNumber",
                LocalDate.now(), BigDecimal.ONE, "unsuitableUnitReportDocumentStatus",
                "customerCode", "customerName", "customerState", "customerPostalCode", "customerCountry",
                "customerCountryCode", "customerCity", "customerDistrict", "customerAddressLine1",
                "customerAddressLine2", "customerAddressContactName", "customerAddressPhoneNumber",
                "customerAddressDepartmentName", ZonedDateTime.now(), ZonedDateTime.now()
            ));
        assertEquals("Product type is required", exception.getMessage());
    }

    @Test
    void checkValid_WithNullCreateEmployeeId_ShouldThrowException() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> RecoveredPlasmaShipment.fromRepository(
                1L, "locationCode", "productType", "123", "status", null,
                "closeEmployeeId", ZonedDateTime.now(), "transportationReferenceNumber",
                LocalDate.now(), BigDecimal.ONE, "unsuitableUnitReportDocumentStatus",
                "customerCode", "customerName", "customerState", "customerPostalCode", "customerCountry",
                "customerCountryCode", "customerCity", "customerDistrict", "customerAddressLine1",
                "customerAddressLine2", "customerAddressContactName", "customerAddressPhoneNumber",
                "customerAddressDepartmentName", ZonedDateTime.now(), ZonedDateTime.now()
            ));
        assertEquals("Create employee ID is required", exception.getMessage());
    }

    @Test
    void checkValid_WithNullShipmentDate_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> RecoveredPlasmaShipment.fromRepository(
                1L, "locationCode", "productType", "123", "status", "createEmployeeId",
                "closeEmployeeId", ZonedDateTime.now(), "transportationReferenceNumber",
                null, BigDecimal.ONE, "unsuitableUnitReportDocumentStatus",
                "customerCode", "customerName", "customerState", "customerPostalCode", "customerCountry",
                "customerCountryCode", "customerCity", "customerDistrict", "customerAddressLine1",
                "customerAddressLine2", "customerAddressContactName", "customerAddressPhoneNumber",
                "customerAddressDepartmentName", ZonedDateTime.now(), ZonedDateTime.now()
            ));
        assertEquals("Shipment date is required", exception.getMessage());
    }

    @Test
    void checkValid_WithNullStatus_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () ->RecoveredPlasmaShipment.fromRepository(
                1L, "locationCode", "productType", "123", null, "createEmployeeId",
                "closeEmployeeId", ZonedDateTime.now(), "transportationReferenceNumber",
                LocalDate.now(), BigDecimal.ONE, "unsuitableUnitReportDocumentStatus",
                "customerCode", "customerName", "customerState", "customerPostalCode", "customerCountry",
                "customerCountryCode", "customerCity", "customerDistrict", "customerAddressLine1",
                "customerAddressLine2", "customerAddressContactName", "customerAddressPhoneNumber",
                "customerAddressDepartmentName", ZonedDateTime.now(), ZonedDateTime.now()
            ));
        assertEquals("Status is required", exception.getMessage());
    }

    @Test
    void checkValid_WithNullCartonTareWeight_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> RecoveredPlasmaShipment.fromRepository(
                1L, "locationCode", "productType", "123", "status", "createEmployeeId",
                "closeEmployeeId", ZonedDateTime.now(), "transportationReferenceNumber",
                LocalDate.now(), null, "unsuitableUnitReportDocumentStatus",
                "customerCode", "customerName", "customerState", "customerPostalCode", "customerCountry",
                "customerCountryCode", "customerCity", "customerDistrict", "customerAddressLine1",
                "customerAddressLine2", "customerAddressContactName", "customerAddressPhoneNumber",
                "customerAddressDepartmentName", ZonedDateTime.now(), ZonedDateTime.now()
            ));
        assertEquals("Carton tare weight is required", exception.getMessage());
    }


    @Test
    public void shouldNotFindByIdWhenShipmentDoesNotExists() {

        //given
        var findCommand = new FindShipmentCommand(1L, "locationCode", "employee-id");

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.any())).thenReturn(Mono.empty());

        //when
        DomainNotFoundForKeyException exception = assertThrows(DomainNotFoundForKeyException.class, () -> RecoveredPlasmaShipment.fromFindByCommand(findCommand , recoveredPlasmaShippingRepository));

        //then

        assertEquals("Domain not found for key 1", exception.getMessage());
    }

    @Test
    public void shouldFindByIdWhenShipmentExists() {

        //given
        var findCommand = new FindShipmentCommand(1L, "LOC001", "employee-id");

        // Arrange
        Long id = 1L;
        String locationCode = "LOC001";
        String productType = "PLASMA";
        String shipmentNumber = "SHP001";
        String status = "OPEN";
        String createEmployeeId = "EMP001";
        String closeEmployeeId = "EMP002";
        ZonedDateTime closeDate = ZonedDateTime.now();
        String transportationReferenceNumber = "TRN001";
        LocalDate shipmentDate = LocalDate.now();
        BigDecimal cartonTareWeight = BigDecimal.valueOf(10.5);
        String unsuitableUnitReportDocumentStatus = "COMPLETED";
        String customerCode = "CUST001";
        String customerName = "Test Customer";
        String customerState = "Test State";
        String customerPostalCode = "12345";
        String customerCountry = "Test Country";
        String customerCountryCode = "TC";
        String customerCity = "Test City";
        String customerDistrict = "Test District";
        String customerAddressLine1 = "123 Test St";
        String customerAddressLine2 = "Apt 4";
        String customerAddressContactName = "John Doe";
        String customerAddressPhoneNumber = "1234567890";
        String customerAddressDepartmentName = "Test Department";
        ZonedDateTime createDate = ZonedDateTime.now();
        ZonedDateTime modificationDate = ZonedDateTime.now();

        // Act
        RecoveredPlasmaShipment shipment = RecoveredPlasmaShipment.fromRepository(
            id, locationCode, productType, shipmentNumber, status, createEmployeeId,
            closeEmployeeId, closeDate, transportationReferenceNumber,shipmentDate, cartonTareWeight, unsuitableUnitReportDocumentStatus,
            customerCode, customerName, customerState, customerPostalCode, customerCountry,
            customerCountryCode, customerCity, customerDistrict, customerAddressLine1,
            customerAddressLine2, customerAddressContactName, customerAddressPhoneNumber,
            customerAddressDepartmentName, createDate, modificationDate
        );

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.any())).thenReturn(Mono.just(shipment));

        //when
        var result = RecoveredPlasmaShipment.fromFindByCommand(findCommand, recoveredPlasmaShippingRepository);

        //then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isCanAddCartons());

    }

    @Test
    public void shouldFindByIdWhenShipmentExists_not_allow_add_cartonsWhenLocationDoesNotMatch() {

        //given
        var findCommand = new FindShipmentCommand(1L, "LOC002", "employee-id");

        // Arrange
        Long id = 1L;
        String locationCode = "LOC001";
        String productType = "PLASMA";
        String shipmentNumber = "SHP001";
        String status = "OPEN";
        String createEmployeeId = "EMP001";
        String closeEmployeeId = "EMP002";
        ZonedDateTime closeDate = ZonedDateTime.now();
        String transportationReferenceNumber = "TRN001";
        LocalDate shipmentDate = LocalDate.now();
        BigDecimal cartonTareWeight = BigDecimal.valueOf(10.5);
        String unsuitableUnitReportDocumentStatus = "COMPLETED";
        String customerCode = "CUST001";
        String customerName = "Test Customer";
        String customerState = "Test State";
        String customerPostalCode = "12345";
        String customerCountry = "Test Country";
        String customerCountryCode = "TC";
        String customerCity = "Test City";
        String customerDistrict = "Test District";
        String customerAddressLine1 = "123 Test St";
        String customerAddressLine2 = "Apt 4";
        String customerAddressContactName = "John Doe";
        String customerAddressPhoneNumber = "1234567890";
        String customerAddressDepartmentName = "Test Department";
        ZonedDateTime createDate = ZonedDateTime.now();
        ZonedDateTime modificationDate = ZonedDateTime.now();

        // Act
        RecoveredPlasmaShipment shipment = RecoveredPlasmaShipment.fromRepository(
            id, locationCode, productType, shipmentNumber, status, createEmployeeId,
            closeEmployeeId, closeDate, transportationReferenceNumber,shipmentDate, cartonTareWeight, unsuitableUnitReportDocumentStatus,
            customerCode, customerName, customerState, customerPostalCode, customerCountry,
            customerCountryCode, customerCity, customerDistrict, customerAddressLine1,
            customerAddressLine2, customerAddressContactName, customerAddressPhoneNumber,
            customerAddressDepartmentName, createDate, modificationDate
        );

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.any())).thenReturn(Mono.just(shipment));

        //when
        var result = RecoveredPlasmaShipment.fromFindByCommand(findCommand, recoveredPlasmaShippingRepository);

        //then
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isCanAddCartons());

    }



}
