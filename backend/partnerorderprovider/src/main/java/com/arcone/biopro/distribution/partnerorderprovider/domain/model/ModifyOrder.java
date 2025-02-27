package com.arcone.biopro.distribution.partnerorderprovider.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@EqualsAndHashCode
@ToString
@Getter
public class ModifyOrder {
    private UUID id;
    private String externalId;
    private String locationCode;
    private String modifyDate;
    private String modifyEmployeeCode;
    private String deliveryType;
    private String shippingMethod;
    private String productCategory;
    private String desiredShippingDate;
    private String comments;
    private PartnerOrderPickUpType partnerOrderPickUpType;
    private List<PartnerOrderItem> orderItems;

    public ModifyOrder(UUID id , String externalId, String locationCode , String modifyDate, String modifyEmployeeCode, String deliveryType, String shippingMethod
        , String productCategory, String desiredShippingDate, String comments , PartnerOrderPickUpType partnerOrderPickUpType) {
        this.id = Objects.requireNonNull(id,"ID cannot be null");
        this.externalId = Objects.requireNonNull(externalId,"External ID cannot be null");
        this.locationCode = Objects.requireNonNull(locationCode,"Location Code cannot be null");
        this.modifyDate = modifyDate;
        this.modifyEmployeeCode = modifyEmployeeCode;
        this.deliveryType = Objects.requireNonNull(deliveryType,"Delivery Type cannot be null");
        this.shippingMethod = Objects.requireNonNull(shippingMethod,"Shipping Method cannot be null");
        this.productCategory = Objects.requireNonNull(productCategory,"Product Category cannot be null");
        this.desiredShippingDate = desiredShippingDate;
        this.comments = comments;
        this.partnerOrderPickUpType = partnerOrderPickUpType;
    }

    public void addItem(PartnerOrderItem orderItem){
        if(Objects.isNull(this.orderItems)){
            this.orderItems = new ArrayList<>();
        }
        this.orderItems.add(orderItem);
    }

}
