package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception.ProductValidationException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.InventoryService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CartonItem implements Validatable {

    private Long id;
    private Long cartonId;
    private String unitNumber;
    private String productCode;
    private String productDescription;
    private String productType;
    private Integer volume;
    private Integer weight;
    private String packedByEmployeeId;
    private String aboRh;
    private String status;
    private LocalDateTime expirationDate;
    private ZonedDateTime collectionDate;
    private ZonedDateTime createDate;
    private ZonedDateTime modificationDate;

    private static final String PACKED_STATUS = "PACKED";

    public static CartonItem createNewCartonItem(PackItemCommand packItemCommand, Carton carton , InventoryService inventoryService , CartonItemRepository cartonItemRepository) {


        validateProductAlreadyPacked(packItemCommand,cartonItemRepository);

        var inventoryValidation = validateInventory(packItemCommand, inventoryService);

        /// Validations
        // Call Inventory
        // Check Product Criteria
         // PRODUCT TYPE
        //  PRODUCT PACKED/SHIPPED



        // Check Recovered Plasma Criteria
            // MINIMUM_VOLUME
            // MINIMUM_UNITS_BY_CARTON
            // MAXIMUM_UNITS_BY_CARTON

        CartonItemBuilder builder = CartonItem.builder();
        builder.id(null);
        builder.cartonId(Optional.ofNullable(carton)
            .map(Carton::getId).orElse(null));
        builder.unitNumber(inventoryValidation.getInventory().getUnitNumber());
        builder.productCode(inventoryValidation.getInventory().getProductCode());
        builder.productDescription(inventoryValidation.getInventory().getProductDescription());
        builder.productType(null);
        builder.volume(null);
        builder.weight(null);
        builder.status(PACKED_STATUS);
        builder.packedByEmployeeId(packItemCommand.getEmployeeId());
        builder.aboRh(inventoryValidation.getInventory().getAboRh());
        builder.expirationDate(inventoryValidation.getInventory().getExpirationDate());
        builder.collectionDate(inventoryValidation.getInventory().getCollectionDate());
        builder.createDate(ZonedDateTime.now());
        builder.modificationDate(null);

        var cartonItem = builder.build();

        cartonItem.checkValid();

        return cartonItem;
    }

    public static CartonItem fromRepository(Long id, Long cartonId, String unitNumber, String productCode, String productDescription, String productType
        , Integer volume, Integer weight, String packedByEmployeeId
        , String aboRh, String status, LocalDateTime expirationDate, ZonedDateTime collectionDate, ZonedDateTime createDate, ZonedDateTime modificationDate) {

        CartonItemBuilder builder = CartonItem.builder();
        builder.id(id);
        builder.cartonId(cartonId);
        builder.unitNumber(unitNumber);
        builder.productCode(productCode);
        builder.productDescription(productDescription);
        builder.productType(productType);
        builder.volume(volume);
        builder.weight(weight);
        builder.status(status);
        builder.packedByEmployeeId(packedByEmployeeId);
        builder.aboRh(aboRh);
        builder.expirationDate(expirationDate);
        builder.collectionDate(collectionDate);
        builder.createDate(createDate);
        builder.modificationDate(modificationDate);

        var cartonItem = builder.build();
        cartonItem.checkValid();

        return cartonItem;
    }



    @Override
    public void checkValid() {
        if (this.cartonId == null) {
            throw new IllegalArgumentException("Carton ID is required");
        }

        if (this.unitNumber == null || this.unitNumber.isBlank()) {
            throw new IllegalArgumentException("Unit Number is required");
        }

        if (this.productCode == null || this.productCode.isBlank()) {
            throw new IllegalArgumentException("Product Code is required");
        }

        if (this.packedByEmployeeId == null || this.packedByEmployeeId.isBlank()) {
            throw new IllegalArgumentException("Packed By Employee ID is required");
        }

        if(productType == null || productType.isBlank()){
            throw new IllegalArgumentException("Product Type is required");
        }

        if(volume == null || volume <= 0){
            throw new IllegalArgumentException("Volume cannot t be null or less than zero");
        }

        if(weight == null || weight <= 0){
            throw new IllegalArgumentException("Weight cannot t be null or less than zero");
        }

        if(productDescription == null || productDescription.isBlank()){
            throw new IllegalArgumentException("Product Description is required");
        }
        if(status == null || status.isBlank()){
            throw new IllegalArgumentException("Status is required");
        }

    }

    private static InventoryValidation validateInventory(PackItemCommand packItemCommand,InventoryService inventoryService){
        if(inventoryService == null){
            throw new IllegalArgumentException("Inventory Service is required");
        }

        var inventoryValidationResponse =  inventoryService.validateInventory(new ValidateInventoryCommand(packItemCommand.getUnitNumber(), packItemCommand.getProductCode(), packItemCommand.getLocationCode()))
            .onErrorResume(error -> {
                throw new ProductValidationException(error.getMessage());
            })
            .block();

        if (inventoryValidationResponse.getInventory() != null && (inventoryValidationResponse.getNotifications() == null || inventoryValidationResponse.getNotifications().isEmpty())) {
            return inventoryValidationResponse;
        } else {
            throw new ProductValidationException("Inventory Validation failed",inventoryValidationResponse);
        }
    }

    private static void validateProductAlreadyPacked(PackItemCommand packItemCommand , CartonItemRepository cartonItemRepository){
        if(cartonItemRepository == null){
            throw new IllegalArgumentException("Carton Item Repository is required");
        }

        cartonItemRepository.countByProduct(packItemCommand.getUnitNumber(), packItemCommand.getProductCode())
            .onErrorResume(error -> {
                throw new ProductValidationException(error.getMessage());
            })
            .blockOptional()
            .ifPresent(count -> {
                if(count > 0){
                    throw new ProductValidationException("Product already used");
                }
            });
    }
}

