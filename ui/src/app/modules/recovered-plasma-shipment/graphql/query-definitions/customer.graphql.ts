import { gql } from 'apollo-angular';

export interface RecoveredPlasmaCustomerDTO {
    id: string;
    externalId?: string;
    customerType?: string;
    name?: string;
    code?: string;
    departmentCode?: string;
    departmentName?: string;
    foreignFlag?: string;
    phoneNumber?: string;
    contactName?: string;
    state?: string;
    postalCode?: string;
    country?: string;
    countryCode?: string;
    city?: string;
    district?: string;
    addressLine1?: string;
    addressLine2?: string;
    active?: boolean;
    createDate?: string;
    modificationDate?: string;
}

export const FIND_ALL_CUSTOMERS = gql<
    {
        findAllCustomers: RecoveredPlasmaCustomerDTO[];
    },
    never
>`
    query FindAllCustomers {
        findAllCustomers {
            id
            externalId
            customerType
            name
            code
            departmentCode
            departmentName
            foreignFlag
            phoneNumber
            contactName
            state
            postalCode
            country
            countryCode
            city
            district
            addressLine1
            addressLine2
            active
            createDate
            modificationDate
        }
    }
`;
