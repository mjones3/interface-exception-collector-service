package com.arcone.biopro.distribution.receiving.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.vo.Barcode;
import com.arcone.biopro.distribution.receiving.domain.model.vo.BloodCenterLocation;
import com.arcone.biopro.distribution.receiving.domain.repository.DeviceRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class CreateImportCommand {

    private final String temperatureCategory;
    private final LocalDateTime transitStartDateTime;
    private final String transitStartTimeZone;
    private final LocalDateTime transitEndDateTime;
    private final String transitEndTimeZone;
    private final BigDecimal temperature;
    private final String thermometerCode;
    private final String locationCode;
    private final String comments;
    private final String employeeId;

    public CreateImportCommand(String temperatureCategory, LocalDateTime transitStartDateTime, String transitStartTimeZone, LocalDateTime transitEndDateTime, String transitEndTimeZone
        , BigDecimal temperature, String thermometerCode, String locationCode, String comments, String employeeId , ProductConsequenceRepository productConsequenceRepository , DeviceRepository deviceRepository) {
        this.temperatureCategory = temperatureCategory;
        this.transitStartDateTime = transitStartDateTime;
        this.transitStartTimeZone = transitStartTimeZone;
        this.transitEndDateTime = transitEndDateTime;
        this.transitEndTimeZone = transitEndTimeZone;
        this.temperature = temperature;
        this.thermometerCode = thermometerCode;
        this.locationCode = locationCode;
        this.comments = comments;
        this.employeeId = employeeId;

        checkValid(productConsequenceRepository, deviceRepository);
    }

    public void checkValid(ProductConsequenceRepository productConsequenceRepository , DeviceRepository deviceRepository) {

        if (temperatureCategory == null || temperatureCategory.isBlank()) {
            throw new IllegalArgumentException("Temperature category is required");
        }

        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee id is required");
        }

        if(comments != null && comments.length() > 250) {
            throw new IllegalArgumentException("Comments length must be less than 250 characters");
        }

        if(locationCode == null || locationCode.isBlank()) {
            throw new IllegalArgumentException("Location code is required");
        }

        if(isTransitTimeRequired(temperatureCategory, productConsequenceRepository)){
            if(transitStartDateTime == null){
                throw new IllegalArgumentException("Transit start date time is required");
            }
            if(transitStartTimeZone == null || transitStartTimeZone.isBlank()){
                throw new IllegalArgumentException("Transit start time zone is required");
            }
            if(transitEndDateTime == null){
                throw new IllegalArgumentException("Transit end date time is required");
            }
            if(transitEndTimeZone == null || transitEndTimeZone.isBlank()){
                throw new IllegalArgumentException("Transit end time zone is required");
            }
        }

        if(isTemperatureRequired(temperatureCategory, productConsequenceRepository)){
            if(thermometerCode == null || thermometerCode.isBlank()){
                throw new IllegalArgumentException("Thermometer code is required");
            }

            if(temperature == null){
                throw new IllegalArgumentException("Temperature is required");
            }

            var device = deviceRepository.findFirstByBloodCenterIdAndLocationAndActiveIsTrue(new Barcode(thermometerCode) , new BloodCenterLocation(locationCode)).block();
            if(device == null){
                throw new IllegalArgumentException("Thermometer code is not valid");
            }
        }
    }

    private boolean isTemperatureRequired(String productCategory, ProductConsequenceRepository productConsequenceRepository){
        return isProductConsequenceConfigured(productCategory,"TEMPERATURE",productConsequenceRepository);
    }

    private boolean isTransitTimeRequired(String productCategory, ProductConsequenceRepository productConsequenceRepository){
        return isProductConsequenceConfigured(productCategory,"TRANSIT_TIME",productConsequenceRepository);
    }

    private boolean isProductConsequenceConfigured(String productCategory , String property , ProductConsequenceRepository productConsequenceRepository){
        if(productCategory == null || productCategory.isBlank()){
            throw new IllegalArgumentException("Product category is required");
        }
        if(productConsequenceRepository == null){
            throw new IllegalArgumentException("ProductConsequenceRepository is required");
        }

        var productConsequence = productConsequenceRepository.findAllByProductCategoryAndResultProperty(productCategory, property)
            .collectList()
            .block();

        return productConsequence != null && !productConsequence.isEmpty();
    }
}
