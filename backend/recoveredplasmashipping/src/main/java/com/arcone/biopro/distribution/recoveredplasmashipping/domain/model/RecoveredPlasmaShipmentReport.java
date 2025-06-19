package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class RecoveredPlasmaShipmentReport implements Validatable {

    private Long shipmentId;
    private String shipmentNumber;
    private String customerName;
    private String location;
    private String transportationReferenceNumber;
    private String productType;
    private LocalDate shipmentDate;
    private String status;

    public RecoveredPlasmaShipmentReport(Long shipmentId, String shipmentNumber, String customerName, String location
        , String transportationReferenceNumber, String productType, LocalDate shipmentDate, String status) {
        this.shipmentId = shipmentId;
        this.shipmentNumber = shipmentNumber;
        this.customerName = customerName;
        this.location = location;
        this.transportationReferenceNumber = transportationReferenceNumber;
        this.productType = productType;
        this.shipmentDate = shipmentDate;
        this.status = status;
    }

    @Override
    public void checkValid() {
        if (shipmentId == null) {
            throw new IllegalArgumentException("shipmentId is null");
        }

        if (shipmentNumber == null || shipmentNumber.isBlank()) {
            throw new IllegalArgumentException("customerName is null or blank");
        }

        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("customerName is null or blank");
        }

        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("location is null or blank");
        }

        if (productType == null || productType.isBlank()) {
            throw new IllegalArgumentException("Product type is null or blank");
        }

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is null or blank");
        }

        if (shipmentDate == null ) {
            throw new IllegalArgumentException("Shipment Date is null");
        }

    }
}


