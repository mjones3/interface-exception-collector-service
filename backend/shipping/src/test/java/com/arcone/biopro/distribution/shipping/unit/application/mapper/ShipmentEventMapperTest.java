package com.arcone.biopro.distribution.shipping.unit.application.mapper;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentItemResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShipmentItemPackedDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentEventMapper;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

class ShipmentEventMapperTest {

    @Test
    public void shouldMapToShipmentCompletedEvent(){

        var mapper = new ShipmentEventMapper();

        var dto = Mockito.mock(ShipmentDetailResponseDTO.class);
        Mockito.when(dto.id()).thenReturn(1L);

        Mockito.when(dto.items()).thenReturn(List.of(ShipmentItemResponseDTO
            .builder()
                .productFamily("PRODUCT_FAMILY")
                .quantity(1)
                .bloodType(BloodType.AP)
                .packedItems(List.of(ShipmentItemPackedDTO.builder()
                        .aboRh("AP")
                        .unitNumber("UNIT_NUMBER")
                        .productCode("PRODUCT_CODE")
                        .expirationDate(LocalDateTime.now())
                        .collectionDate(ZonedDateTime.now())
                    .build()))
            .build()));

        var event = mapper.toShipmentCompletedEvent(dto,"NAME");

        var payload = event.getPayload();

        Assertions.assertEquals(1L,payload.shipmentId());
        Assertions.assertEquals("PRODUCT_FAMILY",payload.lineItems().getFirst().productFamily());
        Assertions.assertEquals("AP",payload.lineItems().getFirst().bloodType());
        Assertions.assertEquals(1,payload.lineItems().getFirst().quantity());

        Assertions.assertEquals("PRODUCT_CODE",payload.lineItems().getFirst().products().getFirst().productCode());
        Assertions.assertEquals("UNIT_NUMBER",payload.lineItems().getFirst().products().getFirst().unitNumber());
        Assertions.assertEquals("AP",payload.lineItems().getFirst().products().getFirst().aboRh());
        Assertions.assertNotNull(payload.lineItems().getFirst().products().getFirst().collectionDate());
        Assertions.assertNotNull(payload.lineItems().getFirst().products().getFirst().expirationDate());

    }

}
