package com.arcone.biopro.distribution.receiving.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.vo.ImportItemConsequence;
import com.arcone.biopro.distribution.receiving.domain.model.vo.VisualInspection;
import com.arcone.biopro.distribution.receiving.domain.repository.ImportRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import com.arcone.biopro.distribution.receiving.domain.service.ConfigurationService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Import implements Validatable {

    private final Long id;
    private final String temperatureCategory;
    private final LocalDateTime transitStartDateTime;
    private final String transitStartTimeZone;
    private final LocalDateTime transitEndDateTime;
    private final String transitEndTimeZone;
    private String totalTransitTime;
    private String transitTimeResult;
    private BigDecimal temperature;
    private final String thermometerCode;
    private String temperatureResult;
    private final String locationCode;
    private final String comments;
    private String status;
    private final String employeeId;
    private ZonedDateTime createDate;
    private ZonedDateTime modificationDate;
    private List<ImportItem> items;
    private int maxNumberOfProducts;
    private ZonedDateTime completeDate;
    private String completeEmployeeId;

    private static final String ACCEPTABLE_RESULT = "ACCEPTABLE";
    private static final String UNACCEPTABLE_RESULT = "UNACCEPTABLE";
    private static final String STATUS_COMPLETED = "COMPLETED";

    public static Import create(CreateImportCommand createImportCommand , ProductConsequenceRepository productConsequenceRepository) {
        if (createImportCommand == null) {
            throw new IllegalArgumentException("CreateImportCommand is required");
        }

        var importBuilder = Import.builder()
            .id(null)
            .temperatureCategory(createImportCommand.getTemperatureCategory())
            .transitStartDateTime(createImportCommand.getTransitStartDateTime())
            .transitStartTimeZone(createImportCommand.getTransitStartTimeZone())
            .transitEndDateTime(createImportCommand.getTransitEndDateTime())
            .transitEndTimeZone(createImportCommand.getTransitEndTimeZone())
            .thermometerCode(createImportCommand.getThermometerCode())
            .temperature(createImportCommand.getTemperature() != null ? createImportCommand.getTemperature().setScale(2, RoundingMode.HALF_UP) : null)
            .locationCode(createImportCommand.getLocationCode())
            .comments(createImportCommand.getComments())
            .status("PENDING")
            .employeeId(createImportCommand.getEmployeeId())
            .createDate(ZonedDateTime.now())
            .modificationDate(ZonedDateTime.now());

        defineTemperatureResult(importBuilder,createImportCommand.getTemperature(),createImportCommand.getTemperatureCategory(),productConsequenceRepository);
        defineTransitTimeResult(importBuilder,createImportCommand,productConsequenceRepository);

        var newImport = importBuilder.build();

        newImport.checkValid();

        return  newImport;
    }

    public static Import  fromRepository(Long id, String temperatureCategory, LocalDateTime transitStartDateTime, String transitStartTimeZone, LocalDateTime transitEndDateTime
        , String transitEndTimeZone, String totalTransitTime, String transitTimeResult, BigDecimal temperature, String thermometerCode, String temperatureResult, String locationCode
        , String comments, String status, String employeeId, ZonedDateTime createDate, ZonedDateTime modificationDate , List<ImportItem> importItems , int maxNumberOfProducts) {

        var existing =  Import.builder()
            .id(id)
            .temperatureCategory(temperatureCategory)
            .transitStartDateTime(transitStartDateTime)
            .transitStartTimeZone(transitStartTimeZone)
            .transitEndDateTime(transitEndDateTime)
            .transitEndTimeZone(transitEndTimeZone)
            .totalTransitTime(totalTransitTime)
            .transitTimeResult(transitTimeResult)
            .temperature(temperature)
            .thermometerCode(thermometerCode)
            .temperatureResult(temperatureResult)
            .locationCode(locationCode)
            .comments(comments)
            .status(status)
            .employeeId(employeeId)
            .createDate(createDate)
            .modificationDate(modificationDate)
            .items(importItems)
            .maxNumberOfProducts(maxNumberOfProducts)
            .build();

        existing.checkValid();

        return  existing;

    }

    @Override
    public void checkValid() {

        if (this.temperatureCategory == null || this.temperatureCategory.isBlank()) {
            throw new IllegalArgumentException("Temperature category is required");
        }

        if (this.locationCode == null || this.locationCode.isBlank()) {
            throw new IllegalArgumentException("Location code is required");
        }

        if (this.comments != null && this.comments.length() > 250) {
            throw new IllegalArgumentException("Comments length must be less than 250 characters");
        }

        if (this.employeeId == null || this.employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee id is required");
        }

        if (this.status == null || this.status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        if(this.transitStartDateTime != null ){

            if(transitStartTimeZone == null || transitStartTimeZone.isBlank()){
                throw new IllegalArgumentException("Transit start time zone is required");
            }
            if(transitEndDateTime == null){
                throw new IllegalArgumentException("Transit end date time is required");
            }
            if(transitEndTimeZone == null || transitEndTimeZone.isBlank()){
                throw new IllegalArgumentException("Transit end time zone is required");
            }
            if(transitTimeResult == null || transitTimeResult.isBlank()){
                throw new IllegalArgumentException("Transit time result is required");
            }
        }

        if(this.temperature != null){
            if(thermometerCode == null || thermometerCode.isBlank()){
                throw new IllegalArgumentException("Thermometer code is required");
            }
            if(temperatureResult == null || temperatureResult.isBlank()){
                throw new IllegalArgumentException("Temperature result is required");
            }
        }

    }

    public boolean isQuarantined(){
        return UNACCEPTABLE_RESULT.equals(this.temperatureResult) || UNACCEPTABLE_RESULT.equals(this.transitTimeResult) ;
    }

    private static void defineTemperatureResult(ImportBuilder importBuilder , BigDecimal temperature, String temperatureCategory , ProductConsequenceRepository productConsequenceRepository){
        if(temperature != null){
            var validationResult = TemperatureValidator.validateTemperature(new ValidateTemperatureCommand(temperature,temperatureCategory), getProductConsequenceByProperty("TEMPERATURE" , temperatureCategory, productConsequenceRepository));
            if(validationResult != null){
                importBuilder.temperatureResult(validationResult.valid() ? ACCEPTABLE_RESULT : UNACCEPTABLE_RESULT);
            }
        }
    }

    private static void defineTransitTimeResult(ImportBuilder importBuilder ,CreateImportCommand createImportCommand , ProductConsequenceRepository productConsequenceRepository){
        if(createImportCommand.getTransitStartDateTime() != null){
            var validationResult = TransitTimeValidator.validateTransitTime(new ValidateTransitTimeCommand(createImportCommand.getTemperatureCategory(), createImportCommand.getTransitStartDateTime(), createImportCommand.getTransitStartTimeZone()
                , createImportCommand.getTransitEndDateTime(), createImportCommand.getTransitEndTimeZone()), getProductConsequenceByProperty("TRANSIT_TIME" , createImportCommand.getTemperatureCategory(), productConsequenceRepository));

            if (validationResult != null) {
                var duration = Duration.parse(validationResult.result());
                if (duration.isNegative()) {
                    throw new IllegalArgumentException("Total transit time cannot be negative");
                }

                importBuilder.totalTransitTime(validationResult.resultDescription());
                importBuilder.transitTimeResult(validationResult.valid() ? ACCEPTABLE_RESULT : UNACCEPTABLE_RESULT);
            }
        }
    }


    private static List<ProductConsequence> getProductConsequenceByProperty(String property ,String temperatureCategory, ProductConsequenceRepository productConsequenceRepository){
        return productConsequenceRepository.findAllByProductCategoryAndResultProperty(temperatureCategory, property)
            .collectList()
            .block();
    }

    public ImportItem createImportItem(AddImportItemCommand addImportItemCommand , ConfigurationService configurationService , ProductConsequenceRepository productConsequenceRepository){
        if(addImportItemCommand == null){
            throw new IllegalArgumentException("AddImportItemCommand is required");
        }

        if(configurationService == null){
            throw new IllegalArgumentException("Configuration Service is required");
        }

        if(this.items == null){
            this.items = new ArrayList<>();
        }

        if(items.size() +1 > maxNumberOfProducts){
            throw new IllegalArgumentException("Max number of products reached");
        }

        if(STATUS_COMPLETED.equals(this.status)){
            throw new IllegalArgumentException("Import is completed");
        }


        validateFinNumber(addImportItemCommand.getUnitNumber(), configurationService);

        validateProductCode(addImportItemCommand.getProductCode(), configurationService);



        var importItem = ImportItem.create(addImportItemCommand,  configurationService, getItemConsequences(addImportItemCommand,productConsequenceRepository));



        this.items.add(importItem);

        return importItem;
    }

    private void validateFinNumber(String unitNumber , ConfigurationService configurationService){

        if(unitNumber == null || unitNumber.isBlank()){
            throw new IllegalArgumentException("Unit Number is required");
        }

        if(configurationService == null){
            throw new IllegalArgumentException("Configuration Service is required");
        }

        configurationService.findByFinNumber(unitNumber.substring(0,5))
            .switchIfEmpty(Mono.error(new IllegalArgumentException("FIN is not associated with a registered facility")))
            .block();
    }

    private void validateProductCode(String productCode, ConfigurationService configurationService){

        if(productCode == null || productCode.isBlank()){
            throw new IllegalArgumentException("Product Code is required");
        }

        if(configurationService == null){
            throw new IllegalArgumentException("Configuration Service is required");
        }

        configurationService.findByCodeAndTemperatureCategory(productCode,this.temperatureCategory)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Product type does not match")))
            .block();
    }

    private List<ImportItemConsequence> getItemConsequences(AddImportItemCommand addImportItemCommand,ProductConsequenceRepository productConsequenceRepository){

        if(addImportItemCommand == null){
            throw new IllegalArgumentException("AddImportItemCommand is required");
        }

        if(productConsequenceRepository == null){
            throw new IllegalArgumentException("ProductConsequenceRepository is required");
        }
        var consequences = new ArrayList<ImportItemConsequence>();

        if(addImportItemCommand.getVisualInspection() != null && VisualInspection.UNSATISFACTORY().equals(addImportItemCommand.getVisualInspection())){
            consequences.add(getItemConsequence(this.temperatureCategory,"VISUAL_INSPECTION",productConsequenceRepository));
        }
        if(UNACCEPTABLE_RESULT.equals(this.temperatureResult)){
            consequences.add(getItemConsequence(this.temperatureCategory,"TEMPERATURE",productConsequenceRepository));
        }
        if(UNACCEPTABLE_RESULT.equals(this.transitTimeResult)){
            consequences.add(getItemConsequence(this.temperatureCategory, "TRANSIT_TIME", productConsequenceRepository));
        }

        return consequences;

    }


    private ImportItemConsequence getItemConsequence(String temperatureCategory , String resultProperty, ProductConsequenceRepository productConsequenceRepository){


        if(productConsequenceRepository == null){
            throw new IllegalArgumentException("ProductConsequenceRepository is required");
        }

        return productConsequenceRepository.findAllByProductCategoryAndResultProperty(temperatureCategory, resultProperty)
            .filter(productConsequence -> !productConsequence.isAcceptable())
            .map(productConsequence -> ImportItemConsequence.builder()
                .consequenceType(productConsequence.getConsequenceType())
                .consequenceReason(productConsequence.getConsequenceReason())
                .build())
            .blockFirst();

    }

    public Import completeImport(String completeEmployeeId){
        if(completeEmployeeId == null || completeEmployeeId.isBlank()){
            throw new IllegalArgumentException("Complete Employee Id is required");
        }

        if(STATUS_COMPLETED.equals(this.status)){
            throw new IllegalArgumentException("Import is already completed");
        }

        if(this.items == null || this.items.isEmpty()){
            throw new IllegalArgumentException("Import must have at least one product in the batch");
        }

        this.status = STATUS_COMPLETED;
        this.completeEmployeeId = completeEmployeeId;
        this.modificationDate = ZonedDateTime.now();
        this.completeDate = ZonedDateTime.now();

        return this;
    }

    public boolean canComplete(){
        return "PENDING".equals(this.status) && this.items != null && !this.items.isEmpty();
    }

}



