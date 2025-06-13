import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from 'app/shared/models/use-case-response.dto';
import { RecoveredPlasmaShipmentResponseDTO } from '../../models/recovered-plasma.dto';

export interface FindShipmentRequestDTO {
    locationCode: string;
    shipmentId: number;
    employeeId: string;
}

export const RECOVERED_PLASMA_SHIPMENT_DETAILS = gql<
    {
        findShipmentById: UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO>;
    },
    { findShipmentCommandDTO: FindShipmentRequestDTO }
>`
    query ($findShipmentCommandDTO: FindShipmentRequestDTO!) {
        findShipmentById(findShipmentCommandDTO: $findShipmentCommandDTO) {
            _links
            data
            notifications {
                message
                type
                code
            }
        }
    }
`;
