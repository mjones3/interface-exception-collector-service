package com.arcone.biopro.distribution.order.verification.support.graphql;

public class GraphQLMutationMapper {

    public static String completeOrderMutation(Integer orderId, String employeeId, String comments, boolean createBackOrder) {
        return (String.format("""
            mutation {
                completeOrder(
            completeOrderCommandDTO: {
                orderId: %s
                employeeId: "%s"
                comments: "%s"
                createBackOrder: %s
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
    """, orderId, employeeId, comments, createBackOrder));
    }

    public static String generatePickListMutation(Integer orderId, boolean skipServiceUnavailable) {
        return (String.format("""
            mutation {
                generatePickList(orderId: %s , skipInventoryUnavailable: %s)
                {
            notifications {
                name
                notificationType
                notificationMessage
            }
            data {
                orderNumber
                orderComments
                shipmentType
                quarantinedProducts
                labelStatus
                customer {
                    code
                    name
                }
                pickListItems {
                    productFamily
                    bloodType
                    quantity
                    comments
                    shortDateList {
                        unitNumber
                        productCode
                        aboRh
                        storageLocation
                    }
                }
            }
       }
     }
    """, orderId, skipServiceUnavailable));
    }

}
