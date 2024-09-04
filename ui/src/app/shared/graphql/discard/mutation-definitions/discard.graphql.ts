import { gql } from 'apollo-angular';

const ADD_DISCARD = gql`
    mutation discardProduct(
        $unitNumber: String!
        $productCode: String!
        $reasonDescriptionKey: String!
        $locationId: String!
        $comments: String
        $employeeId: String!
        $triggeredBy: TriggeredByProcess!
        $productShortDescription: String!
        $productFamily: String!
    ) {
        discardProduct(
            discardRequest: {
                productCode: $productCode
                locationId: $locationId
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
        }
    }
`;



export { ADD_DISCARD};
