package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.PackingSlipProduct;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.PackingSlipShipFrom;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.PackingSlipShipTo;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.PackingSlipShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CartonPackingSlip implements Validatable {

    private Long cartonId;
    private String cartonNumber;
    private Integer cartonSequence;
    private int totalProducts;
    private String dateTimePacked;
    private String packedByEmployeeId;
    private PackingSlipShipFrom shipFrom;
    private PackingSlipShipTo shipTo;
    private PackingSlipShipment packingSlipShipment;
    private String testingStatement;
    private boolean displaySignature;
    private boolean displayTransportationReferenceNumber;
    private boolean displayTestingStatement;
    private boolean displayLicenceNumber;
    private List<PackingSlipProduct> packedProducts;

    private static final String RPS_SYSTEM_PROCESS_TYPE = "RPS_CARTON_PACKING_SLIP";
    private static final String YES_PROPERTY_VALUE = "Y";


    public static CartonPackingSlip generatePackingSlip(final Carton carton, LocationRepository locationRepository
        , SystemProcessPropertyRepository systemProcessPropertyRepository, RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository
        , RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository){

        var systemProperties = getSystemProperties(systemProcessPropertyRepository);

        var shipment = getShipment(carton.getShipmentId(), recoveredPlasmaShippingRepository);

        var location = getLocation(shipment.getLocationCode(), locationRepository);

       var packingSlip = CartonPackingSlip
           .builder()
           .cartonId(carton.getId())
           .cartonNumber(carton.getCartonNumber())
           .cartonSequence(carton.getCartonSequence())
           .totalProducts(carton.getTotalProducts())
           .packedByEmployeeId(carton.getCloseEmployeeId())
           .dateTimePacked(formatDateTime(carton.getCloseDate(),systemProperties,location))
           .packedProducts(buildPackedProducts(carton,systemProperties,location))
           .packingSlipShipment(buildPackSlipShipment(shipment,recoveredPlasmaShipmentCriteriaRepository))
           .shipFrom(buildShipFrom(systemProperties,location))
           .displaySignature(YES_PROPERTY_VALUE.equals(getSystemPropertyByKey(systemProperties,"USE_SIGNATURE")))
           .displayTransportationReferenceNumber(YES_PROPERTY_VALUE.equals(getSystemPropertyByKey(systemProperties,"USE_TRANSPORTATION_NUMBER")))
           .displayTestingStatement(YES_PROPERTY_VALUE.equals(getSystemPropertyByKey(systemProperties,"USE_TESTING_STATEMENT")))
           .displayLicenceNumber(YES_PROPERTY_VALUE.equals(getSystemPropertyByKey(systemProperties,"USE_LICENSE_NUMBER")))
           .shipTo(buildShipTo(shipment,systemProperties))
           .testingStatement(buildTestingStatement(carton.getCloseEmployeeId(), systemProperties))
           .build();

       packingSlip.checkValid();

       return packingSlip;

    }

    @Override
    public void checkValid() {
        if(cartonId == null){
            throw new IllegalArgumentException("Carton id is required");
        }

        if(cartonNumber == null || cartonNumber.isBlank()){
            throw new IllegalArgumentException("Carton Number is required");
        }

        if(cartonSequence == null){
            throw new IllegalArgumentException("Carton Sequence is required");
        }

        if(dateTimePacked == null || dateTimePacked.isBlank()){
            throw new IllegalArgumentException("Date Time Packed is required");
        }
        if(packedByEmployeeId == null || packedByEmployeeId.isBlank()){
            throw new IllegalArgumentException("Packed By Employee Id is required");
        }
        if(shipFrom == null){
            throw new IllegalArgumentException("Ship From is required");
        }
        if(shipTo == null){
            throw new IllegalArgumentException("Ship To is required");
        }
        if(packingSlipShipment == null){
            throw new IllegalArgumentException("Packing Slip Shipment is required");
        }
        if(packedProducts == null || packedProducts.isEmpty()){
            throw new IllegalArgumentException("Packed Products is required");
        }
    }

    private static List<PackingSlipProduct> buildPackedProducts(final Carton carton , final List<SystemProcessProperty> systemProcessProperties , final Location location){

        if(systemProcessProperties == null || systemProcessProperties.isEmpty()){
            throw new IllegalArgumentException("System Property is required");
        }

        if(carton == null){
            throw new IllegalArgumentException("Carton is required");
        }

        if(location == null){
            throw new IllegalArgumentException("Location is required");
        }

        if(carton.getVerifiedProducts().isEmpty()){
            log.error("Verified products is empty");
            throw new IllegalArgumentException("VePacked Products is required");
        }

        return carton.getVerifiedProducts().stream()
            .map(cartonItem -> new PackingSlipProduct(cartonItem.getUnitNumber(), cartonItem.getCollectionDate() , cartonItem.getVolume()
                , getSystemPropertyByKey(systemProcessProperties,"DATE_FORMAT"), getTimeZone(location)))
            .collect(Collectors.toList());
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

    private static RecoveredPlasmaShipment getShipment(Long shipmentId , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository) {
        if (recoveredPlasmaShippingRepository == null) {
            throw new IllegalArgumentException("RecoveredPlasmaShippingRepository is required");
        }

        return recoveredPlasmaShippingRepository.findOneById(shipmentId)
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("Shipment is required")))
            .block();
    }

    private static Location getLocation(String locationCode, LocationRepository locationRepository) {
        if (locationRepository == null) {
            throw new IllegalArgumentException("LocationRepository is required");
        }

        return locationRepository.findOneByCode(locationCode)
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("Location is required")))
            .block();
    }

    private static PackingSlipShipment buildPackSlipShipment(RecoveredPlasmaShipment shipment , RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository) {
        if(shipment == null){
            throw new IllegalArgumentException("Shipment is required");
        }

        if(recoveredPlasmaShipmentCriteriaRepository == null){
            throw new IllegalArgumentException("RecoveredPlasmaShipmentCriteriaRepository is required");
        }

        var productType = recoveredPlasmaShipmentCriteriaRepository.findProductTypeByProductCode(shipment.getProductType())
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("Product Type is required")))
            .block();

        if(productType == null){
            throw new IllegalArgumentException("Product Type is required");
        }

        return new PackingSlipShipment(shipment.getId(), shipment.getShipmentNumber(), shipment.getProductType()
            , productType.getProductTypeDescription(), shipment.getTransportationReferenceNumber());
    }

    private static PackingSlipShipFrom buildShipFrom(List<SystemProcessProperty> systemProcessProperties,Location location) {
        if(location == null){
            throw new IllegalArgumentException("Location is required");
        }

        if(systemProcessProperties == null || systemProcessProperties.isEmpty()){
            throw new IllegalArgumentException("System Property is required");
        }

        return new PackingSlipShipFrom(getSystemPropertyByKey(systemProcessProperties,"BLOOD_CENTER_NAME") , location, getSystemPropertyByKey(systemProcessProperties,"ADDRESS_FORMAT"));
    }

    private static PackingSlipShipTo buildShipTo(RecoveredPlasmaShipment shipment , List<SystemProcessProperty> systemProcessProperties) {
        if(shipment == null){
            throw new IllegalArgumentException("Shipment is required");
        }

        return new PackingSlipShipTo(shipment.getShipmentCustomer(), getSystemPropertyByKey(systemProcessProperties,"ADDRESS_FORMAT") );
    }

   private static String getTimeZone(Location location){
       if(location == null){
           throw new IllegalArgumentException("Location is required");
       }

       var timeZone = location.findProperty("TZ");
       if(timeZone.isEmpty()){
           log.error("Location Timezone is missing {}", location);
           throw new IllegalArgumentException("Timezone is required");
       }

       return timeZone.get().getPropertyValue();
   }

    private static String formatDateTime(ZonedDateTime zonedDateTime ,  List<SystemProcessProperty> systemProcessProperties , Location location){

        if(zonedDateTime == null){
            throw new IllegalArgumentException("Date Time is required");
        }

        if(systemProcessProperties == null || systemProcessProperties.isEmpty()){
            throw new IllegalArgumentException("System Property is required");
        }

        if(location == null){
            throw new IllegalArgumentException("Location is required");
        }

        try{
            return DateTimeFormatter.ofPattern(getSystemPropertyByKey(systemProcessProperties,"DATE_TIME_FORMAT")).withZone(ZoneId.of(getTimeZone(location))).format(zonedDateTime);
        }catch (Exception e){
            log.error("Not able to format date time {} {}", zonedDateTime , e.getMessage());
            throw  new IllegalArgumentException("Date Time is required");
        }

    }

    private static String buildTestingStatement(final String packedByEmployeeId , List<SystemProcessProperty> systemProcessProperties) {
        if(systemProcessProperties == null || systemProcessProperties.isEmpty()){
            throw new IllegalArgumentException("System Property is required");
        }

        if(YES_PROPERTY_VALUE.equals(getSystemPropertyByKey(systemProcessProperties,"USE_TESTING_STATEMENT"))){
            return getSystemPropertyByKey(systemProcessProperties,"TESTING_STATEMENT_TXT").replace("{employeeName}",packedByEmployeeId);
        }else{
            return null;
        }
    }

}
