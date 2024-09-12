import { gql } from 'apollo-angular';
import {
    DiscardRequestDTO,
    DiscardResponseDTO,
} from '../../../models/discard.model';

const ADD_DISCARD = gql<
    { discardProduct: DiscardResponseDTO },
    { discardRequest: DiscardRequestDTO }
>`
    mutation discardProduct(
        $unitNumber: String!
        $productCode: String!
        $reasonDescriptionKey: String!
        $locationCode: String!
        $comments: String
        $employeeId: String!
        $triggeredBy: String!
        $productShortDescription: String!
        $productFamily: String!
    ) {
        discardProduct(
            discardRequest: {
                productCode: $productCode
                locationCode: $locationId
                unitNumber: $unitNumber
                reasonDescriptionKey: $reasonDescriptionKey
                employeeId: $employeeId
                comments: $comments
                triggeredBy: $triggeredBy
                productShortDescription: $productShortDescription
                productFamily: $productFamily
            }
        ) {
            id
            unitNumber
            productCode
            productShortDescription
            productFamily
            locationCode
            employeeId
            createDate
        }
    }
`;

export { ADD_DISCARD };
