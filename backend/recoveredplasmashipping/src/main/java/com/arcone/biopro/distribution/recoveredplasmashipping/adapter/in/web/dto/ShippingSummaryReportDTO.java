package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record ShippingSummaryReportDTO(

    String reportTitle,
    String employeeName,
    String employeeId,
    String shipDate,
    String closeDate,
    String shipmentDetailShipmentNumber,
    String shipmentDetailProductType,
    String shipmentDetailProductCode,
    int shipmentDetailTotalNumberOfCartons,
    int shipmentDetailTotalNumberOfProducts,
    String shipmentDetailTransportationReferenceNumber,
    boolean shipmentDetailDisplayTransportationNumber,
    String shipToAddress,
    String shipToCustomerName,
    String shipFromBloodCenterName,
    String shipFromLocationAddress,
    String testingStatement,
    boolean displayHeader,
    String headerStatement,
    List<ShippingSummaryCartonItemDTO> cartonList

) implements Serializable {

}
