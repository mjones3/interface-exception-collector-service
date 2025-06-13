import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';
import { CartonDTO } from '../../models/recovered-plasma.dto';

export interface CreateCartonRequestDTO {
    shipmentId: number;
    employeeId: string;
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
