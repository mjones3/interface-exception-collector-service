import { gql } from 'apollo-angular';

export interface RecoveredPlasmaLocationDTO {
    id: string;
    name: string;
    code: string;
    externalId?: string;
    addressLine1?: string;
    addressLine2?: string;
    postalCode?: string;
    city?: string;
    state?: string;
    properties?: Record<string, unknown>;
}

export const FIND_ALL_LOCATIONS = gql<
    {
        findAllLocations: RecoveredPlasmaLocationDTO[];
    },
    never
>`
    query FindAllLocations {
        findAllLocations {
            id
            name
            code
            externalId
            addressLine1
            addressLine2
            postalCode
            city
            state
            properties
        }
    }
`;
