package com.arcone.biopro.distribution.eventbridge.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.eventbridge.domain.model.InventoryUpdatedOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.InventoryUpdatedOutboundMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

class InventoryUpdatedOutboundMapperTest {

    @Test
    public void shouldMapToDto(){

        var target = new InventoryUpdatedOutboundMapper();
        var createDate = LocalDate.now();

        var mockPayload = Mockito.mock(InventoryUpdatedOutbound.class);
        Mockito.when(mockPayload.getUpdateType()).thenReturn("UPDATE_TYPE");
        Mockito.when(mockPayload.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(mockPayload.getProductCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(mockPayload.getProductDescription()).thenReturn("PRODUCT_DESCRIPTION");
        Mockito.when(mockPayload.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(mockPayload.getBloodType()).thenReturn("BLOOD_TYPE");
        Mockito.when(mockPayload.getExpirationDate()).thenReturn(LocalDate.now());
        Mockito.when(mockPayload.getLocationCode()).thenReturn("LOCATION_CODE");
        Mockito.when(mockPayload.getStorageLocation()).thenReturn("STORAGE_LOCATION");
        Mockito.when(mockPayload.getInventoryStatus()).thenReturn(List.of("INVENTORY_STATUS"));
        Mockito.when(mockPayload.getProperties()).thenReturn(Map.of());

        var dto = target.toDto(mockPayload);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals("UPDATE_TYPE",dto.updateType());
        Assertions.assertEquals("UNIT_NUMBER",dto.unitNumber());
        Assertions.assertEquals(createDate,dto.expirationDate());
        Assertions.assertEquals("PRODUCT_CODE",dto.productCode());
        Assertions.assertEquals("PRODUCT_DESCRIPTION",dto.productDescription());

        Assertions.assertEquals("LOCATION_CODE",dto.locationCode());
        Assertions.assertEquals("STORAGE_LOCATION",dto.storageLocation());

        Assertions.assertEquals(1,dto.inventoryStatus().size());
        Assertions.assertEquals("INVENTORY_STATUS",dto.inventoryStatus().getFirst());

    }

}
