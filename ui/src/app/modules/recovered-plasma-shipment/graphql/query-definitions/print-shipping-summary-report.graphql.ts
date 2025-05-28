import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';

export interface PrintShippingSummaryReportRequestDTO {
    shipmentId: number;
    employeeId: string;
    locationCode: string;
}

export interface ShippingSummaryCartonItemDTO {
    cartonNumber: string;
    productCode: string;
    productDescription: string;
    totalProducts: number;
}

export interface ShippingSummaryReportDTO {
    reportTitle: string,
    employeeName: string,
    employeeId: string,
    closeDateTime: string,
    closeDate: string,
    shipmentDetailShipmentNumber: string;
    shipmentDetailProductType: string;
    shipmentDetailProductCode: string;
    shipmentDetailTotalNumberOfCartons: number;
    shipmentDetailTotalNumberOfProducts: number;
    shipmentDetailTransportationReferenceNumber: string;
    shipmentDetailDisplayTransportationNumber: boolean;
    shipToAddress: string;
    shipToCustomerName: string;
    shipFromBloodCenterName: string;
    shipFromLocationAddress: string;
    shipFromPhoneNumber: string;
    testingStatement: string;
    displayHeader: boolean,
    headerStatement: string,
    cartonList: ShippingSummaryCartonItemDTO[]
}

export const PRINT_SHIPPING_SUMMARY_REPORT = gql<
    { printShippingSummaryReport: UseCaseResponseDTO<ShippingSummaryReportDTO> },
    PrintShippingSummaryReportRequestDTO
>`
    query PrintShippingSummaryReport(
        $shipmentId: Int!,
        $employeeId: String!,
        $locationCode: String!
    ) {
        printShippingSummaryReport(printShippingSummaryReportRequest: {
            shipmentId: $shipmentId,
            employeeId: $employeeId,
            locationCode: $locationCode
        }) {
            _links
            data
            notifications {
                message
                type
                code
                action
                reason
                details
                name
            }
        }
    }
`;
