import { gql } from 'apollo-angular';
const PACK_ITEM = gql`
    mutation packItem(
        $shipmentItemId: Int!
        $locationCode: String!
        $unitNumber: String!
        $employeeId: String!
        $productCode: String!
        $visualInspection: VisualInspection!
    ) {
        packItem(
            packItemRequest: {
                shipmentItemId: $shipmentItemId
                locationCode: $locationCode
                unitNumber: $unitNumber
                employeeId: $employeeId
                productCode: $productCode
                visualInspection: $visualInspection
            }
        ) {
            ruleCode
            results
            notifications {
                statusCode
                notificationType
                message
            }
            _links
        }
    }
`;

const COMPLETE_SHIPMENT = gql`
    mutation completeShipment($shipmentId: Int!, $employeeId: String!) {
        completeShipment(
            completeShipmentRequest: {
                shipmentId: $shipmentId
                employeeId: $employeeId
            }
        ) {
            ruleCode
            results
            notifications {
                statusCode
                notificationType
                message
            }
            _links
        }
    }
`;

export { COMPLETE_SHIPMENT, PACK_ITEM };
