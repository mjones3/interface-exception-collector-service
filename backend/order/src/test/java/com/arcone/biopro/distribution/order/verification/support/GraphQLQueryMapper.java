package com.arcone.biopro.distribution.order.verification.support;

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

    public static String listOrders(String locationCode) {
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
}
