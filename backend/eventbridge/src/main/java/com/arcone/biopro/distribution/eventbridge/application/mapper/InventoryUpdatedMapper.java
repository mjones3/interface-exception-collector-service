package com.arcone.biopro.distribution.eventbridge.application.mapper;

import com.arcone.biopro.distribution.eventbridge.application.dto.InventoryUpdatedPayload;
import com.arcone.biopro.distribution.eventbridge.domain.model.InventoryUpdatedOutbound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryUpdatedMapper {

    public InventoryUpdatedOutbound toDomain(InventoryUpdatedPayload inventoryUpdatedPayload){

        return new InventoryUpdatedOutbound(inventoryUpdatedPayload.updateType(), inventoryUpdatedPayload.unitNumber(), inventoryUpdatedPayload.productCode(),
            inventoryUpdatedPayload.productDescription() , inventoryUpdatedPayload.productFamily(),inventoryUpdatedPayload.bloodType(),
            inventoryUpdatedPayload.expirationDate(), inventoryUpdatedPayload.locationCode(), inventoryUpdatedPayload.storageLocation(),
            inventoryUpdatedPayload.inventoryStatus(), inventoryUpdatedPayload.properties()
        );
    }
}
