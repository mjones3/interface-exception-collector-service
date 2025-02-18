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
                secondVerification
                verifiedByEmployeeId
                verifiedDate
                ineligibleStatus
                ineligibleReason
                ineligibleMessage
                ineligibleAction
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
                secondVerification
                verifiedByEmployeeId
                verifiedDate
                ineligibleStatus
                ineligibleReason
                ineligibleMessage
                ineligibleAction
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

export interface ShipmentItemRemovedDTO {
    id: number;
    shipmentId: number;
    unitNumber: string;
    productCode: string;
    productFamily: string;
    removedDate: string;
    removedByEmployeeId: string;
    ineligibleStatus: string;
}

export interface RemoveProductResponseDTO {
    shipmentId: number;
    removedItems: ShipmentItemRemovedDTO[];
    toBeRemovedItems: ShipmentItemPackedDTO[];
    removedItem: ShipmentItemPackedDTO;
}

export const GET_NOTIFICATION_DETAILS_BY_SHIPMENT_ID = gql<
    { getNotificationDetailsByShipmentId: RemoveProductResponseDTO },
    { shipmentId: number }
>`
    query GetNotificationDetailsByShipmentId($shipmentId: ID!) {
        getNotificationDetailsByShipmentId(shipmentId: $shipmentId) {
            shipmentId
            removedItems {
                id
                shipmentId
                unitNumber
                productCode
                productFamily
                removedByEmployeeId
                removedDate
                ineligibleStatus
            }
            toBeRemovedItems {
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
                secondVerification
                verifiedByEmployeeId
                verifiedDate
                ineligibleStatus
                ineligibleReason
                ineligibleMessage
                ineligibleAction
            }
            removedItem {
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
                secondVerification
                verifiedByEmployeeId
                verifiedDate
                ineligibleStatus
                ineligibleReason
                ineligibleMessage
                ineligibleAction
            }
        }
    }
`;

export interface RemoveItemRequest {
    shipmentId: number;
    unitNumber: string;
    productCode: string;
    employeeId: string;
}

export const REMOVE_ITEM = gql<
    { removeItem: RuleResponseDTO<{ results: RemoveProductResponseDTO[] }> },
    RemoveItemRequest
>`
    mutation RemoveItem(
        $shipmentId: Int!
        $unitNumber: String!
        $productCode: String!
        $employeeId: String!
    ) {
        removeItem(
            removeItemRequest: {
                shipmentId: $shipmentId
                unitNumber: $unitNumber
                productCode: $productCode
                employeeId: $employeeId
            }
        ) {
            ruleCode
            notifications {
                name
                statusCode
                notificationType
                code
                action
                reason
                message
            }
            _links
            results
        }
    }
`;

export interface CancelSecondVerificationRequest {
    shipmentId: number;
    employeeId: string;
}
export type ConfirmCancelSecondVerificationRequest =
    CancelSecondVerificationRequest;

export interface ShipmentDTO {
    id: number;
}

export const CANCEL_SECOND_VERIFICATION = gql<
    { cancelSecondVerification: RuleResponseDTO<{ results: never }> },
    CancelSecondVerificationRequest
>`
    mutation CancelSecondVerification($shipmentId: Int!, $employeeId: String!) {
        cancelSecondVerification(
            cancelSecondVerificationRequest: {
                shipmentId: $shipmentId
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

export const CONFIRM_CANCEL_SECOND_VERIFICATION = gql<
    { confirmCancelSecondVerification: RuleResponseDTO<{ results: never }> },
    ConfirmCancelSecondVerificationRequest
>`
    mutation ConfirmCancelSecondVerification(
        $shipmentId: Int!
        $employeeId: String!
    ) {
        confirmCancelSecondVerification(
            confirmCancelSecondVerificationRequest: {
                shipmentId: $shipmentId
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
