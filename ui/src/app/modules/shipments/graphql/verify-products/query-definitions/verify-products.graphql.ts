import { gql } from 'apollo-angular';
import { RuleResponseDTO } from '../../../../../shared/models/rule.model';
import { ShipmentItemPackedDTO } from '../../../models/shipment-info.dto';

export interface VerifyProductResponseDTO {
    shipmentId: number;
    packedItems: ShipmentItemPackedDTO[];
    verifiedItems: ShipmentItemPackedDTO[];
}

export const GET_SHIPMENT_VERIFICATION_DETAILS_BY_ID = gql<
    { getShipmentVerificationDetailsById: VerifyProductResponseDTO },
    { shipmentId: number }
>`
    query GetShipmentVerificationDetailsById($shipmentId: ID!) {
        getShipmentVerificationDetailsById(shipmentId: $shipmentId) {
            shipmentId
            packedItems {
                id
                shipmentItemId
                inventoryId
                unitNumber
                productCode
                aboRh
                productDescription
                productFamily
                expirationDate
                collectionDate
                packedByEmployeeId
                visualInspection
            }
            verifiedItems {
                id
                shipmentItemId
                inventoryId
                unitNumber
                productCode
                aboRh
                productDescription
                productFamily
                expirationDate
                collectionDate
                packedByEmployeeId
                visualInspection
            }
        }
    }
`;

export interface VerifyItemRequest {
    shipmentId: number;
    unitNumber: string;
    productCode: string;
    employeeId: string;
}

export const VERIFY_ITEM = gql<
    { verifyItem: RuleResponseDTO<{ results: VerifyProductResponseDTO[] }> },
    VerifyItemRequest
>`
    mutation VerifyItem(
        $shipmentId: Int!
        $unitNumber: String!
        $productCode: String!
        $employeeId: String!
    ) {
        verifyItem(
            verifyItemRequest: {
                shipmentId: $shipmentId
                unitNumber: $unitNumber
                productCode: $productCode
                employeeId: $employeeId
            }
        ) {
            ruleCode
            _links
            results
            notifications {
                name
                statusCode
                notificationType
                code
                action
                reason
                message
            }
        }
    }
`;
