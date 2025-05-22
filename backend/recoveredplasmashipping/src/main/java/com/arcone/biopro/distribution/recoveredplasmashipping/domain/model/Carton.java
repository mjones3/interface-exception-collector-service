package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;


import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.InventoryService;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.LabelTemplateService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Carton implements Validatable {

    private Long id;
    private String cartonNumber;
    private Long shipmentId;
    private Integer cartonSequence;
    private String createEmployeeId;
    private String closeEmployeeId;
    private ZonedDateTime createDate;
    private ZonedDateTime modificationDate;
    private ZonedDateTime closeDate;
    private String status;
    @Getter(AccessLevel.NONE)
    private int totalProducts;
    private BigDecimal totalWeight;
    private BigDecimal totalVolume;
    private List<CartonItem> products;
    private Integer maxNumberOfProducts;
    private Integer minNumberOfProducts;
    private String repackEmployeeId;
    private ZonedDateTime repackDate;
    private String repackComments;
    private boolean canRemove;
    private String deleteEmployeeId;
    private ZonedDateTime deleteDate;

    private static final String STATUS_OPEN = "OPEN";
    private static final String CARTON_PARTNER_PREFIX_KEY = "RPS_CARTON_PARTNER_PREFIX";
    private static final String RPS_LOCATION_CARTON_CODE_KEY = "RPS_LOCATION_CARTON_CODE";
    private static final String STATUS_VERIFIED = "VERIFIED";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final String STATUS_REPACK = "REPACK";
    private static final String CARTON_LABEL_TEMPLATE_TYPE = "RPS_CARTON_LABEL";
    private static final String RPS_SYSTEM_PROCESS_TYPE = "RPS_CARTON_PACKING_SLIP";


    public static Carton createNewCarton(CreateCartonCommand createCartonCommand , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository, CartonRepository cartonRepository, LocationRepository locationRepository) {

        var shipment = getShipment(createCartonCommand.getRecoveredPlasmaShipmentId() , recoveredPlasmaShippingRepository);

        var totalCartons = getTotalCartons(shipment.getId(),cartonRepository);

        var cartonId = getNextCartonId(cartonRepository);

        var carton = Carton.builder()
            .id(null)
            .cartonNumber(generateCartonNumber(cartonId,shipment.getLocationCode(),locationRepository))
            .shipmentId(shipment.getId())
            .cartonSequence( totalCartons + 1 )
            .createEmployeeId(createCartonCommand.getCreateEmployeeId())
            .closeEmployeeId(null)
            .closeDate(null)
            .createDate(ZonedDateTime.now())
            .modificationDate(null)
            .status(STATUS_OPEN)
            .totalProducts(0)
            .totalWeight(BigDecimal.ZERO)
            .totalVolume(BigDecimal.ZERO)
            .build();

        carton.checkValid();

        return carton;
    }

    private static RecoveredPlasmaShipment getShipment(Long shipmentId , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository) {
        if (recoveredPlasmaShippingRepository == null) {
            throw new IllegalArgumentException("RecoveredPlasmaShippingRepository is required");
        }

        return recoveredPlasmaShippingRepository.findOneById(shipmentId)
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("Shipment is required")))
            .block();
    }

    public static Carton fromRepository(Long id, String cartonNumber, Long shipmentId, Integer cartonSequence, String createEmployeeId, String closeEmployeeId
        , ZonedDateTime createDate, ZonedDateTime modificationDate, ZonedDateTime closeDate, String status , BigDecimal totalVolume , BigDecimal totalWeight
        , List<CartonItem> products , Integer minNumberOfUnits , Integer maxNumberOfUnits) {
        var carton = Carton.builder()
            .id(id)
            .cartonNumber(cartonNumber)
            .shipmentId(shipmentId)
            .cartonSequence(cartonSequence)
            .createEmployeeId(createEmployeeId)
            .closeEmployeeId(closeEmployeeId)
            .createDate(createDate)
            .modificationDate(modificationDate)
            .closeDate(closeDate)
            .status(status)
            .totalWeight(totalWeight)
            .totalVolume(totalVolume)
            .products(products)
            .minNumberOfProducts(minNumberOfUnits)
            .maxNumberOfProducts(maxNumberOfUnits)
            .build();

        carton.checkValid();

        return carton;

    }

    @Override
    public void checkValid() {

        if (this.shipmentId == null) {
            throw new IllegalArgumentException("Shipment Id is required");
        }

        if (this.createEmployeeId == null || this.createEmployeeId.isBlank()) {
            throw new IllegalArgumentException("Create Employee Id is required");
        }

        if (this.status == null || this.status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        if(this.cartonNumber == null || this.cartonNumber.isBlank()){
            throw new IllegalArgumentException("Carton Number is required");
        }

        if(this.cartonSequence == null){
            throw new IllegalArgumentException("Carton Sequence is required");
        }

        if(this.cartonSequence <= 0){
            throw new IllegalArgumentException("Carton Sequence must be greater than 0");
        }

    }

    private static String generateCartonNumber(Long cartonId, String locationCode , LocationRepository locationRepository ) {

        var location = locationRepository.findOneByCode(locationCode)
            .switchIfEmpty(Mono.error( ()-> new DomainNotFoundForKeyException(String.format("%s", locationCode))))
            .block();

        var cartonNumber = "";

        var prefix = location.findProperty(CARTON_PARTNER_PREFIX_KEY);
        if (prefix.isEmpty()) {
            log.error("Location property is missed {}", CARTON_PARTNER_PREFIX_KEY);
            throw new IllegalArgumentException("Location configuration is missing the setup for  " + CARTON_PARTNER_PREFIX_KEY + " property");
        }

        var cartonLocationCode = location.findProperty(RPS_LOCATION_CARTON_CODE_KEY);
        if (cartonLocationCode.isEmpty()) {
            log.error("Location property is missed {}", RPS_LOCATION_CARTON_CODE_KEY);
            throw new IllegalArgumentException("Location configuration is missing the setup for  " + RPS_LOCATION_CARTON_CODE_KEY + " property");
        }
        cartonNumber = String.format("%s%s%s",prefix.get().getPropertyValue(),cartonLocationCode.get().getPropertyValue(),cartonId);

        return cartonNumber;

    }

    private static Long getNextCartonId(CartonRepository cartonRepository) {

        if (cartonRepository == null) {
            throw new IllegalArgumentException("CartonRepository is required");
        }

        return cartonRepository.getNextCartonId()
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("Carton ID is required")))
            .block();
    }

    private static Integer getTotalCartons(Long shipmentId,CartonRepository cartonRepository) {

        if (cartonRepository == null) {
            throw new IllegalArgumentException("CartonRepository is required");
        }

        return cartonRepository.countByShipment(shipmentId)
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("Total Cartons is required")))
            .block();
    }

    public int getTotalProducts() {
        if(this.products == null){
            return  0;
        }
        return this.products.size();
    }

    public CartonItem packItem(PackItemCommand packItemCommand , InventoryService inventoryService, CartonItemRepository cartonItemRepository , RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository) {
        if(this.products == null){
            this.products = new ArrayList<>();
        }

        var item = CartonItem.createNewCartonItem(packItemCommand,this,inventoryService , cartonItemRepository , recoveredPlasmaShippingRepository , recoveredPlasmaShipmentCriteriaRepository);

        this.products.add(item);

        return item;
    }

    public boolean canVerify(){
        return STATUS_OPEN.equals(this.status) && this.getTotalProducts() >= minNumberOfProducts;
    }

    public boolean canClose(){
        var verifiedProducts = getVerifiedProducts();
        if(verifiedProducts == null || verifiedProducts.isEmpty()){
            return false;
        }
        return this.status.equals(STATUS_OPEN) && this.getTotalProducts() > 0 && verifiedProducts.size() == this.getTotalProducts() && verifiedProducts.size() >= this.minNumberOfProducts;
    }

    public List<CartonItem> getVerifiedProducts(){
        if(this.products == null){
            return new ArrayList<>();
        }

        return this.products.stream().filter(product -> product.getStatus().equals(STATUS_VERIFIED)).toList();
    }

    public CartonItem verifyItem(VerifyItemCommand verifyItemCommand , InventoryService inventoryService, CartonItemRepository cartonItemRepository , RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository){
        if(!canVerify()){
            log.warn("Carton is not ready for verification {}", this.cartonNumber);
            throw new IllegalArgumentException("Carton cannot be verified");
        }

        return CartonItem.verifyCartonItem(verifyItemCommand, this, inventoryService, cartonItemRepository, recoveredPlasmaShippingRepository, recoveredPlasmaShipmentCriteriaRepository);
    }

    public Carton close(CloseCartonCommand closeCartonCommand){
        if(!canClose()){
            log.warn("Carton is not ready for closing {}", this.cartonNumber);
            throw new IllegalArgumentException("Carton cannot be closed");
        }

        this.closeEmployeeId = closeCartonCommand.getEmployeeId();
        this.closeDate = ZonedDateTime.now();
        this.status = STATUS_CLOSED;

        return this;
    }

    public boolean canPrint(){
        return STATUS_CLOSED.equals(this.status);
    }

    public CartonPackingSlip generatePackingSlip(LocationRepository locationRepository, SystemProcessPropertyRepository systemProcessPropertyRepository
        , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository , RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository){

        if(!canPrint()){
            log.warn("Carton is not ready for printing {}", this.cartonNumber);
            throw new IllegalArgumentException("Carton cannot be printed");
        }

        return CartonPackingSlip.generatePackingSlip(this,locationRepository , systemProcessPropertyRepository , recoveredPlasmaShippingRepository , recoveredPlasmaShipmentCriteriaRepository);
    }

    public Carton markAsRepack(){
        this.status = STATUS_REPACK;
        this.closeDate = null;
        this.closeEmployeeId = null;

        return this;
    }

    public Carton markAsReopen(final RepackCartonCommand repackCartonCommand){
        if(repackCartonCommand == null){
            throw new IllegalArgumentException("RepackCartonCommand is required");
        }

        if(!STATUS_REPACK.equals(this.status)){
            throw new IllegalArgumentException("Carton cannot be repacked");
        }

        this.status = STATUS_OPEN;
        this.closeDate = null;
        this.closeEmployeeId = null;
        this.repackDate = ZonedDateTime.now();
        this.repackEmployeeId = repackCartonCommand.getEmployeeId();
        this.repackComments = repackCartonCommand.getReasonComments();
        return this;
    }

    public Carton marForRemoval(){
        this.canRemove = Boolean.TRUE;
        return  this;
    }

    public Carton removeCarton(RemoveCartonCommand removeCartonCommand , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository){

        if(STATUS_CLOSED.equals(this.status)){
            throw new IllegalArgumentException("Carton is closed and cannot be removed");
        }

        var shipment = getShipment(this.shipmentId, recoveredPlasmaShippingRepository);
        if(shipment == null){
            throw new IllegalArgumentException("Shipment not found");
        }

        if(!shipment.canModify()){
            throw new IllegalArgumentException("Shipment is closed and cannot be modified");
        }

        if(this.cartonSequence != shipment.getTotalCartons()){
            throw new IllegalArgumentException("Carton is not the last one and cannot be removed");
        }

        this.deleteEmployeeId = removeCartonCommand.getEmployeeId();
        this.deleteDate = ZonedDateTime.now();
        this.status = "REMOVED";

        return this;

    }

    public String generateCartonLabel(LabelTemplateService labelTemplateService , LocationRepository locationRepository
        , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository , SystemProcessPropertyRepository systemProcessPropertyRepository ){

        var shipment = getShipment(this.shipmentId, recoveredPlasmaShippingRepository);
        if(shipment == null){
            throw new IllegalArgumentException("Shipment not found");
        }

        var location = locationRepository.findOneByCode(shipment.getLocationCode())
            .switchIfEmpty(Mono.error(()-> new DomainNotFoundForKeyException(String.format("%s", shipment.getLocationCode()))))
            .block();

        var productCode = this.getProducts().getFirst().getProductCode();

        var bloodCenterName = getSystemPropertyByKey(getSystemProperties(systemProcessPropertyRepository),"BLOOD_CENTER_NAME");

        return labelTemplateService.processTemplate(CARTON_LABEL_TEMPLATE_TYPE, new CartonLabel(shipment.getShipmentCustomer(),this.cartonNumber,this.cartonSequence,this.closeDate,bloodCenterName,location
            , shipment.getTransportationReferenceNumber(), shipment.getShipmentNumber(), productCode )).block();

    }


    private static String getSystemPropertyByKey(List<SystemProcessProperty> systemProcessProperties , String key) {
        return systemProcessProperties.stream()
            .filter(systemProcessProperty -> key.equals(systemProcessProperty.getPropertyKey()))
            .map(SystemProcessProperty::getPropertyValue)
            .findFirst()
            .orElseThrow(()-> new IllegalArgumentException("System Property value is required for the Key : "+key));
    }


    private static List<SystemProcessProperty> getSystemProperties(SystemProcessPropertyRepository systemProcessPropertyRepository) {
        return systemProcessPropertyRepository.findAllByType(RPS_SYSTEM_PROCESS_TYPE)
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("System Property is required: "+RPS_SYSTEM_PROCESS_TYPE)))
            .collectList()
            .block();
    }
}
