import { gql } from 'apollo-angular';
import { OrderDetailsDTO } from '../../models/order-details.dto';

export interface Notification {
    name: string;
    notificationType: string;
    notificationMessage: string;
}
export const GET_ORDER_BY_ID = gql<
    {
        findOrderById: {
            notifications: Notification[];
            data: OrderDetailsDTO;
        };
    },
    { orderId: number }
>`
    query FindOrderById($orderId: ID!) {
        findOrderById(orderId: $orderId) {
            notifications {
                name
                notificationType
                notificationMessage
            }
            data {
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
                totalShipped
                totalRemaining
                totalProducts
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
                    quantityShipped
                    quantityRemaining
                }
            }
        }
    }
`;

export interface OrderShipmentDTO {
    id: number;
    orderId: number;
    shipmentId: number;
    shipmentStatus: string;
    createDate: string;
}
export const FIND_ORDER_SHIPMENT_BY_ORDER_ID = gql<
    { findOrderShipmentByOrderId: OrderShipmentDTO | null }, // NULL here means "order shipment not found"
    { orderId: number }
>`
    query FindOrderShipmentByOrderId($orderId: ID!) {
        findOrderShipmentByOrderId(orderId: $orderId) {
            id
            orderId
            shipmentId
            shipmentStatus
            createDate
        }
    }
`;
