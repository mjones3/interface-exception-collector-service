package com.arcone.biopro.distribution.partnerorderprovider.domain.model;

import java.util.Objects;

public class PartnerOrderItem {

    private String productFamily;
    private String bloodType;
    private Integer quantity;
    private String comments;

    public PartnerOrderItem(String productFamily, String bloodType, Integer quantity, String comments) {
        this.productFamily = Objects.requireNonNull(productFamily,"product family cannot be null");
        this.bloodType = Objects.requireNonNull(bloodType,"product family cannot be null");
        this.quantity = Objects.requireNonNull(quantity,"product family cannot be null");
        this.comments = comments;
    }

    public String getProductFamily() {
        return productFamily;
    }

    public String getBloodType() {
        return bloodType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getComments() {
        return comments;
    }
}
