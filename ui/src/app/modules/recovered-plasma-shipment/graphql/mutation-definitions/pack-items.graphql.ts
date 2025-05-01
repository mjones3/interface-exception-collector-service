import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from 'app/shared/models/use-case-response.dto';
import { CartonDTO } from '../../models/recovered-plasma.dto';

export interface PackCartonItemsDTO {
    cartonId: number;
    locationCode: string;
    unitNumber: string;
    employeeId: string;
    productCode: string;
}

export const CARTON_PACK_ITEM = gql<
    {
        packCartonItem: UseCaseResponseDTO<CartonDTO>;
    },
    PackCartonItemsDTO
>`
    mutation packCartonItem(
        $cartonId: Int!
        $locationCode: String!
        $unitNumber: String!
        $employeeId: String!
        $productCode: String!
    ) {
        packCartonItem(
            packCartonItemRequest: {
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
