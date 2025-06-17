package com.arcone.biopro.distribution.receiving.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.enumeration.ParseType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class ValidateBarcodeCommand implements Validatable {

    private final String barcodeValue;
    private final String barcodePattern;
    private final ParseType parseType;
    private final String temperatureCategory;

    public ValidateBarcodeCommand(String barcodeValue, String barcodePattern, String temperatureCategory) {
        this.barcodeValue = barcodeValue;
        this.barcodePattern = barcodePattern;
        this.parseType = ParseType.valueOf(barcodePattern);
        this.temperatureCategory = temperatureCategory;
    }

    @Override
    public void checkValid() {
        if (barcodeValue == null || barcodeValue.isBlank()) {
            throw new IllegalArgumentException("Barcode value is required");
        }

        if (barcodePattern == null || barcodePattern.isBlank()) {
            throw new IllegalArgumentException("Barcode pattern is required");
        }

        if(temperatureCategory == null || temperatureCategory.isBlank()){
            throw new IllegalArgumentException("Temperature Category is required");
        }

        if(parseType == null){
            throw new IllegalArgumentException("Barcode pattern is not valid");
        }
    }
}
