import { gql } from 'apollo-angular';
import { RuleResponseDTO } from '../../../../shared/models/rule.model';
import { ProductResponseDTO } from './get-unlabeled-products.graphql';

export interface GetUnlabeledPackedItemsRequestDTO {
    shipmentId: number;
    unitNumber: string;
}

export const GET_UNLABELED_PACKED_ITEMS = gql<
    { getUnlabeledPackedItems: RuleResponseDTO<{ results: [ ProductResponseDTO[] ] }> },
    { getUnlabeledPackedItemsRequest: GetUnlabeledPackedItemsRequestDTO }
>`
    query (
        $getUnlabeledPackedItemsRequest: GetUnlabeledPackedItemsRequestDTO!
    ) {
        getUnlabeledPackedItems(
            getUnlabeledPackedItemsRequest: $getUnlabeledPackedItemsRequest
        ) {
            ruleCode
            notifications {
                statusCode
                notificationType
                name
                action
                reason
                message
                details
            }
            _links
            results
        }
    }
`;
