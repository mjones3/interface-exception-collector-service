package com.arcone.biopro.distribution.order.verification.support.graphql;

public class GraphQLMutationMapper {

    public static String completeOrderMutation(Integer orderId, String employeeId, String comments) {
        return (String.format("""
            mutation {
                completeOrder(
            completeOrderCommandDTO: {
                orderId: %s
                employeeId: "%s"
                comments: "%s"
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
    """, orderId, employeeId, comments));
    }

}
