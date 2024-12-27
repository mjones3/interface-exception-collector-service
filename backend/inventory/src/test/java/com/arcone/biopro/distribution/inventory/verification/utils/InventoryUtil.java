package com.arcone.biopro.distribution.inventory.verification.utils;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryUtil {

    private final InventoryEntityRepository inventoryEntityRepository;

    @Value("${default.location}")
    private String defaultLocation;

    /**
     * Creates a new InventoryEntity object with the provided parameters.
     *
     * @param unitNumber  the unit number of the inventory
     * @param productCode the product code of the inventory
     * @param status      the inventory status
     * @return a newly created InventoryEntity instance
     */
    public InventoryEntity createNewInventoryObject(String unitNumber, String productCode, InventoryStatus status) {
        return InventoryEntity.builder()
            .id(UUID.randomUUID())
            .productFamily(ISBTProductUtil.getProductFamily(productCode))
            .aboRh(AboRhType.OP) // Assuming default ABO/Rh type; modify as needed
            .location(defaultLocation)
            .collectionDate(ZonedDateTime.now())
            .inventoryStatus(status)
            .expirationDate(LocalDateTime.now().plusDays(1))
            .unitNumber(unitNumber)
            .productCode(productCode)
            .isLabeled(false)
            .shortDescription(ISBTProductUtil.getProductDescription(productCode))
            .build();
    }

    /**
     * Persists the given InventoryEntity to the repository.
     *
     * @param inventoryEntity the InventoryEntity to be saved
     */
    public void saveInventory(InventoryEntity inventoryEntity) {
        inventoryEntityRepository.save(inventoryEntity).block();
    }
}
