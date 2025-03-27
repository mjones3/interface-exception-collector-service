package com.arcone.biopro.distribution.inventory.verification.utils;

import com.arcone.biopro.distribution.inventory.application.dto.CheckInCompletedInput;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryInput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductCreatedInput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductDiscardedInput;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.InputProduct;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    public InventoryEntity newInventoryEntity(String unitNumber, String productCode, InventoryStatus status) {
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
            .storageLocation("FREEZER 1, RACK 1, SHELF 1")
            .weight(100)
            .isLabeled(true)
            .isLicensed(true)
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

    /**
     * Creates a new InventoryInput object with the provided parameters.
     *
     * @param unitNumber  the unit number
     * @param productCode the product code
     * @param isLicensed  the licensing status
     * @return a newly created InventoryInput instance
     */
    public InventoryInput newInventoryInput(String unitNumber, String productCode, Boolean isLicensed) {
        return InventoryInput.builder()
            .isLicensed(isLicensed)
            .productFamily(ISBTProductUtil.getProductFamily(productCode))
            .aboRh(AboRhType.OP)
            .location(defaultLocation)
            .collectionDate(ZonedDateTime.now())
            .expirationDate(LocalDateTime.now().plusDays(1))
            .weight(100)
            .unitNumber(unitNumber)
            .productCode(productCode)
            .shortDescription(ISBTProductUtil.getProductDescription(productCode))
            .build();
    }


    public ProductCreatedInput newProductCreatedInput(String unitNumber, String productCode, List<InputProduct> inputProducts) {
        return this.newProductCreatedInput(unitNumber, productCode, inputProducts, true);
    }

    /**
     * Creates a new ProductCreatedInput object with the provided parameters.
     *
     * @param unitNumber    the unit number
     * @param productCode   the product code
     * @param inputProducts the list of input products
     * @return a newly created ProductCreatedInput instance
     */
    public ProductCreatedInput newProductCreatedInput(String unitNumber, String productCode, List<InputProduct> inputProducts, Boolean hasExpDate) {
        var builder = ProductCreatedInput.builder();
        builder.productFamily(ISBTProductUtil.getProductFamily(productCode))
            .aboRh(AboRhType.OP)
            .location(defaultLocation)
            .collectionDate(ZonedDateTime.now())
            .weight(100)
            .unitNumber(unitNumber)
            .productCode(productCode)
            .productDescription(ISBTProductUtil.getProductDescription(productCode))
            .inputProducts(inputProducts);
        if(hasExpDate) {
            builder.expirationDate(DateTimeFormatter.ofPattern("MM/dd/yyyy").format(LocalDate.now().plusDays(1)))
                .expirationTime(LocalTime.now().toString());
        }
        return builder.build();
    }

    public CheckInCompletedInput newCheckInCompletedInput(String unitNumber, String productCode) {
        return CheckInCompletedInput.builder()
            .productFamily(ISBTProductUtil.getProductFamily(productCode))
            .aboRh(AboRhType.OP)
            .location(defaultLocation)
            .collectionDate(ZonedDateTime.now())
            .unitNumber(unitNumber)
            .productCode(productCode)
            .productDescription(ISBTProductUtil.getProductDescription(productCode))
            .build();
    }

    public ProductDiscardedInput newProductDiscardedInput(String unitNumber, String productCode, String reason, String comments) {
        return ProductDiscardedInput.builder()
            .unitNumber(unitNumber)
            .productCode(productCode)
            .reason(reason)
            .comments(comments)
            .build();
    }

//    public Object newProductCompletedInput(String unitNumber, String productCode, String volume, String anticoagulantVolume) {
//
//    }
}
