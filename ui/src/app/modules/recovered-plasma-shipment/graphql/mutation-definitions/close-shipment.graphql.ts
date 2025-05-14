import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from 'app/shared/models/use-case-response.dto';
import { RecoveredPlasmaShipmentResponseDTO } from '../../models/recovered-plasma.dto';

export interface CloseShipmentRequestDTO {
    shipmentId: number;
    employeeId: string;
    locationCode: string;
    shipDate: string;
}

export const CLOSE_SHIPMENT = gql<
    {
        closeShipment: UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO>;
    },
    CloseShipmentRequestDTO
>`
    mutation closeShipment(
        $shipmentId: Int!
        $employeeId: String!
        $locationCode: String!
        $shipDate: Date!
    ) {
        closeShipment(
            closeShipmentRequest: {
                shipmentId: $shipmentId
                employeeId: $employeeId
                locationCode: $locationCode
                shipDate: $shipDate
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

