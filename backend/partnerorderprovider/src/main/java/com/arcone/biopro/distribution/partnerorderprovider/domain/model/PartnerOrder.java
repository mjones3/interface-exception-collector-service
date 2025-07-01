package com.arcone.biopro.distribution.partnerorderprovider.domain.model;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ToString
@Getter
public class PartnerOrder {
    private UUID id;
    private String externalId;
    private String orderStatus;
    private String locationCode;
    private String createDate;
    private String createEmployeeCode;
    private String shipmentType;
    private String deliveryType;
    private String shippingMethod;
    private String productCategory;
    private String desiredShippingDate;
    private String shippingCustomerCode;
    private String billingCustomerCode;
    private String comments;
    private PartnerOrderPickUpType partnerOrderPickUpType;
    private List<PartnerOrderItem> orderItems;
    private String labelStatus;
    private boolean quarantinedProducts;

    public PartnerOrder(UUID id ,String externalId, String orderStatus, String locationCode, String createDate
        , String createEmployeeCode, String shipmentType, String deliveryType, String shippingMethod
        , String productCategory, String desiredShippingDate, String shippingCustomerCode
        , String billingCustomerCode, String comments , PartnerOrderPickUpType partnerOrderPickUpType,String labelStatus, Boolean quarantinedProducts) {
        this.id = id;
        this.externalId = externalId;
        this.orderStatus = orderStatus;
        this.locationCode = locationCode;
        this.createDate = createDate;
        this.createEmployeeCode = createEmployeeCode;
        this.shipmentType = shipmentType;
        this.deliveryType = deliveryType;
        this.shippingMethod = shippingMethod;
        this.productCategory = productCategory;
        this.desiredShippingDate = desiredShippingDate;
        this.shippingCustomerCode = shippingCustomerCode;
        this.billingCustomerCode = billingCustomerCode;
        this.comments = comments;
        this.partnerOrderPickUpType = partnerOrderPickUpType;
        this.quarantinedProducts = quarantinedProducts != null && quarantinedProducts;
        this.labelStatus = labelStatus;

        this.validate();
    }

    private void validate(){

        if (this.id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        if (this.externalId == null || this.externalId.isBlank()) {
            throw new IllegalArgumentException("External ID cannot be null");
        }
        if (this.orderStatus == null || this.orderStatus.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (this.locationCode == null || this.locationCode.isBlank()) {
            throw new IllegalArgumentException("Location Code cannot be null");
        }
        if (this.shipmentType == null || this.shipmentType.isBlank()) {
            throw new IllegalArgumentException("Shipment Type cannot be null");
        }
        if (this.deliveryType == null || this.deliveryType.isBlank()) {
            throw new IllegalArgumentException("Delivery Type cannot be null");
        }
        if (this.shippingMethod == null || this.shippingMethod.isBlank()) {
            throw new IllegalArgumentException("Shipping Method cannot be null");
        }
        if (this.productCategory == null || this.productCategory.isBlank()) {
            throw new IllegalArgumentException("Product Category cannot be null");
        }
        if(shippingCustomerCode == null || shippingCustomerCode.isBlank()){
            throw new IllegalArgumentException("Shipping Customer code cannot be null");
        }

        if("INTERNAL_TRANSFER".equals(shipmentType) && (labelStatus == null || labelStatus.isBlank()) ){
            throw new IllegalArgumentException("Label Status cannot be null");
        }
    }

    public void addItem(PartnerOrderItem orderItem){
        if(Objects.isNull(this.orderItems)){
            this.orderItems = new ArrayList<>();
        }
        this.orderItems.add(orderItem);
    }
}
