package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class PackingSlipShipment implements Validatable {

    private Long shipmentId;
    private String shipmentNumber;
    private String productType;
    private String productDescription;
    private String transportationReferenceNumber;

    public PackingSlipShipment(Long shipmentId, String shipmentNumber, String productType, String productDescription, String transportationReferenceNumber) {
        this.shipmentId = shipmentId;
        this.shipmentNumber = shipmentNumber;
        this.productType = productType;
        this.productDescription = productDescription;
        this.transportationReferenceNumber = transportationReferenceNumber;
    }

    @Override
    public void checkValid() {

        if(shipmentId == null){
            throw new IllegalArgumentException("Shipment Id is required");
        }

        if(shipmentNumber == null || shipmentNumber.isBlank()){
            throw new IllegalArgumentException("Shipment Number is required");
        }

        if(productType == null || productType.isBlank()){
            throw new IllegalArgumentException("Product Type is required");
        }

        if(productDescription == null || productDescription.isBlank()){
            throw new IllegalArgumentException("Product Description is required");
        }

    }
}
