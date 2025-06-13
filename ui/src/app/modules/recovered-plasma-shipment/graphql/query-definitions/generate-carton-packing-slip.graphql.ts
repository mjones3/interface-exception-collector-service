import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';

export interface GenerateCartonPackingSlipRequestDTO {
    cartonId: number;
    employeeId: string;
    locationCode: string;
}

export interface PackingSlipProductDTO {
    unitNumber: string;
    volume: string;
    collectionDate: string;
}

export interface CartonPackingSlipDTO {
    cartonId: number;
    cartonNumber: string;
    cartonSequence: number;
    totalProducts: number;
    dateTimePacked: string;
    packedByEmployeeId: string;
    testingStatement: string;
    shipFromBloodCenterName: string;
    shipFromLicenseNumber: string;
    shipFromLocationAddress: string;
    shipToAddress: string;
    shipToCustomerName: string;
    shipmentNumber: string;
    shipmentProductType: string;
    shipmentProductDescription: string;
    cartonProductCode: string;
    cartonProductDescription: string;
    shipmentTransportationReferenceNumber: string;
    displaySignature: boolean;
    displayTransportationReferenceNumber: boolean;
    displayTestingStatement: boolean;
    displayLicenceNumber: boolean;
    products: PackingSlipProductDTO[];
}

export const GENERATE_CARTON_PACKING_SLIP = gql<
    {
        generateCartonPackingSlip: UseCaseResponseDTO<CartonPackingSlipDTO>;
    },
    GenerateCartonPackingSlipRequestDTO
>`
    query GenerateCartonPackingSlip($cartonId: Int!, $employeeId: String!, $locationCode: String!) {
        generateCartonPackingSlip(
            generateCartonPackingSlipRequest: {
                cartonId: $cartonId,
                employeeId: $employeeId,
                locationCode: $locationCode
            }
        ) {
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
