import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from 'app/shared/models/use-case-response.dto';
import { CartonDTO } from '../../models/recovered-plasma.dto';

export interface RemoveCartonDTO {
    cartonId: number;
    locationCode: string;
    employeeId: string;
}

export const REMOVE_CARTON = gql<
    {
        removeCarton: UseCaseResponseDTO<CartonDTO>;
    },
    RemoveCartonDTO
>`
    mutation removeCarton(
        $cartonId: Int!
        $locationCode: String!
        $employeeId: String!
        $comments: String!
    ) {
        removeCarton(
            removeCartonRequest: {
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
