import { gql } from 'apollo-angular';
import { RuleResponseDTO } from 'app/shared/models/rule.model';
import { CreateShipmentRequestDTO } from '../models/recovered-plasma.dto';

export interface CustomerOption {
    customerCode: string;
    customerName: string;
}

export interface ProductTypeOption {
    id: string;
    productType: string;
    productTypeDescription:string;
}


export const GET_PRODUCT_TYPE_OPTIONS = gql<ProductTypeOption, never>`
    query ($customerCode: String!) {
        findAllProductTypeByCustomer(customerCode: $customerCode) {
            id
            productType
            productTypeDescription
        }
    }
`;


export const CREATE_RECOVERED_PLASMA_SHIPMENT = gql<
    { createShipment: RuleResponseDTO },
    CreateShipmentRequestDTO
>`
    mutation createShipment(
        $customerCode: String!
        $locationCode: String!
        $productType: String!
        $cartonTareWeight: Float!
        $scheduleDate: Date!
        $transportationReferenceNumber: String
        $createEmployeeId: String!
    ) {
        createShipment(
            createShipmentRequest: {
                customerCode: $customerCode
                productType: $productType
                cartonTareWeight: $cartonTareWeight
                scheduleDate: $scheduleDate
                transportationReferenceNumber: $transportationReferenceNumber
                locationCode: $locationCode
                createEmployeeId: $createEmployeeId
            }
        ) {
            data
            notifications {
                code
                type
                message
            }
            _links
        }
    }
`;
