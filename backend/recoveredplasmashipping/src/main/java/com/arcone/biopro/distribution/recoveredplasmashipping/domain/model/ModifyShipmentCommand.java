package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class ModifyShipmentCommand implements Validatable {

    private Long shipmentId;
    private String customerCode;
    private String productType;
    private String modifyEmployeeId;
    private String transportationReferenceNumber;
    private LocalDate shipmentDate;
    private BigDecimal cartonTareWeight;
    private String comments;

    public ModifyShipmentCommand(Long shipmentId , String customerCode, String productType, String modifyEmployeeId
        , String transportationReferenceNumber, LocalDate shipmentDate, BigDecimal cartonTareWeight , String comments) {
        this.shipmentId = shipmentId;
        this.customerCode = customerCode;
        this.productType = productType;
        this.modifyEmployeeId = modifyEmployeeId;
        this.transportationReferenceNumber = transportationReferenceNumber;
        this.shipmentDate = shipmentDate;
        this.cartonTareWeight = cartonTareWeight;
        this.comments = comments;

        checkValid();
    }


    @Override
    public void checkValid() {

        if(shipmentId == null ){
            throw new IllegalArgumentException("Shipment is required");
        }
        if(customerCode == null || customerCode.isBlank()){
            throw new IllegalArgumentException("Customer code is required");
        }

        if(productType == null || productType.isBlank()){
            throw new IllegalArgumentException("Product type is required");
        }

        if(modifyEmployeeId == null || modifyEmployeeId.isBlank()){
            throw new IllegalArgumentException("ModifyEmployeeId employee ID is required");
        }

        if(shipmentDate != null && shipmentDate.isBefore(LocalDate.now())){
            throw new IllegalArgumentException("Shipment date must be in the future");
        }
        if(comments == null || comments.isBlank()){
            throw new IllegalArgumentException("Comments is required");
        }
        if(comments.length() > 250){
            throw new IllegalArgumentException("Comments cannot be greater than 250 chars.");
        }

    }
}
