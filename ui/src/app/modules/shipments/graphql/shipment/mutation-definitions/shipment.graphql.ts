import { gql } from 'apollo-angular';
import { RuleResponseDTO } from '../../../../../shared/models/rule.model';
import { VerifyProductDTO } from '../../../models/shipment-info.dto';

const PACK_ITEM = gql<{ packItem: RuleResponseDTO }, VerifyProductDTO>`
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
            notifications {
                statusCode
                notificationType
                name
                action
                reason
                message
            }
            _links
            results
        }
    }
`;

const COMPLETE_SHIPMENT = gql<
    { completeShipment: RuleResponseDTO },
    { shipmentId: number; employeeId: string }
>`
    mutation completeShipment($shipmentId: Int!, $employeeId: String!) {
        completeShipment(
            completeShipmentRequest: {
                shipmentId: $shipmentId
                employeeId: $employeeId
            }
        ) {
            ruleCode
            notifications {
                statusCode
                notificationType
                message
            }
            _links
            results
        }
    }
`;

export { COMPLETE_SHIPMENT, PACK_ITEM };
