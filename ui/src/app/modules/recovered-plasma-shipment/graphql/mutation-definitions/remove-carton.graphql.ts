import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from 'app/shared/models/use-case-response.dto';
import { ShipmentResponseDTO } from 'app/modules/shipments/models/shipment-info.dto';

export interface RemoveCartonDTO {
    cartonId: number;
    employeeId: string;
}

export const REMOVE_CARTON = gql<
    {
        removeCarton: UseCaseResponseDTO<ShipmentResponseDTO>;
    },
    RemoveCartonDTO
>`
    mutation removeCarton(
        $cartonId: Int!
        $employeeId: String!
    ) {
        removeCarton(
            removeCartonRequest: {
                cartonId: $cartonId
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
