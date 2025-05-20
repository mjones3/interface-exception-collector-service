import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from 'app/shared/models/use-case-response.dto';
import { CartonDTO } from '../../models/recovered-plasma.dto';

export interface RepackCartonDTO {
    cartonId: number;
    locationCode: string;
    employeeId: string;
    comments: string;
}

export const REPACK_CARTON = gql<
    {
        repackCarton: UseCaseResponseDTO<CartonDTO>;
    },
    RepackCartonDTO
>`
    mutation repackCarton(
        $cartonId: Int!
        $locationCode: String!
        $employeeId: String!
        $comments: String!
    ) {
        repackCarton(
            repackCartonRequest: {
                cartonId: $cartonId
                locationCode: $locationCode
                employeeId: $employeeId
                comments: $comments
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
