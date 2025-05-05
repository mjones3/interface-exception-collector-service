import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from 'app/shared/models/use-case-response.dto';
import { CartonDTO } from '../../models/recovered-plasma.dto';

export interface CloseCartonDTO {
    cartonId: number;
    locationCode: string;
    employeeId: string;
}

export const CLOSE_CARTON = gql<
    {
        closeCarton: UseCaseResponseDTO<CartonDTO>;
    },
    CloseCartonDTO
>`
    mutation closeCarton(
        $cartonId: Int!
        $locationCode: String!
        $employeeId: String!
    ) {
        closeCarton(
            closeCartonRequest: {
                cartonId: $cartonId
                locationCode: $locationCode
                employeeId: $employeeId
            }
        ) {
            notifications {
                message
                type
                code
                reason
                action
                details
                name
            }
            data
            _links
        }
    }
`;
