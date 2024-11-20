package com.arcone.biopro.distribution.eventbridge.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentCompletedOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentLineItem;
import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentLineItemProduct;
import com.arcone.biopro.distribution.eventbridge.domain.model.vo.ShipmentCustomer;
import com.arcone.biopro.distribution.eventbridge.domain.model.vo.ShipmentLocation;
import com.arcone.biopro.distribution.eventbridge.domain.model.vo.ShipmentService;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.ShipmentCompletedOutboundMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

class ShipmentCompletedOutboundMapperTest {

    @Test
    public void shouldMapToDto(){

        var target = new ShipmentCompletedOutboundMapper();
        var createDate = ZonedDateTime.now();

        var mockPayload = Mockito.mock(ShipmentCompletedOutbound.class);
        Mockito.when(mockPayload.getShipmentId()).thenReturn(1L);
        Mockito.when(mockPayload.getShipmentDate()).thenReturn(createDate);
        Mockito.when(mockPayload.getExternalId()).thenReturn("EXTERNAL_ID");
        Mockito.when(mockPayload.getQuantityShipped()).thenReturn(10);

        Mockito.when(mockPayload.getShipmentCustomer()).thenReturn(new ShipmentCustomer("CUSTOMER_CODE","CUSTOMER_TYPE"));
        Mockito.when(mockPayload.getShipmentLocation()).thenReturn(new ShipmentLocation("LOCATION_CODE","LOCATION_NAME"));

        var payloadLineItem = Mockito.mock(ShipmentLineItem.class);
        Mockito.when(payloadLineItem.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(payloadLineItem.getQuantityOrdered()).thenReturn(1);
        Mockito.when(payloadLineItem.getQuantityFilled()).thenReturn(1);

        var itemProduct = Mockito.mock(ShipmentLineItemProduct.class);
        Mockito.when(itemProduct.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(itemProduct.getProductCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(itemProduct.getBloodType()).thenReturn("ABO_RH");
        Mockito.when(itemProduct.getExpirationDate()).thenReturn(LocalDateTime.now());
        Mockito.when(itemProduct.getCollectionDate()).thenReturn(ZonedDateTime.now());

        var mockService = Mockito.mock(ShipmentService.class);
        Mockito.when(mockService.code()).thenReturn("SERVICE_CODE");
        Mockito.when(mockService.quantity()).thenReturn(1);

        Mockito.when(mockPayload.getServices()).thenReturn(List.of(mockService));

        Mockito.when(payloadLineItem.getProducts()).thenReturn(List.of(itemProduct));

        Mockito.when(mockPayload.getLineItems()).thenReturn(List.of(payloadLineItem));

        var dto = target.toDto(mockPayload);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(1L,dto.shipmentNumber());
        Assertions.assertEquals("EXTERNAL_ID",dto.externalOrderId());
        Assertions.assertEquals(createDate,dto.shipmentDate());
        Assertions.assertEquals("CUSTOMER_CODE",dto.customerCode());
        Assertions.assertEquals("CUSTOMER_TYPE",dto.customerType());

        Assertions.assertEquals("LOCATION_CODE",dto.shipmentLocationCode());
        Assertions.assertEquals("LOCATION_NAME",dto.shipmentLocationName());

        Assertions.assertEquals(10,dto.quantityShipped());
        Assertions.assertEquals(1,dto.services().size());

        var lineItem = dto.lineItems().getFirst();
        Assertions.assertEquals("PRODUCT_FAMILY",lineItem.productFamily());
        Assertions.assertEquals(1,lineItem.qtyFilled());
        Assertions.assertEquals(1,lineItem.qtyOrdered());
        Assertions.assertEquals(1,lineItem.products().size());

        var service = dto.services().getFirst();
        Assertions.assertEquals("SERVICE_CODE",service.serviceItemCode());
        Assertions.assertEquals(1,service.quantity());

        var product = lineItem.products().getFirst();
        Assertions.assertEquals("PRODUCT_CODE",product.productCode());
        Assertions.assertEquals("UNIT_NUMBER",product.unitNumber());
        Assertions.assertEquals("ABO_RH",product.bloodType());
        Assertions.assertNotNull(product.expirationDate());
        Assertions.assertNotNull(product.collectionDate());

    }

}
