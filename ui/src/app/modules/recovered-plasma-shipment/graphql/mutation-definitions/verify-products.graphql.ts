import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from 'app/shared/models/use-case-response.dto';
import { CartonDTO } from '../../models/recovered-plasma.dto';

export interface VerifyCartonItemsDTO {
    cartonId: number;
    locationCode: string;
    unitNumber: string;
    employeeId: string;
    productCode: string;
}

export const VERIFY_CARTON_PACK_ITEM = gql<
    {
        verifyCarton: UseCaseResponseDTO<CartonDTO>;
    },
    VerifyCartonItemsDTO
>`
    mutation verifyCarton(
        $cartonId: Int!
        $locationCode: String!
        $unitNumber: String!
        $employeeId: String!
        $productCode: String!
    ) {
        verifyCarton(
            verifyCartonItemRequest: {
                cartonId: $cartonId
                locationCode: $locationCode
                unitNumber: $unitNumber
                employeeId: $employeeId
                productCode: $productCode
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
