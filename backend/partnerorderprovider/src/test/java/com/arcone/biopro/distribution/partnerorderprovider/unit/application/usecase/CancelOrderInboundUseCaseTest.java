package com.arcone.biopro.distribution.partnerorderprovider.unit.application.usecase;

import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.CancelOrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.OrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.application.usecase.CancelOrderInboundUseCase;
import com.arcone.biopro.distribution.partnerorderprovider.domain.event.CancelOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderprovider.domain.event.PartnerOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderprovider.domain.service.CancelOrderInboundService;
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
@ContextConfiguration(classes = CancelOrderInboundUseCase.class)
class CancelOrderInboundUseCaseTest {

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private CancelOrderInboundService cancelOrderInboundService;

    @Test
    public void shouldReceiveCancelOrder(){

        var inboundDto = Mockito.mock(CancelOrderInboundDTO.class);
        Mockito.when(inboundDto.getExternalId()).thenReturn("123");
        Mockito.when(inboundDto.getCancelEmployeeCode()).thenReturn("emp-test");
        Mockito.when(inboundDto.getCancelDate()).thenReturn("ShipmentType");
        Mockito.when(inboundDto.getCancelReason()).thenReturn("DeliveryType");

        var response = cancelOrderInboundService.receiveCancelOrderInbound(inboundDto);

        Assertions.assertNotNull(response);
        assertEquals("ACCEPTED", response.status());

        assertEquals(1, applicationEvents
            .stream(CancelOrderInboundReceived.class)
            .filter(event -> event.getPayload().getExternalId().equals("123"))
            .count());
    }
}
