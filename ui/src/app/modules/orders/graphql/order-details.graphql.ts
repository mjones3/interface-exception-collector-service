import { gql } from 'apollo-angular';

const GET_ORDER_BY_ID = gql`
    query findOrderById($orderId: ID!) {
        findOrderById(orderId: $orderId) {
            id
            orderNumber
            externalId
            locationCode
            shipmentType
            shippingMethod
            shippingCustomerName
            shippingCustomerCode
            billingCustomerName
            billingCustomerCode
            desiredShippingDate
            willCallPickup
            phoneNumber
            productCategory
            comments
            status
            priority
            createEmployeeId
            createDate
            modificationDate
            deleteDate
            orderItems {
                id
                orderId
                productFamily
                bloodType
                quantity
                comments
                createDate
                modificationDate
            }
        }
    }
`;

export { GET_ORDER_BY_ID };
