package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception.ProductCriteriaValidationException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception.ProductValidationException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.InventoryVolume;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.InventoryService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

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
    private static final String VOLUME_TYPE = "volume";
    private static final String MINIMUM_VOLUME_CRITERIA_TYPE = "MINIMUM_VOLUME";
    private static final String MAXIMUM_UNITS_BY_CARTON_CRITERIA_TYPE = "MAXIMUM_UNITS_BY_CARTON";
    private static final String SYSTEM_ERROR_TYPE = "SYSTEM";
    private static final String WARN_ERROR_TYPE = "WARN";



    public static CartonItem createNewCartonItem(PackItemCommand packItemCommand, Carton carton , InventoryService inventoryService
        , CartonItemRepository cartonItemRepository
        , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository
        , RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository) {


        validateProductAlreadyPacked(packItemCommand,cartonItemRepository);

        var shipment = getShipment(carton.getShipmentId(), recoveredPlasmaShippingRepository);

        validateProductType(recoveredPlasmaShipmentCriteriaRepository, shipment.getProductType(), packItemCommand.getProductCode());

        var inventoryValidation = validateInventory(packItemCommand, inventoryService);

        CartonItemBuilder builder = CartonItem.builder();
        builder.id(null);
        builder.cartonId(Optional.of(carton)
            .map(Carton::getId).orElse(null));
        builder.unitNumber(inventoryValidation.getInventory().getUnitNumber());
        builder.productCode(inventoryValidation.getInventory().getProductCode());
        builder.productDescription(inventoryValidation.getInventory().getProductDescription());
        builder.productType(shipment.getProductType());
        builder.volume(inventoryValidation.getInventory().getVolumeByType(VOLUME_TYPE).map(InventoryVolume::getValue).orElse(0));
        builder.weight(inventoryValidation.getInventory().getWeight());
        builder.status(PACKED_STATUS);
        builder.packedByEmployeeId(packItemCommand.getEmployeeId());
        builder.aboRh(inventoryValidation.getInventory().getAboRh());
        builder.expirationDate(inventoryValidation.getInventory().getExpirationDate());
        builder.collectionDate(inventoryValidation.getInventory().getCollectionDate());
        builder.createDate(ZonedDateTime.now());
        builder.modificationDate(null);

        var cartonItem = builder.build();


        validateProductCriteria(shipment.getShipmentCustomer().getCustomerCode(), shipment.getProductType(), cartonItem.getVolume() , carton.getTotalProducts() , recoveredPlasmaShipmentCriteriaRepository );

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
                throw new ProductValidationException(error.getMessage(), SYSTEM_ERROR_TYPE);
            })
            .block();

        if (inventoryValidationResponse.getInventory() != null && (inventoryValidationResponse.getNotifications() == null || inventoryValidationResponse.getNotifications().isEmpty())) {
            return inventoryValidationResponse;
        } else {
            throw new ProductValidationException("Inventory Validation failed",inventoryValidationResponse,WARN_ERROR_TYPE);
        }
    }

    private static void validateProductAlreadyPacked(PackItemCommand packItemCommand , CartonItemRepository cartonItemRepository){
        if(cartonItemRepository == null){
            throw new IllegalArgumentException("Carton Item Repository is required");
        }

        cartonItemRepository.countByProduct(packItemCommand.getUnitNumber(), packItemCommand.getProductCode())
            .onErrorResume(error -> {
                throw new ProductValidationException(error.getMessage(),SYSTEM_ERROR_TYPE);
            })
            .blockOptional()
            .ifPresent(count -> {
                if(count > 0){
                    throw new ProductValidationException("Product already used",WARN_ERROR_TYPE);
                }
            });
    }

    private static void validateProductType(RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository , String cartonProductType, String productCode){
        if(recoveredPlasmaShipmentCriteriaRepository == null){
            log.error("Recovered Plasma Shipment Criteria Repository is required");
            throw new IllegalArgumentException("Recovered Plasma Shipment Criteria Repository is required");
        }

        recoveredPlasmaShipmentCriteriaRepository.findProductTypeByProductCode(productCode)
            .onErrorResume(error -> {
                log.error("Error finding product type: {}", error.getMessage());
                throw new ProductValidationException(error.getMessage(), SYSTEM_ERROR_TYPE);
            })
            .switchIfEmpty(Mono.error(() -> {
                log.warn("No results for product code {}", productCode);
                return new ProductValidationException("Product Type does not match", WARN_ERROR_TYPE);
            }))
            .blockOptional()
            .ifPresent(productType -> {
                if(!productType.getProductType().equals(cartonProductType)) {
                    log.warn("Product Type {} does not match with Carton product type {}", productType.getProductType(), cartonProductType);
                    throw new ProductValidationException("Product Type does not match", WARN_ERROR_TYPE);
                }
            });

    }

    private static RecoveredPlasmaShipment getShipment(Long shipmentId , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository) {
        if (recoveredPlasmaShippingRepository == null) {
            throw new IllegalArgumentException("RecoveredPlasmaShippingRepository is required");
        }

        return recoveredPlasmaShippingRepository.findOneById(shipmentId)
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("Shipment is required")))
            .block();
    }

    private static void validateProductCriteria(String customerCode , String productType ,Integer volume , Integer totalProducts, RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository){

        if (recoveredPlasmaShipmentCriteriaRepository == null) {
            throw new IllegalArgumentException("RecoveredPlasmaShipmentCriteriaRepository is required");
        }

        var criteria = recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(productType, customerCode)
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("Product Criteria not found")))
            .block();

        if(criteria != null){

            var minVolumeCriteria = criteria.findCriteriaItemByType(MINIMUM_VOLUME_CRITERIA_TYPE);
            if(minVolumeCriteria.isPresent()){
                if(volume < Integer.parseInt(minVolumeCriteria.get().getValue())){
                    throw new ProductCriteriaValidationException(minVolumeCriteria.get().getMessage(), minVolumeCriteria.get().getMessageType() , minVolumeCriteria.get().getType());
                }
            }else{
                log.debug("Criteria configuration is missed skip validation {}", MINIMUM_VOLUME_CRITERIA_TYPE);
            }

            var maxUnitsCriteria = criteria.findCriteriaItemByType(MAXIMUM_UNITS_BY_CARTON_CRITERIA_TYPE);
            if(maxUnitsCriteria.isPresent()){
                if(totalProducts +1 > Integer.parseInt(maxUnitsCriteria.get().getValue())){
                    throw new ProductCriteriaValidationException(maxUnitsCriteria.get().getMessage(), maxUnitsCriteria.get().getMessageType() , maxUnitsCriteria.get().getType());
                }
            }else{
                log.debug("Criteria configuration is missed skip validation {}", MAXIMUM_UNITS_BY_CARTON_CRITERIA_TYPE);
            }
        }

    }
}

