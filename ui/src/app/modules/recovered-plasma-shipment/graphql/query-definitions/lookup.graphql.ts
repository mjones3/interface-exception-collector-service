import { gql } from 'apollo-angular';
import { LookUpDto } from '@shared';

export const FIND_ALL_LOOKUPS_BY_TYPE = gql<
    {
        findAllLookupsByType: LookUpDto[];
    },
    { type: string }
>`
    query FindAllLookupsByType($type: String!) {
        findAllLookupsByType(type: $type) {
            id
            type
            optionValue
            descriptionKey
            orderNumber
            active
        }
    }
`;
