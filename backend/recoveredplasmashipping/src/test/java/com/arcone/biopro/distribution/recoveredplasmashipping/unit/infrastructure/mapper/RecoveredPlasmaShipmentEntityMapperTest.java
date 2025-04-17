package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShipmentCustomer;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaShipmentEntityMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonEntity;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaShipmentEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RecoveredPlasmaShipmentEntityMapperTest {


    private final RecoveredPlasmaShipmentEntityMapper mapper = Mappers.getMapper(RecoveredPlasmaShipmentEntityMapper.class);

    @Test
    void entityToModel_ShouldMapAllFields() {
        // Arrange
        RecoveredPlasmaShipmentEntity entity = createTestEntity();
        List<CartonEntity> cartonEntityList = List.of(CartonEntity.
            builder()
                .shipmentId(1L)
                .cartonNumber("NUMBER")
                .id(1L)
                .status("OPEN")
                .createDate(ZonedDateTime.now())
                .cartonSequenceNumber(1)
                .closeDate(ZonedDateTime.now())
                .closeEmployeeId("close-id")
                .createDate(ZonedDateTime.now())
                .createEmployeeId("create-id")
            .build());



        // Act
        RecoveredPlasmaShipment result = mapper.entityToModel(entity,cartonEntityList);

        // Assert
        assertNotNull(result);
        assertEquals(entity.getId(), result.getId());
        assertEquals(entity.getLocationCode(), result.getLocationCode());
        assertEquals(entity.getProductType(), result.getProductType());
        assertEquals(entity.getShipmentNumber(), result.getShipmentNumber());
        assertEquals(entity.getStatus(), result.getStatus());
        assertEquals(entity.getCreateEmployeeId(), result.getCreateEmployeeId());
        assertEquals(entity.getCloseEmployeeId(), result.getCloseEmployeeId());
        assertEquals(entity.getCloseDate(), result.getCloseDate());
        assertEquals(entity.getTransportationReferenceNumber(), result.getTransportationReferenceNumber());
        assertEquals(entity.getShipmentDate(), result.getShipmentDate());
        assertEquals(entity.getCartonTareWeight(), result.getCartonTareWeight());
        assertEquals(entity.getUnsuitableUnitReportDocumentStatus(), result.getUnsuitableUnitReportDocumentStatus());
        assertEquals(entity.getCustomerCode(), result.getShipmentCustomer().getCustomerCode());
        assertEquals(entity.getCustomerName(), result.getShipmentCustomer().getCustomerName());
        assertEquals(entity.getCustomerState(), result.getShipmentCustomer().getCustomerState());
        assertEquals(entity.getCustomerPostalCode(), result.getShipmentCustomer().getCustomerPostalCode());
        assertEquals(entity.getCustomerCountry(), result.getShipmentCustomer().getCustomerCountry());
        assertEquals(entity.getCustomerCity(), result.getShipmentCustomer().getCustomerCity());
        assertEquals(entity.getCustomerDistrict(), result.getShipmentCustomer().getCustomerDistrict());
        assertEquals(entity.getCustomerAddressLine1(), result.getShipmentCustomer().getCustomerAddressLine1());
        assertEquals(entity.getCustomerAddressLine2(), result.getShipmentCustomer().getCustomerAddressLine2());
        assertEquals(entity.getCustomerAddressContactName(), result.getShipmentCustomer().getCustomerAddressContactName());
        assertEquals(entity.getCustomerAddressPhoneNumber(), result.getShipmentCustomer().getCustomerAddressPhoneNumber());
        assertEquals(entity.getCustomerAddressDepartmentName(), result.getShipmentCustomer().getCustomerAddressDepartmentName());
        assertEquals(entity.getCreateDate(), result.getCreateDate());
        assertEquals(entity.getModificationDate(), result.getModificationDate());

        assertEquals(1,result.getCartonList().size());
        assertEquals(1,result.getTotalCartons());
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        // Arrange
        RecoveredPlasmaShipment shipment = createTestModel();

        // Act
        RecoveredPlasmaShipmentEntity result = mapper.toEntity(shipment);

        // Assert
        assertNotNull(result);
        assertEquals(shipment.getId(), result.getId());
        assertEquals(shipment.getLocationCode(), result.getLocationCode());
        assertEquals(shipment.getProductType(), result.getProductType());
        assertEquals(shipment.getShipmentNumber(), result.getShipmentNumber());
        assertEquals(shipment.getStatus(), result.getStatus());
        assertEquals(shipment.getCreateEmployeeId(), result.getCreateEmployeeId());
        assertEquals(shipment.getCloseEmployeeId(), result.getCloseEmployeeId());
        assertEquals(shipment.getCloseDate(), result.getCloseDate());
        assertEquals(shipment.getTransportationReferenceNumber(), result.getTransportationReferenceNumber());
        assertEquals(shipment.getShipmentDate(), result.getShipmentDate());
        assertEquals(shipment.getCartonTareWeight(), result.getCartonTareWeight());
        assertEquals(shipment.getUnsuitableUnitReportDocumentStatus(), result.getUnsuitableUnitReportDocumentStatus());

        // Customer mappings
        assertEquals(shipment.getShipmentCustomer().getCustomerCode(), result.getCustomerCode());
        assertEquals(shipment.getShipmentCustomer().getCustomerName(), result.getCustomerName());
        assertEquals(shipment.getShipmentCustomer().getCustomerState(), result.getCustomerState());
        assertEquals(shipment.getShipmentCustomer().getCustomerPostalCode(), result.getCustomerPostalCode());
        assertEquals(shipment.getShipmentCustomer().getCustomerCountry(), result.getCustomerCountry());
        assertEquals(shipment.getShipmentCustomer().getCustomerCountryCode(), result.getCustomerCountryCode());
        assertEquals(shipment.getShipmentCustomer().getCustomerCity(), result.getCustomerCity());
        assertEquals(shipment.getShipmentCustomer().getCustomerDistrict(), result.getCustomerDistrict());
        assertEquals(shipment.getShipmentCustomer().getCustomerAddressLine1(), result.getCustomerAddressLine1());
        assertEquals(shipment.getShipmentCustomer().getCustomerAddressLine2(), result.getCustomerAddressLine2());
        assertEquals(shipment.getShipmentCustomer().getCustomerAddressContactName(), result.getCustomerAddressContactName());
        assertEquals(shipment.getShipmentCustomer().getCustomerAddressPhoneNumber(), result.getCustomerAddressPhoneNumber());
        //assertEquals(shipment.getShipmentCustomer().getCustomerAddressDepartmentName(), result.getCustomerAddressDepartmentName());
    }

    private RecoveredPlasmaShipmentEntity createTestEntity() {
        RecoveredPlasmaShipmentEntity entity = RecoveredPlasmaShipmentEntity
            .builder()
            .id(1L)
            .locationCode("LOC123")
            .productType("PLASMA")
            .shipmentNumber("SHP001")
            .status("ACTIVE")
            .createEmployeeId("EMP123")
            .closeEmployeeId("EMP456")
            .closeDate(ZonedDateTime.now())
            .transportationReferenceNumber("TRN123")
            .shipmentDate(LocalDate.now())
            .cartonTareWeight(new BigDecimal(10.5))
            .unsuitableUnitReportDocumentStatus("COMPLETED")
            .customerCode("CUST123")
            .customerName("Test Customer")
            .customerState("TX")
            .customerPostalCode("12345")
            .customerCountry("USA")
            .customerCity("Test City")
            .customerDistrict("Test District")
            .customerAddressLine1("123 Test St")
            .customerAddressLine2("Suite 100")
            .customerAddressContactName("John Doe")
            .customerAddressPhoneNumber("123-456-7890")
            .customerAddressDepartmentName("Shipping")
            .createDate(ZonedDateTime.now().now())
            .modificationDate(ZonedDateTime.now().now())
            .build();

        return entity;
    }

    private RecoveredPlasmaShipment createTestModel() {
        RecoveredPlasmaShipment model = Mockito.mock(RecoveredPlasmaShipment.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(model.getId()).thenReturn(1L);
        Mockito.when(model.getLocationCode()).thenReturn("LOC123");
        Mockito.when(model.getProductType()).thenReturn("PLASMA");
        Mockito.when(model.getShipmentNumber()).thenReturn("SHP001");
        Mockito.when(model.getStatus()).thenReturn("ACTIVE");
        Mockito.when(model.getCreateEmployeeId()).thenReturn("EMP123");
        Mockito.when(model.getCloseEmployeeId()).thenReturn("EMP456");
        Mockito.when(model.getCloseDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(model.getTransportationReferenceNumber()).thenReturn("TRN123");
        Mockito.when(model.getShipmentDate()).thenReturn(LocalDate.now());
        Mockito.when(model.getCartonTareWeight()).thenReturn(new BigDecimal(10.5));
        Mockito.when(model.getUnsuitableUnitReportDocumentStatus()).thenReturn("COMPLETED");

        ShipmentCustomer customer = Mockito.mock(ShipmentCustomer.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(customer.getCustomerCode()).thenReturn("CUST123");
        Mockito.when(customer.getCustomerName()).thenReturn("Test Customer");
        Mockito.when(customer.getCustomerState()).thenReturn("TX");
        Mockito.when(customer.getCustomerPostalCode()).thenReturn("12345");
        Mockito.when(customer.getCustomerCountry()).thenReturn("USA");
        Mockito.when(customer.getCustomerCountryCode()).thenReturn("US");
        Mockito.when(customer.getCustomerCity()).thenReturn("Test City");
        Mockito.when(customer.getCustomerDistrict()).thenReturn("Test District");
        Mockito.when(customer.getCustomerAddressLine1()).thenReturn("123 Test St");
        Mockito.when(customer.getCustomerAddressLine2()).thenReturn("Suite 100");
        Mockito.when(customer.getCustomerAddressContactName()).thenReturn("John Doe");
        Mockito.when(customer.getCustomerAddressPhoneNumber()).thenReturn("123-456-7890");
        Mockito.when(customer.getCustomerAddressDepartmentName()).thenReturn("Shipping");
        Mockito.when(model.getShipmentCustomer()).thenReturn(customer);
        return model;
    }

}



