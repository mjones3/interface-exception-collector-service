package com.arcone.biopro.distribution.partnerorderprovider.unit.application.usecase;

import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.ModifyOrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.application.usecase.ModifyOrderInboundUseCase;
import com.arcone.biopro.distribution.partnerorderprovider.domain.event.ModifyOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderprovider.domain.service.ModifyOrderInboundService;
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
@ContextConfiguration(classes = ModifyOrderInboundUseCase.class)
class ModifyOrderInboundUseCaseTest {


    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private ModifyOrderInboundService modifyOrderInboundService;

    @Test
    public void shouldReceiveModifyOrderInbound(){

        var modifyInboundDto = Mockito.mock(ModifyOrderInboundDTO.class);
        Mockito.when(modifyInboundDto.getExternalId()).thenReturn("123");
        Mockito.when(modifyInboundDto.getLocationCode()).thenReturn("123");
        Mockito.when(modifyInboundDto.getModifyEmployeeCode()).thenReturn("emp-test");
        Mockito.when(modifyInboundDto.getModifyDate()).thenReturn("date");
        Mockito.when(modifyInboundDto.getDeliveryType()).thenReturn("DeliveryType");
        Mockito.when(modifyInboundDto.getShippingMethod()).thenReturn("ShippingMethod");
        Mockito.when(modifyInboundDto.getProductCategory()).thenReturn("ProductCategory");
        Mockito.when(modifyInboundDto.getDesiredShippingDate()).thenReturn("DesiredShippingDate");

        var response = modifyOrderInboundService.receiveModifyOrderInbound(modifyInboundDto);

        Assertions.assertNotNull(response);
        assertEquals("ACCEPTED", response.status());

        assertEquals(1, applicationEvents
            .stream(ModifyOrderInboundReceived.class)
            .filter(event -> event.getPayload().getExternalId().equals("123"))
            .count());
    }

}
