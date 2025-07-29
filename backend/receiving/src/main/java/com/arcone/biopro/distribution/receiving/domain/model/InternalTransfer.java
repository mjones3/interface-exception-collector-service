package com.arcone.biopro.distribution.receiving.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class InternalTransfer implements Validatable {

    private Long id;
    private Long orderNumber;
    private String externalOrderId;
    private String temperatureCategory;
    private String locationCodeFrom;
    private String locationCodeTo;
    private String labelStatus;
    private boolean quarantinedProducts;
    private String createEmployeeId;
    private List<InternalTransferItem> internalTransferItems;

    public static InternalTransfer create(Long orderNumber, String externalOrderId, String temperatureCategory
        ,String locationCodeFrom, String locationCodeTo, String labelStatus, boolean quarantinedProducts, String createEmployeeId){
        var internalTransferBuilder = InternalTransfer.builder();

        var newInternalTransfer = internalTransferBuilder
            .orderNumber(orderNumber)
            .externalOrderId(externalOrderId)
            .temperatureCategory(temperatureCategory)
            .locationCodeFrom(locationCodeFrom)
            .locationCodeTo(locationCodeTo)
            .labelStatus(labelStatus)
            .quarantinedProducts(quarantinedProducts)
            .createEmployeeId(createEmployeeId)
            .build();

        newInternalTransfer.checkValid();

        return  newInternalTransfer;
    }

    public static InternalTransfer fromRepository(Long id,Long orderNumber, String externalOrderId, String temperatureCategory
        ,String locationCodeFrom, String locationCodeTo, String labelStatus, boolean quarantinedProducts, List<InternalTransferItem> internalTransferItems){

        var internalTransferBuilder = InternalTransfer
            .builder()
            .id(id)
            .orderNumber(orderNumber)
            .externalOrderId(externalOrderId)
            .temperatureCategory(temperatureCategory)
            .locationCodeFrom(locationCodeFrom)
            .locationCodeTo(locationCodeTo)
            .labelStatus(labelStatus)
            .quarantinedProducts(quarantinedProducts)
            .internalTransferItems(internalTransferItems)
            .build();

        internalTransferBuilder.checkValid();

        return  internalTransferBuilder;
    }

    public InternalTransferItem addItem(String unitNumber,String productCode,String productDescription){
        if(internalTransferItems == null){
            internalTransferItems = new ArrayList<>();
        }
        var item = new InternalTransferItem(unitNumber,productCode,productDescription);
        internalTransferItems.add(item);
        return item;
    }

    @Override
    public void checkValid() {
        if(orderNumber == null){
            throw new IllegalArgumentException("orderNumber is required");
        }

        if (temperatureCategory == null || temperatureCategory.isBlank()){
            throw new  IllegalArgumentException("temperatureCategory is required");
        }

        if(locationCodeFrom == null || locationCodeFrom.isBlank()){
            throw  new IllegalArgumentException("locationCodeFrom is required");
        }

        if(locationCodeTo == null || locationCodeTo.isBlank()){
            throw  new IllegalArgumentException("locationCodeTo is required");
        }
    }
}
