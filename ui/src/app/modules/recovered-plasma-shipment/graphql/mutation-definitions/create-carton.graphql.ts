import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';

export interface CreateCartonRequestDTO {
    shipmentId: number;
    employeeId: string;
}

export interface CartonDTO {
    id?: number;
    cartonNumber?: string;
    shipmentId?: number;
    cartonSequence?: number;
    createEmployeeId?: string;
    closeEmployeeId?: string;
    createDate?: string;
    modificationDate?: string;
    closeDate?: string;
    status?: string;
    totalProducts?: number;
    totalWeight?: number;
    totalVolume?: number;
}

export const CREATE_CARTON = gql<
    {
        createCarton: UseCaseResponseDTO<CartonDTO>;
    },
    CreateCartonRequestDTO
>`
    mutation CreateCarton($shipmentId: Int!, $employeeId: String!) {
        createCarton(
            createCartonRequest: {
                shipmentId: $shipmentId
                employeeId: $employeeId
            }
        ) {
            notifications {
                type
                message
                code
            }
            data
            _links
        }
    }
`;
