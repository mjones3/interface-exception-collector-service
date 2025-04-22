package com.arcone.biopro.distribution.eventbridge.unit.application.mapper;

import com.arcone.biopro.distribution.eventbridge.application.dto.ShipmentCompletedItemPayload;
import com.arcone.biopro.distribution.eventbridge.application.dto.ShipmentCompletedItemProductPayload;
import com.arcone.biopro.distribution.eventbridge.application.dto.ShipmentCompletedPayload;
import com.arcone.biopro.distribution.eventbridge.application.dto.ShipmentCompletedServicePayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.ShipmentCompletedMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

class ShipmentCompletedMapperTest {


    @Test
    public void shouldMapToDomain(){

        var target = new ShipmentCompletedMapper();
        var createDate = ZonedDateTime.now();

        var mockPayload = Mockito.mock(ShipmentCompletedPayload.class);
        Mockito.when(mockPayload.shipmentId()).thenReturn(1L);
        Mockito.when(mockPayload.createDate()).thenReturn(createDate);
        Mockito.when(mockPayload.externalOrderId()).thenReturn("EXTERNAL_ID");
        Mockito.when(mockPayload.customerCode()).thenReturn("CUSTOMER_CODE");
        Mockito.when(mockPayload.customerType()).thenReturn("CUSTOMER_TYPE");
        Mockito.when(mockPayload.customerName()).thenReturn("CUSTOMER_NAME");
        Mockito.when(mockPayload.locationName()).thenReturn("LOCATION_NAME");
        Mockito.when(mockPayload.locationCode()).thenReturn("LOCATION_CODE");
        Mockito.when(mockPayload.deliveryType()).thenReturn("DELIVERY_TYPE");

        var payloadLineItem = Mockito.mock(ShipmentCompletedItemPayload.class);
        Mockito.when(payloadLineItem.productFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(payloadLineItem.quantity()).thenReturn(1);
        Mockito.when(payloadLineItem.bloodType()).thenReturn("BLOOD_TYPE");

        var itemProduct = Mockito.mock(ShipmentCompletedItemProductPayload.class);
        Mockito.when(itemProduct.unitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(itemProduct.productCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(itemProduct.aboRh()).thenReturn("ABO_RH");
        Mockito.when(itemProduct.expirationDate()).thenReturn(LocalDateTime.now());
        Mockito.when(itemProduct.collectionDate()).thenReturn(ZonedDateTime.now());

        var mockService = Mockito.mock(ShipmentCompletedServicePayload.class);
        Mockito.when(mockService.code()).thenReturn("SERVICE_CODE");
        Mockito.when(mockService.quantity()).thenReturn(1);

        Mockito.when(mockPayload.services()).thenReturn(List.of(mockService));

        Mockito.when(payloadLineItem.products()).thenReturn(List.of(itemProduct));

        Mockito.when(mockPayload.lineItems()).thenReturn(List.of(payloadLineItem));

        var domain = target.toDomain(mockPayload);

        Assertions.assertNotNull(domain);
        Assertions.assertEquals(1L,domain.getShipmentId());
        Assertions.assertEquals("EXTERNAL_ID",domain.getExternalId());
        Assertions.assertEquals(createDate,domain.getShipmentDate());
        Assertions.assertEquals("CUSTOMER_CODE",domain.getShipmentCustomer().customerCode());
        Assertions.assertEquals("CUSTOMER_TYPE",domain.getShipmentCustomer().customerType());
        Assertions.assertEquals("CUSTOMER_NAME",domain.getShipmentCustomer().customerName());

        Assertions.assertEquals("LOCATION_CODE",domain.getShipmentLocation().shipmentLocationCode());
        Assertions.assertEquals("LOCATION_NAME",domain.getShipmentLocation().shipmentLocationName());

        Assertions.assertEquals("DELIVERY_TYPE",domain.getDeliveryType());

        Assertions.assertEquals(1,domain.getQuantityShipped());
        Assertions.assertEquals(1,domain.getServices().size());

        var lineItem = domain.getLineItems().getFirst();
        Assertions.assertEquals("PRODUCT_FAMILY",lineItem.getProductFamily());
        Assertions.assertEquals(1,lineItem.getQuantityFilled());
        Assertions.assertEquals(1,lineItem.getProducts().size());

        var service = domain.getServices().getFirst();
        Assertions.assertEquals("SERVICE_CODE",service.code());
        Assertions.assertEquals(1,service.quantity());

        var product = lineItem.getProducts().getFirst();
        Assertions.assertEquals("PRODUCT_CODE",product.getProductCode());
        Assertions.assertEquals("UNIT_NUMBER",product.getUnitNumber());
        Assertions.assertEquals("ABO_RH",product.getBloodType());
        Assertions.assertNotNull(product.getExpirationDate());
        Assertions.assertNotNull(product.getCollectionDate());


    }

}
