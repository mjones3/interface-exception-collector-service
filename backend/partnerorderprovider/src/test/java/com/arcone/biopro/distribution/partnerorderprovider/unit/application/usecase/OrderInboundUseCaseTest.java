package com.arcone.biopro.distribution.partnerorderprovider.unit.application.usecase;

import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.OrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.application.usecase.OrderInboundUseCase;
import com.arcone.biopro.distribution.partnerorderprovider.domain.event.PartnerOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderprovider.domain.service.OrderInboundService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RecordApplicationEvents
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OrderInboundUseCase.class)
class OrderInboundUseCaseTest {


    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private OrderInboundService orderInboundService;

    @Test
    public void shouldReceivePartnerOrder(){

        var orderInboundDto = Mockito.mock(OrderInboundDTO.class);
        Mockito.when(orderInboundDto.getExternalId()).thenReturn("123");
        Mockito.when(orderInboundDto.getBillingCustomerCode()).thenReturn("1");
        Mockito.when(orderInboundDto.getShippingCustomerCode()).thenReturn("2");
        Mockito.when(orderInboundDto.getOrderStatus()).thenReturn("OPEN");
        Mockito.when(orderInboundDto.getLocationCode()).thenReturn("123");
        Mockito.when(orderInboundDto.getCreateDate()).thenReturn("date");
        Mockito.when(orderInboundDto.getCreateEmployeeCode()).thenReturn("emp-test");
        Mockito.when(orderInboundDto.getShipmentType()).thenReturn("ShipmentType");
        Mockito.when(orderInboundDto.getDeliveryType()).thenReturn("DeliveryType");
        Mockito.when(orderInboundDto.getShippingMethod()).thenReturn("ShippingMethod");
        Mockito.when(orderInboundDto.getProductCategory()).thenReturn("ProductCategory");
        Mockito.when(orderInboundDto.getDesiredShippingDate()).thenReturn("DesiredShippingDate");

        var response = orderInboundService.receiveOrderInbound(orderInboundDto);

        Assertions.assertNotNull(response);
        assertEquals("ACCEPTED", response.status());

        assertEquals(1, applicationEvents
            .stream(PartnerOrderInboundReceived.class)
            .filter(event -> event.getPayload().getExternalId().equals("123"))
            .count());
    }

    @Test
    public void shouldReceivePartnerInternalTransferOrder(){

        var orderInboundDto = Mockito.mock(OrderInboundDTO.class);
        Mockito.when(orderInboundDto.getExternalId()).thenReturn("123");
        Mockito.when(orderInboundDto.getBillingCustomerCode()).thenReturn("1");
        Mockito.when(orderInboundDto.getShippingCustomerCode()).thenReturn("2");
        Mockito.when(orderInboundDto.getOrderStatus()).thenReturn("OPEN");
        Mockito.when(orderInboundDto.getLocationCode()).thenReturn("123");
        Mockito.when(orderInboundDto.getCreateDate()).thenReturn("date");
        Mockito.when(orderInboundDto.getCreateEmployeeCode()).thenReturn("emp-test");
        Mockito.when(orderInboundDto.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(orderInboundDto.getDeliveryType()).thenReturn("DeliveryType");
        Mockito.when(orderInboundDto.getShippingMethod()).thenReturn("ShippingMethod");
        Mockito.when(orderInboundDto.getProductCategory()).thenReturn("ProductCategory");
        Mockito.when(orderInboundDto.getDesiredShippingDate()).thenReturn("DesiredShippingDate");
        Mockito.when(orderInboundDto.getQuarantineProducts()).thenReturn(true);
        Mockito.when(orderInboundDto.getLabelStatus()).thenReturn("LABELED");

        var response = orderInboundService.receiveOrderInbound(orderInboundDto);

        Assertions.assertNotNull(response);
        assertEquals("ACCEPTED", response.status());

        assertEquals(1, applicationEvents
            .stream(PartnerOrderInboundReceived.class)
            .filter(event -> event.getPayload().getExternalId().equals("123"))
            .count());
    }
}
