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
    private String verifiedByEmployeeId;
    private ZonedDateTime verifyDate;
    private String collectionLocation;
    private String collectionTimeZone;

    private static final String PACKED_STATUS = "PACKED";
    private static final String VOLUME_TYPE = "volume";
    private static final String MINIMUM_VOLUME_CRITERIA_TYPE = "MINIMUM_VOLUME";
    private static final String MAXIMUM_UNITS_BY_CARTON_CRITERIA_TYPE = "MAXIMUM_UNITS_BY_CARTON";
    private static final String SYSTEM_ERROR_TYPE = "SYSTEM";
    private static final String WARN_ERROR_TYPE = "WARN";
    private static final String VERIFIED_STATUS = "VERIFIED";



    public static CartonItem createNewCartonItem(PackItemCommand packItemCommand, Carton carton , InventoryService inventoryService
        , CartonItemRepository cartonItemRepository
        , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository
        , RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository) {

        validateProductCodeUnique(carton,packItemCommand);

        validateProductAlreadyPacked(packItemCommand,cartonItemRepository);

        var shipment = getShipment(carton.getShipmentId(), recoveredPlasmaShippingRepository);

        validateProductType(recoveredPlasmaShipmentCriteriaRepository, shipment.getProductType(), packItemCommand.getProductCode());

        var inventoryValidation = validateInventory(packItemCommand.getUnitNumber(), packItemCommand.getProductCode() , packItemCommand.getLocationCode(), inventoryService);

        var cartonItem = CartonItem.builder()
            .id(null)
            .cartonId(Optional.of(carton).map(Carton::getId).orElse(null))
            .unitNumber(inventoryValidation.getInventory().getUnitNumber())
            .productCode(inventoryValidation.getInventory().getProductCode())
            .productDescription(inventoryValidation.getInventory().getProductDescription())
            .productType(shipment.getProductType())
            .volume(inventoryValidation.getInventory().getVolumeByType(VOLUME_TYPE).map(InventoryVolume::getValue).orElse(0))
            .weight(inventoryValidation.getInventory().getWeight())
            .status(PACKED_STATUS)
            .packedByEmployeeId(packItemCommand.getEmployeeId())
            .aboRh(inventoryValidation.getInventory().getAboRh())
            .expirationDate(inventoryValidation.getInventory().getExpirationDate())
            .collectionDate(inventoryValidation.getInventory().getCollectionDate())
            .collectionLocation(inventoryValidation.getInventory().getCollectionLocation())
            .collectionTimeZone(inventoryValidation.getInventory().getCollectionTimeZone())
            .createDate(ZonedDateTime.now())
            .modificationDate(null)
            .build();

        validateProductCriteria(shipment.getShipmentCustomer().getCustomerCode(), shipment.getProductType(), cartonItem.getVolume() , carton.getTotalProducts() , recoveredPlasmaShipmentCriteriaRepository , Boolean.TRUE );

        cartonItem.checkValid();
        return cartonItem;
    }

    public static CartonItem fromRepository(Long id, Long cartonId, String unitNumber, String productCode, String productDescription, String productType
        , Integer volume, Integer weight, String packedByEmployeeId
        , String aboRh, String status, LocalDateTime expirationDate, ZonedDateTime collectionDate, ZonedDateTime createDate, ZonedDateTime modificationDate , String verifiedByEmployeeId , ZonedDateTime verifyDate) {

        var cartonItem = CartonItem.builder()
            .id(id)
            .cartonId(cartonId)
            .unitNumber(unitNumber)
            .productCode(productCode)
            .productDescription(productDescription)
            .productType(productType)
            .volume(volume)
            .weight(weight)
            .status(status)
            .packedByEmployeeId(packedByEmployeeId)
            .aboRh(aboRh)
            .expirationDate(expirationDate)
            .collectionDate(collectionDate)
            .createDate(createDate)
            .modificationDate(modificationDate)
            .verifiedByEmployeeId(verifiedByEmployeeId)
            .verifyDate(verifyDate)
            .build();

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
        if(VERIFIED_STATUS.equals(status) && (verifiedByEmployeeId == null || verifiedByEmployeeId.isBlank())){
            throw new IllegalArgumentException("Verified By EmployeeId is required");
        }
        if(VERIFIED_STATUS.equals(status) && (verifyDate == null)){
            throw new IllegalArgumentException("Verified Date is required");
        }

    }

    private static InventoryValidation validateInventory(String unitNumber, String productCode , String locationCode,InventoryService inventoryService){
        if(inventoryService == null){
            throw new IllegalArgumentException("Inventory Service is required");
        }

        var inventoryValidationResponse =  inventoryService.validateInventory(new ValidateInventoryCommand(unitNumber, productCode, locationCode))
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
                    throw new ProductValidationException("Product already added in a carton",WARN_ERROR_TYPE);
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

    private static void validateProductCriteria(String customerCode , String productType ,Integer volume , Integer totalProducts, RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository , boolean packing){

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
            if(maxUnitsCriteria.isPresent() && packing){
                if(totalProducts +1 > Integer.parseInt(maxUnitsCriteria.get().getValue())){
                    throw new ProductCriteriaValidationException(maxUnitsCriteria.get().getMessage(), maxUnitsCriteria.get().getMessageType() , maxUnitsCriteria.get().getType());
                }
            }else{
                log.debug("Criteria configuration is missed skip validation {}", MAXIMUM_UNITS_BY_CARTON_CRITERIA_TYPE);
            }
        }

    }



    public static CartonItem verifyCartonItem(VerifyItemCommand verifyItemCommand, Carton carton , InventoryService inventoryService
        , CartonItemRepository cartonItemRepository
        , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository
        , RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository) {

        var cartonItem = validateProductIsPacked(verifyItemCommand,cartonItemRepository);

        var shipment = getShipment(carton.getShipmentId(), recoveredPlasmaShippingRepository);

        if(!verifyItemCommand.getLocationCode().equals(shipment.getLocationCode())){
            log.warn("Carton cannot be verified from a different location {} {}", verifyItemCommand.getLocationCode(),shipment.getLocationCode() );
            throw new ProductValidationException("Carton cannot be verified from a different location", WARN_ERROR_TYPE);
        }

        validateProductType(recoveredPlasmaShipmentCriteriaRepository, shipment.getProductType(), verifyItemCommand.getProductCode());

        var inventoryValidation = validateInventory(verifyItemCommand.getUnitNumber(), verifyItemCommand.getProductCode() , verifyItemCommand.getLocationCode(), inventoryService);

        var cartonItemVerified = CartonItem.builder()
            .id(cartonItem.getId())
            .cartonId(cartonItem.getCartonId())
            .unitNumber(cartonItem.getUnitNumber())
            .productCode(cartonItem.getProductCode())
            .productDescription(cartonItem.getProductDescription())
            .productType(cartonItem.getProductType())
            .volume(inventoryValidation.getInventory().getVolumeByType(VOLUME_TYPE).map(InventoryVolume::getValue).orElse(0))
            .weight(inventoryValidation.getInventory().getWeight())
            .packedByEmployeeId(cartonItem.getPackedByEmployeeId())
            .aboRh(cartonItem.getAboRh())
            .expirationDate(inventoryValidation.getInventory().getExpirationDate())
            .collectionDate(inventoryValidation.getInventory().getCollectionDate())
            .collectionTimeZone(inventoryValidation.getInventory().getCollectionTimeZone())
            .collectionLocation(inventoryValidation.getInventory().getCollectionLocation())
            .createDate(cartonItem.getCreateDate())
            .modificationDate(ZonedDateTime.now())
            .status(VERIFIED_STATUS)
            .verifyDate(ZonedDateTime.now())
            .verifiedByEmployeeId(verifyItemCommand.getEmployeeId())
            .build();

        validateProductCriteria(shipment.getShipmentCustomer().getCustomerCode(), shipment.getProductType(), cartonItemVerified.getVolume() , carton.getTotalProducts() , recoveredPlasmaShipmentCriteriaRepository , Boolean.FALSE );

        cartonItemVerified.checkValid();

        return cartonItemVerified;
    }

    private static CartonItem validateProductIsPacked(VerifyItemCommand verifyItemCommand , CartonItemRepository cartonItemRepository){
        if(cartonItemRepository == null){
            throw new IllegalArgumentException("Carton Item Repository is required");
        }

        var item = cartonItemRepository.findByCartonAndProduct(verifyItemCommand.getCartonId(), verifyItemCommand.getUnitNumber(), verifyItemCommand.getProductCode())
            .onErrorResume(error -> {
                throw new ProductValidationException(error.getMessage(),SYSTEM_ERROR_TYPE);
            })
            .switchIfEmpty(Mono.error( ()-> new ProductValidationException("The verification does not match all products in this carton. Please re-scan all the products.", WARN_ERROR_TYPE)))
            .block();

        if(item != null && !item.getStatus().equals(PACKED_STATUS)){
            throw new ProductValidationException("This product has already been verified. Please re-scan all the products in the carton.",WARN_ERROR_TYPE);
        }

        return item;

    }

    private static void validateProductCodeUnique(Carton carton , PackItemCommand packItemCommand){
        if(carton == null){
            throw new IllegalArgumentException("Carton is required");
        }
        if(packItemCommand == null){
            throw new IllegalArgumentException("PackItemCommand is required");
        }

        if(carton.getProducts() != null && !carton.getProducts().isEmpty() && carton.getProducts().stream().noneMatch(cartonItem -> cartonItem.getProductCode().equals(packItemCommand.getProductCode()))){
            log.warn("Preventing Mixing products in a carton {}",packItemCommand);
            throw new ProductValidationException("The product code does not match the products in the carton",WARN_ERROR_TYPE);
        }
    }

    public CartonItem resetVerification(){
        this.status = PACKED_STATUS;
        this.verifyDate = null;
        this.verifiedByEmployeeId = null;
        this.modificationDate = ZonedDateTime.now();
        return this;
    }
}

