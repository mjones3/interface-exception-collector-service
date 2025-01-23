import { gql } from 'apollo-angular';
import { OrderDetailsDTO } from '../../models/order-details.dto';
import { Notification } from '../../models/notification.dto';

export interface CompleteOrderCommandDTO {
    orderId: number;
    employeeId: string;
    comments?: string;
}

export const COMPLETE_ORDER = gql<
    {
        completeOrder: {
            notifications: Notification[];
            data: OrderDetailsDTO;
        }
    },
    CompleteOrderCommandDTO
>`
    mutation CompleteOrder(
        $orderId: ID!
        $employeeId: String!
        $comments: String
    ) {
        completeOrder(
            completeOrderCommandDTO: {
                orderId: $orderId
                employeeId: $employeeId
                comments: $comments
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
