import { gql } from 'apollo-angular';
import { RuleResponseDTO } from 'app/shared/models/rule.model';

export interface UnpackItemRequest {
    shipmentItemId: number;
    employeeId: string;
    locationCode: string;
    unpackItems: UnpackItems[];
}

export interface UnpackItems {
    unitNumber: string;
    productCode: string;
}

const UNPACK_ITEM = gql<{ unpackItems: RuleResponseDTO }, UnpackItemRequest>`
    mutation unpackItems(
        $shipmentItemId: Int!
        $locationCode: String!
        $employeeId: String!
        $unpackItems: [UnpackItemRequest!]
    ) {
        unpackItems(
            unpackItemsRequest: {
                shipmentItemId: $shipmentItemId
                locationCode: $locationCode
                employeeId: $employeeId
                unpackItems: $unpackItems
            }
        ) {
            ruleCode
            _links
            results
            notifications {
                statusCode
                name
                notificationType
                message
            }
        }
    }
`;

export { UNPACK_ITEM };
