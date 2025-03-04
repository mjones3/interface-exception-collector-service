package com.arcone.biopro.distribution.eventbridge.unit.application.mapper;

import com.arcone.biopro.distribution.eventbridge.application.dto.InventoryUpdatedPayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.InventoryUpdatedMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

class InventoryUpdatedMapperTest {


    @Test
    public void shouldMapToDomain(){

        var target = new InventoryUpdatedMapper();
        var createDate = LocalDate.now();

        var mockPayload = Mockito.mock(InventoryUpdatedPayload.class);
        Mockito.when(mockPayload.updateType()).thenReturn("UPDATE_TYPE");
        Mockito.when(mockPayload.unitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(mockPayload.productCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(mockPayload.productDescription()).thenReturn("PRODUCT_DESCRIPTION");
        Mockito.when(mockPayload.productFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(mockPayload.bloodType()).thenReturn("BLOOD_TYPE");
        Mockito.when(mockPayload.expirationDate()).thenReturn(LocalDate.now());
        Mockito.when(mockPayload.locationCode()).thenReturn("LOCATION_CODE");
        Mockito.when(mockPayload.storageLocation()).thenReturn("STORAGE_LOCATION");
        Mockito.when(mockPayload.inventoryStatus()).thenReturn(List.of("INVENTORY_STATUS"));
        Mockito.when(mockPayload.properties()).thenReturn(Map.of());

        var domain = target.toDomain(mockPayload);

        Assertions.assertNotNull(domain);
        Assertions.assertEquals("UPDATE_TYPE",domain.getUpdateType());
        Assertions.assertEquals("UNIT_NUMBER",domain.getUnitNumber());
        Assertions.assertEquals(createDate,domain.getExpirationDate());
        Assertions.assertEquals("PRODUCT_CODE",domain.getProductCode());
        Assertions.assertEquals("PRODUCT_DESCRIPTION",domain.getProductDescription());

        Assertions.assertEquals("LOCATION_CODE",domain.getLocationCode());
        Assertions.assertEquals("STORAGE_LOCATION",domain.getStorageLocation());

        Assertions.assertEquals(1,domain.getInventoryStatus().size());
        Assertions.assertEquals("INVENTORY_STATUS",domain.getInventoryStatus().getFirst());

    }

}
