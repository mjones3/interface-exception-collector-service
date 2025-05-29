import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from 'app/shared/models/use-case-response.dto';
import { CartonDTO } from '../../models/recovered-plasma.dto';

export interface RemovePackedProductsDTO {
    cartonItemIds: number[];
    cartonId: number;
    employeeId: string;
}

export const REMOVE_CARTON_PRODUCTS = gql<
    {
        removeCartonItems: UseCaseResponseDTO<CartonDTO>;
    },
    RemovePackedProductsDTO
>`
    mutation removeCartonItems(
        $cartonId: Int!
        $employeeId: String!
        $cartonItemIds: [Int]!
    ) {
        removeCartonItems(
            removeCartonItemRequest: {
                cartonId: $cartonId
                employeeId: $employeeId
                cartonItemIds: $cartonItemIds
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
