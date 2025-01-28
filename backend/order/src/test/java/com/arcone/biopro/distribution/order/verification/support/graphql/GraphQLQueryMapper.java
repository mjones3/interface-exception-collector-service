package com.arcone.biopro.distribution.order.verification.support.graphql;

public class GraphQLQueryMapper {
    public static String findCustomerByCode(String code) {
        return (String.format("""
            query {
                findCustomerByCode(code: "%s") {
                    externalId
                    name
                    code
                    departmentCode
                    departmentName
                    phoneNumber
                    active
                    addresses {
                        contactName
                        addressType
                        state
                        postalCode
                        countryCode
                        city
                        district
                        addressLine1
                        addressLine2
                        active
                    }
                }
            }
            """, code));
    }

    public static String listOrdersById(String locationCode) {
        return String.format("""
            query  {
              searchOrders(orderQueryCommandDTO:{
                locationCode:"%s"
              }) {
                orderId
                orderNumber
                externalId
                orderStatus
                createDate
                desireShipDate
                orderPriorityReport {
                  priority
                  priorityColor
                }
                orderCustomerReport {
                  code
                  name
                }
              }
            }
            """, locationCode);
    }

    public static String listOrdersByExternalId(String locationCode, String externalId) {
        return String.format("""
            query  {
              searchOrders(orderQueryCommandDTO:{
                locationCode:"%s",
                orderUniqueIdentifier:"%s",
                orderStatus: ["OPEN", "IN_PROGRESS", "COMPLETED"]
              }) {
                orderId
                orderNumber
                externalId
                orderStatus
                createDate
                desireShipDate
                orderPriorityReport {
                  priority
                  priorityColor
                }
                orderCustomerReport {
                  code
                  name
                }
              }
            }
            """, locationCode, externalId);
    }

    public static String findOrderById(Integer orderId) {
        return String.format("""
            query  {
              findOrderById(orderId:%s) {
                       data{
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
                           orderItems{
                                                     id
                                                     productFamily
                                                     bloodType
                                                     quantity
                                                 }
                           totalShipped
                           totalRemaining
                           totalProducts
                           canBeCompleted
                           completeEmployeeId
                           completeDate
                           completeComments
                           backOrderCreationActive
                       }
                       notifications{
                           notificationType
                           notificationMessage
                       }
                   }
            }
            """, orderId);
    }
}
