import { gql } from 'apollo-angular';

export interface CustomerOption {
    customerCode: string;
    customerName: string;
}

export interface productTypeOptionResponse {
    findAllProductTypeByCustomer: ProductTypeOption[];
}

export interface ProductTypeOption {
    id: string;
    productType: string;
    productTypeDescription: string;
}

export const GET_PRODUCT_TYPE_OPTIONS = gql<
    { findAllProductTypeByCustomer: productTypeOptionResponse },
    { customerCode: string }
>`
    query ($customerCode: String!) {
        findAllProductTypeByCustomer(customerCode: $customerCode) {
            id
            productType
            productTypeDescription
        }
    }
`;
