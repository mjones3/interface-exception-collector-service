package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record CartonPackingSlipDTO(
    Long cartonId,
    String cartonNumber,
    Integer cartonSequence,
    int totalProducts,
    String dateTimePacked,
    String packedByEmployeeId,
    String cartonProductCode,
    String cartonProductDescription,
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
    List<PackingSlipProductDTO> products
) implements Serializable {
}
