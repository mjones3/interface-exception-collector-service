package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record CartonPackingSlipOutput(

    Long cartonId,
    String cartonNumber,
    Integer cartonSequence,
    int totalProducts,
    String dateTimePacked,
    String packedByEmployeeId,
    String testingStatement,
    String shipFromBloodCenterName,
    String shipFromLicenseNumber,
    String shipFromLocationAddress,

    String shipToAddress,
    String shipToCustomerName,

    String shipmentNumber,
    String shipmentProductType,
    String shipmentProductDescription,
    String shipmentTransportationReferenceNumber,

    boolean displaySignature,
    boolean displayTransportationReferenceNumber,
    boolean displayTestingStatement,
    boolean displayLicenceNumber,
    List<PackingSlipProductOutput> products

) implements Serializable {
}
