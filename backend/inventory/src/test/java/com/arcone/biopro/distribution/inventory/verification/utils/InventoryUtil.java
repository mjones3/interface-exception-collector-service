package com.arcone.biopro.distribution.inventory.verification.utils;

import com.arcone.biopro.distribution.inventory.application.dto.*;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.PropertyKey;
import com.arcone.biopro.distribution.inventory.domain.model.vo.InputProduct;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.PropertyEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.PropertyEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryUtil {

    private final InventoryEntityRepository inventoryEntityRepository;
    private final PropertyEntityRepository propertyEntityRepository;


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
            .inventoryLocation(defaultLocation)
            .collectionLocation(defaultLocation)
            .collectionDate(ZonedDateTime.now())
            .collectionTimeZone(ZonedDateTime.now().getZone().toString())
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

    public void createInventory(String unitNumber, String productCode, String productFamily, AboRhType aboRhType, String location, String expireIn, InventoryStatus status, String temperatureCategory, Boolean isLabeled, String statusReason, String comments, String expirationTimezone, String timezoneRelevant) {
        var inventory = newInventoryEntity(unitNumber, productCode, status);

        var expirePlus = Integer.parseInt(expireIn.split(" ")[0]);
        var expireType = expireIn.split(" ")[1];
        if ("Hours".equals(expireType)) {
            inventory.setExpirationDate(LocalDateTime.now().plusHours(expirePlus));
        } else {
            inventory.setExpirationDate(LocalDateTime.now().plusDays(expirePlus));
        }
        inventory.setInventoryLocation(location);
        inventory.setCollectionLocation(location);
        inventory.setProductFamily(productFamily);
        inventory.setAboRh(aboRhType);
        inventory.setStatusReason(statusReason);
        inventory.setComments(comments);
        inventory.setIsLabeled(isLabeled);
        inventory.setTemperatureCategory(temperatureCategory);
        inventory.setExpirationTimeZone(expirationTimezone);
        saveInventory(inventory);
        if ("Y".equals(timezoneRelevant)) {
            propertyEntityRepository.save(
                PropertyEntity.builder()
                    .id(UUID.randomUUID())
                    .key(PropertyKey.TIMEZONE_RELEVANT.name())
                    .value(timezoneRelevant)
                    .inventoryId(inventory.getId())
                    .build()
            ).block();
        }
    }

    public void createInventory(String unitNumber, String productCode, String productFamily, AboRhType aboRhType, String location, String expireIn, InventoryStatus status, String temperatureCategory) {
        createInventory(unitNumber, productCode, productFamily, aboRhType, location, expireIn, status, temperatureCategory, null, null, null, null, null);
    }

    public void createMultipleProducts(String unitNumber, String productCode, Integer quantity, String productFamily, String aboRh, String location, String expireIn, InventoryStatus status, String temperatureCategory, String expirationTimezone, String timezoneRelevant ) {
        AboRhType aboRhType = AboRhType.valueOf(aboRh);

        for (int i = 0; i < quantity; i++) {
            createInventory(unitNumber, productCode, productFamily, aboRhType, location, expireIn, status, temperatureCategory, true, null, null, expirationTimezone, timezoneRelevant);
        }
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
            .inventoryLocation(defaultLocation)
            .collectionLocation(defaultLocation)
            .collectionDate(ZonedDateTime.now())
            .collectionTimeZone(ZonedDateTime.now().getZone().toString())
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
            .inventoryLocation(defaultLocation)
            .collectionLocation(defaultLocation)
            .collectionDate(ZonedDateTime.now())
            .collectionTimeZone(ZonedDateTime.now().getZone().toString())
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

    public CheckInCompletedInput newCheckInCompletedInput(String unitNumber, String productCode, String collectionLocation, String collectionTimeZone, String checkinLocation) {
        return CheckInCompletedInput.builder()
            .productFamily(ISBTProductUtil.getProductFamily(productCode))
            .aboRh(AboRhType.OP)
            .checkInLocation(checkinLocation)
            .collectionLocation(collectionLocation)
            .collectionTimeZone(collectionTimeZone)
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

    public ProductCompletedInput newProductCompletedInput(String unitNumber, String productCode, Integer volume, Integer anticoagulantVolume, String volumeUnit) {
        List<VolumeInput> volumeInputs = new ArrayList<>();
        if (Objects.nonNull(volume)) {
            volumeInputs.add(new VolumeInput("volume", volume, volumeUnit));
        }
        if (Objects.nonNull(anticoagulantVolume)) {
            volumeInputs.add(new VolumeInput("anticoagulantVolume", anticoagulantVolume, volumeUnit));
        }
        return ProductCompletedInput.builder()
            .productCode(productCode)
            .unitNumber(unitNumber)
            .volumes(volumeInputs)
            .build();
    }
}
