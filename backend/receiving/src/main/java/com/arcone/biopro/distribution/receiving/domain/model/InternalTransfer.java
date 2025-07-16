package com.arcone.biopro.distribution.receiving.domain.model;

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
public class InternalTransfer implements Validatable {

    private Long id;
    private String orderNumber;
    private String externalOrderId;
    private String temperatureCategory;
    private String locationCodeFrom;
    private String locationCodeTo;
    private String labelStatus;
    private Boolean quarantinedProducts;
    private List<InternalTransferItem> internalTransferItems;

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
        if(orderNumber == null || orderNumber.isBlank()){
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
