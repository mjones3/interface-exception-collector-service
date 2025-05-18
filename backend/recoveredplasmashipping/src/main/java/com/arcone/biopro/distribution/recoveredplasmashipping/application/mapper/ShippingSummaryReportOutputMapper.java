package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ShippingSummaryCartonItemOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ShippingSummaryReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShippingSummaryReport;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShippingSummaryCartonItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShippingSummaryReportOutputMapper {


    @Mapping(source = "shippingSummaryReport.shipmentDetail.shipmentNumber", target = "shipmentDetailShipmentNumber")
    @Mapping(source = "shippingSummaryReport.shipmentDetail.productType", target = "shipmentDetailProductType")
    @Mapping(source = "shippingSummaryReport.shipmentDetail.productCode", target = "shipmentDetailProductCode")
    @Mapping(source = "shippingSummaryReport.shipmentDetail.totalNumberOfCartons", target = "shipmentDetailTotalNumberOfCartons")
    @Mapping(source = "shippingSummaryReport.shipmentDetail.totalNumberOfProducts", target = "shipmentDetailTotalNumberOfProducts")
    @Mapping(source = "shippingSummaryReport.shipmentDetail.transportationReferenceNumber", target = "shipmentDetailTransportationReferenceNumber")
    @Mapping(source = "shippingSummaryReport.shipmentDetail.displayTransportationNumber", target = "shipmentDetailDisplayTransportationNumber")
    @Mapping(source = "shippingSummaryReport.shipTo.formattedAddress", target = "shipToAddress")
    @Mapping(source = "shippingSummaryReport.shipTo.customerName", target = "shipToCustomerName")
    @Mapping(source = "shippingSummaryReport.shipFrom.bloodCenterName", target = "shipFromBloodCenterName")
    @Mapping(source = "shippingSummaryReport.shipFrom.locationAddress", target = "shipFromLocationAddress")
    @Mapping(source = "shippingSummaryReport.shipFrom.phoneNumber", target = "shipFromPhoneNumber")
    ShippingSummaryReportOutput toOutput(ShippingSummaryReport shippingSummaryReport);

    ShippingSummaryCartonItemOutput toOutput(ShippingSummaryCartonItem shippingSummaryCartonItem);


}
