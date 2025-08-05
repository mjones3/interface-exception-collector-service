import { gql } from 'apollo-angular';
import { RuleResponseDTO } from '../../../../shared/models/rule.model';

export interface ProductResponseDTO {
    inventoryId: string;
    unitNumber: string;
    productCode: string;
    aboRh: string;
    productDescription: string;
    productFamily: string;
    status: string;
    isLabeled: boolean;
    isLicensed: boolean;
}

export interface GetUnlabeledProductsRequestDTO {
    shipmentItemId: number;
    unitNumber: string;
    locationCode: string;
}

export const GET_UNLABELED_PRODUCTS = gql<
    { getUnlabeledProducts: RuleResponseDTO<{ results: [ ProductResponseDTO[] ] }> },
    { getUnlabeledProductsRequest: GetUnlabeledProductsRequestDTO }
>`
    query (
        $getUnlabeledProductsRequest: GetUnlabeledProductsRequestDTO!
    ) {
        getUnlabeledProducts(
            getUnlabeledProductsRequest: $getUnlabeledProductsRequest
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
