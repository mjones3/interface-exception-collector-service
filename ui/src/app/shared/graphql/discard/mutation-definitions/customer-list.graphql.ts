import { gql } from 'apollo-angular';

export const ALL_CUSTOMER_LIST_INFO = gql<
    { findAllCustomers: { code: string; name: string } },
    never
>`
    query {
        findAllCustomers {
            code
            name
        }
    }
`;
