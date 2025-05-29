
import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from 'app/shared/models/use-case-response.dto';
import { CreateShipmentRequestDTO, RecoveredPlasmaShipmentResponseDTO } from '../../models/recovered-plasma.dto';

export const CREATE_RECOVERED_PLASMA_SHIPMENT = gql<
    { createShipment: UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO> },
    CreateShipmentRequestDTO
>`
    mutation createShipment(
        $customerCode: String!
        $locationCode: String!
        $productType: String!
        $cartonTareWeight: Float
        $shipmentDate: Date
        $transportationReferenceNumber: String
        $createEmployeeId: String!
    ) {
        createShipment(
            createShipmentRequest: {
                customerCode: $customerCode
                productType: $productType
                cartonTareWeight: $cartonTareWeight
                shipmentDate: $shipmentDate
                transportationReferenceNumber: $transportationReferenceNumber
                locationCode: $locationCode
                createEmployeeId: $createEmployeeId
            }
        ) {
            data
            notifications {
                code
                type
                message
            }
            _links
        }
    }
`;
