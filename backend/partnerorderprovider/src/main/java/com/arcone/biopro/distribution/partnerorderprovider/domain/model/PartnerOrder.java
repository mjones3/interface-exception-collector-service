package com.arcone.biopro.distribution.partnerorderprovider.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

    public PartnerOrder(UUID id ,String externalId, String orderStatus, String locationCode, String createDate
        , String createEmployeeCode, String shipmentType, String deliveryType, String shippingMethod
        , String productCategory, String desiredShippingDate, String shippingCustomerCode
        , String billingCustomerCode, String comments , PartnerOrderPickUpType partnerOrderPickUpType) {
        this.id = Objects.requireNonNull(id,"ID cannot be null");
        this.externalId = Objects.requireNonNull(externalId,"External ID cannot be null");
        this.orderStatus = Objects.requireNonNull(orderStatus,"Status cannot be null");
        this.locationCode = Objects.requireNonNull(locationCode,"Location Code cannot be null");
        this.createDate = Objects.requireNonNull(createDate,"Create Date cannot be null");
        this.createEmployeeCode = Objects.requireNonNull(createEmployeeCode,"Create Employee Code cannot be null");
        this.shipmentType = Objects.requireNonNull(shipmentType,"Shipment Type cannot be null");
        this.deliveryType = Objects.requireNonNull(deliveryType,"Delivery Type cannot be null");
        this.shippingMethod = Objects.requireNonNull(shippingMethod,"Shipping Method cannot be null");
        this.productCategory = Objects.requireNonNull(productCategory,"Product Category cannot be null");
        this.desiredShippingDate = Objects.requireNonNull(desiredShippingDate,"Desire Shipping Date cannot be null");
        this.shippingCustomerCode = Objects.requireNonNull(shippingCustomerCode,"Shipping Customer code cannot be null");
        this.billingCustomerCode = Objects.requireNonNull(billingCustomerCode,"Billing Customer Code cannot be null");
        this.comments = comments;
        this.partnerOrderPickUpType = partnerOrderPickUpType;
    }

    public void addItem(PartnerOrderItem orderItem){
        if(Objects.isNull(this.orderItems)){
            this.orderItems = new ArrayList<>();
        }
        this.orderItems.add(orderItem);
    }

    public UUID getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getCreateEmployeeCode() {
        return createEmployeeCode;
    }

    public String getShipmentType() {
        return shipmentType;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public String getDesiredShippingDate() {
        return desiredShippingDate;
    }

    public String getShippingCustomerCode() {
        return shippingCustomerCode;
    }

    public String getBillingCustomerCode() {
        return billingCustomerCode;
    }

    public String getComments() {
        return comments;
    }

    public PartnerOrderPickUpType getPartnerOrderPickUpType() {
        return partnerOrderPickUpType;
    }

    public List<PartnerOrderItem> getOrderItems() {
        return orderItems;
    }
}
