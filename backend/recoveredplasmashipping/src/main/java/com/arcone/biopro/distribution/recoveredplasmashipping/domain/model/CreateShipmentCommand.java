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
public class CreateShipmentCommand implements Validatable {

    private String customerCode;
    private String locationCode;
    private String productType;
    private String createEmployeeId;
    private String transportationReferenceNumber;
    private LocalDate shipmentDate;
    private BigDecimal cartonTareWeight;

    public CreateShipmentCommand(String customerCode, String locationCode, String productType, String createEmployeeId
        , String transportationReferenceNumber, LocalDate shipmentDate, BigDecimal cartonTareWeight) {
        this.customerCode = customerCode;
        this.locationCode = locationCode;
        this.productType = productType;
        this.createEmployeeId = createEmployeeId;
        this.transportationReferenceNumber = transportationReferenceNumber;
        this.shipmentDate = shipmentDate;
        this.cartonTareWeight = cartonTareWeight;

        checkValid();
    }

    @Override
    public void checkValid() {

        if(customerCode == null || customerCode.isBlank()){
            throw new IllegalArgumentException("Customer code is required");
        }

        if(locationCode == null || locationCode.isBlank()){
            throw new IllegalArgumentException("Location code is required");
        }

        if(productType == null || productType.isBlank()){
            throw new IllegalArgumentException("Product type is required");
        }

        if(createEmployeeId == null || createEmployeeId.isBlank()){
            throw new IllegalArgumentException("Create employee ID is required");
        }

        if(shipmentDate != null && shipmentDate.isBefore(LocalDate.now())){
            throw new IllegalArgumentException("Shipment date must be in the future");
        }
    }
}
