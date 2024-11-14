package com.arcone.biopro.distribution.shipping.unit.application.mapper;

import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentMapper;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.SecondVerification;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.VisualInspection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

class ShipmentMapperTest {

    @Test
    public void shouldMapToDto(){

        var mapper = new ShipmentMapper();

        var itemPacked = ShipmentItemPacked
            .builder()
            .id(1L)
            .aboRh("ABO")
            .expirationDate(LocalDateTime.now())
            .shipmentItemId(1L)
            .productCode("PRODUCT_CODE")
            .unitNumber("UNIT_NUMBER")
            .productFamily("PRODUCT_FAMILY")
            .productDescription("DESCRIPTION")
            .collectionDate(ZonedDateTime.now())
            .packedByEmployeeId("PACK_EMPLOYEE_ID")
            .visualInspection(VisualInspection.SATISFACTORY)
            .secondVerification(SecondVerification.COMPLETED)
            .verifiedByEmployeeId("VERIFY_EMPLOYEE_ID")
            .verificationDate(ZonedDateTime.now())
            .build();

        var dto = mapper.toShipmentItemPackedDTO(itemPacked);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(1L,dto.id());
        Assertions.assertEquals("ABO",dto.aboRh());
        Assertions.assertNotNull(dto.expirationDate());
        Assertions.assertEquals(1L,dto.shipmentItemId());
        Assertions.assertEquals("PRODUCT_CODE",dto.productCode());
        Assertions.assertEquals("UNIT_NUMBER",dto.unitNumber());
        Assertions.assertEquals("PRODUCT_FAMILY",dto.productFamily());
        Assertions.assertEquals("DESCRIPTION",dto.productDescription());
        Assertions.assertNotNull(dto.collectionDate());
        Assertions.assertEquals("PACK_EMPLOYEE_ID",dto.packedByEmployeeId());
        Assertions.assertEquals(VisualInspection.SATISFACTORY,dto.visualInspection());
        Assertions.assertEquals(SecondVerification.COMPLETED,dto.secondVerification());
        Assertions.assertEquals("VERIFY_EMPLOYEE_ID",dto.verifiedByEmployeeId());
        Assertions.assertNotNull(dto.verifiedDate());

    }
}
