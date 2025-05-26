package com.arcone.biopro.distribution.receiving.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class ProductConsequence implements Validatable {

    private Long id;
    private String productCategory;
    private boolean acceptable;
    private String resultProperty;
    private String resultType;
    private String resultValue;
    private String consequenceType;
    private String consequenceReason;

    public ProductConsequence(Long id, String productCategory, boolean acceptable, String resultProperty, String resultType
        , String resultValue, String consequenceType, String consequenceReason) {
        this.id = id;
        this.productCategory = productCategory;
        this.acceptable = acceptable;
        this.resultProperty = resultProperty;
        this.resultType = resultType;
        this.resultValue = resultValue;
        this.consequenceType = consequenceType;
        this.consequenceReason = consequenceReason;
    }

    @Override
    public void checkValid() {

        if (productCategory == null || productCategory.isBlank()) {
            throw new IllegalArgumentException("Product category is required");
        }
        if (resultProperty == null || resultProperty.isBlank()) {
            throw new IllegalArgumentException("Result property is required");
        }
        if (resultType == null || resultType.isBlank()) {
            throw new IllegalArgumentException("Result type is required");
        }
        if (resultValue == null || resultValue.isBlank()) {
            throw new IllegalArgumentException("Result value is required");
        }
        if (consequenceType == null || consequenceType.isBlank()) {
            throw new IllegalArgumentException("Consequence type is required");
        }
        if (consequenceReason == null || consequenceReason.isBlank()) {
            throw new IllegalArgumentException("Consequence reason is required");
        }

    }
}
