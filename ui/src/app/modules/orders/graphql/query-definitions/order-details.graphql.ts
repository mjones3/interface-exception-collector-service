import { gql } from 'apollo-angular';
import { OrderDetailsDto } from '../../models/order-details.dto';

const GET_ORDER_BY_ID = gql<
    { findOrderById: OrderDetailsDto },
    { orderId: number }
>`
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
                quantityAvailable
            }
        }
    }
`;

export { GET_ORDER_BY_ID };
