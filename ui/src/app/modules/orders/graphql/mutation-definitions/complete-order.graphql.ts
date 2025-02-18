import { gql } from 'apollo-angular';
import { Notification } from '../../models/notification.dto';
import { OrderDetailsDTO } from '../../models/order-details.dto';

export interface CompleteOrderCommandDTO {
    orderId: number;
    employeeId: string;
    comments?: string;
    createBackOrder: boolean;
}

export const COMPLETE_ORDER = gql<
    {
        completeOrder: {
            notifications: Notification[];
            data: OrderDetailsDTO;
        };
    },
    CompleteOrderCommandDTO
>`
    mutation CompleteOrder(
        $orderId: ID!
        $employeeId: String!
        $comments: String
        $createBackOrder: Boolean!
    ) {
        completeOrder(
            completeOrderCommandDTO: {
                orderId: $orderId
                employeeId: $employeeId
                comments: $comments
                createBackOrder: $createBackOrder
            }
        ) {
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
                canBeCompleted
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
