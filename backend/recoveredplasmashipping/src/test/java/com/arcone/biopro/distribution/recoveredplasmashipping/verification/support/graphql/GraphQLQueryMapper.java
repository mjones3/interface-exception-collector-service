package com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.graphql;

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

    public static String listOrdersByLocation(String locationCode) {
        return String.format("""
            query {
                searchOrders(
                    orderQueryCommandDTO: {
                        locationCode:"%s"
                    }
                ) {
                    content
                    pageNumber
                    pageSize
                    totalRecords
                    hasPrevious
                    hasNext
                    isFirst
                    isLast
                    totalPages
                    querySort {
                        orderByList {
                            property
                            direction
                        }
                    }
                }
            }
            """, locationCode);
    }

    public static String listOrdersByPage(String locationCode , Integer page) {
        var pageNumber = page != null ? page : 0;
        return String.format("""
            query {
                searchOrders(
                    orderQueryCommandDTO: {
                        locationCode:"%s",
                        pageNumber:%s,
                        pageSize:20
                    }
                ) {
                    content
                    pageNumber
                    pageSize
                    totalRecords
                    hasPrevious
                    hasNext
                    isFirst
                    isLast
                    totalPages
                    querySort {
                        orderByList {
                            property
                            direction
                        }
                    }
                }
            }
            """, locationCode , pageNumber);
    }

    public static String sortOrdersByPage(String locationCode , Integer page , String sortingColumn , String sortingOrder) {
        var pageNumber = page != null ? page : 0;
        var sortOrder = sortingOrder != null ? sortingOrder : "ASC";
        return String.format("""
            query {
                searchOrders(
                    orderQueryCommandDTO: {
                        locationCode:"%s",
                        pageNumber:%s,
                        querySort:{
                           orderByList:[
                            {
                                property:"%s",
                                direction:"%s"
                            }
                            ]
                        }
                    }
                ) {
                    content
                    pageNumber
                    pageSize
                    totalRecords
                    hasPrevious
                    hasNext
                    isFirst
                    isLast
                    totalPages
                    querySort {
                        orderByList {
                            property
                            direction
                        }
                    }
                }
            }
            """, locationCode , pageNumber, sortingColumn , sortOrder);
    }

    public static String listOrdersByUniqueIdentifier(String locationCode, String externalId) {
        return String.format("""
            query {
                searchOrders(
                    orderQueryCommandDTO: {
                        locationCode:"%s",
                        orderUniqueIdentifier:"%s"
                    }
                ) {
                    content
                    pageNumber
                    pageSize
                    totalRecords
                    hasPrevious
                    hasNext
                    isFirst
                    isLast
                    totalPages
                    querySort {
                        orderByList {
                            property
                            direction
                        }
                    }
                }
            }
            """, locationCode, externalId);
    }
    public static String searchOrdersByCreateDate(String locationCode, String createDateFrom, String createDateTo) {
        return String.format("""
            query {
                searchOrders(
                    orderQueryCommandDTO: {
                        locationCode:"%s",
                        createDateFrom:"%s",
                        createDateTo: "%s"
                    }
                ) {
                    content
                    pageNumber
                    pageSize
                    totalRecords
                    hasPrevious
                    hasNext
                    isFirst
                    isLast
                    totalPages
                    querySort {
                        orderByList {
                            property
                            direction
                        }
                    }
                }
            }
            """, locationCode, createDateFrom, createDateTo);
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
                           cancelEmployeeId
                           cancelDate
                           cancelReason
                           modifyEmployeeId
                           modifyReason
                           modifyByProcess
                           displayModificationDetails
                       }
                       notifications{
                           notificationType
                           notificationMessage
                       }
                   }
            }
            """, orderId);
    }

    public static String findAllLocations() {
        return (String.format("""
            query {
                findAllLocations {
                    id
                    name
                    code
                    externalId
                    addressLine1
                    addressLine2
                    postalCode
                    city
                    state
                    properties
                }
            }
            """));
    }
}
