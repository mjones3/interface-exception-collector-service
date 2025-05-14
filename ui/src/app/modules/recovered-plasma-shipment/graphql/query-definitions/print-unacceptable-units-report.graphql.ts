import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';

export interface PrintUnacceptableUnitReportRequestDTO {
    shipmentId: number
    employeeId: string
    locationCode: string
}

export interface UnacceptableUnitReportItemOutput {
    cartonNumber: string
    cartonSequenceNumber: number
    unitNumber: string
    productCode: string
    failureReason: string
    createDate: string
}

export interface UnacceptableUnitReportOutput {
    shipmentNumber: string
    reportTitle: string
    dateTimeExported: string
    noProductsFlaggedMessage: string
    failedProducts: UnacceptableUnitReportItemOutput[]
}

export const PRINT_UNACCEPTABLE_UNITS_REPORT = gql<
    { printUnacceptableUnitsReport: UseCaseResponseDTO<UnacceptableUnitReportOutput> },
    PrintUnacceptableUnitReportRequestDTO
>`
    query PrintUnacceptableUnitsReport(
        $shipmentId: Int!,
        $employeeId: String!,
        $locationCode: String!
    ) {
        printUnacceptableUnitsReport(printUnacceptableUnitReportRequest: {
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
