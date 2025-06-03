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

    public static String findAllCustomers() {
        return (String.format("""
            query {
                findAllCustomers {
                    externalId
                    customerType
                    name
                    code
                    departmentCode
                    departmentName
                    foreignFlag
                    phoneNumber
                    contactName
                    state
                    postalCode
                    country
                    countryCode
                    city
                    district
                    addressLine1
                    addressLine2
                    active
                    createDate
                    modificationDate
                }
            }
            """));
    }

    public static String findAllProductTypeCustomer(String customerCode) {
        return String.format("""
            query {
                findAllProductTypeByCustomer(customerCode:"%s") {
                    id
                   productType
                   productTypeDescription
                }
            }
            """, customerCode);
    }

    public static String searchShipment(
        String locationCodeList,
        String shipmentNumber,
        String shipmentStatusList,
        String customerList,
        String productTypeList,
        String shipmentDateFrom,
        String shipmentDateTo,
        String transportationReferenceNumber) {
        return String.format("""
            query SearchShipment {
                searchShipment(
                    recoveredPlasmaShipmentQueryCommandRequestDTO: {
                        locationCode: %s
                        shipmentNumber: %s
                        shipmentStatus: %s
                        customers: %s
                        productTypes: %s
                        shipmentDateFrom: %s
                        shipmentDateTo: %s
                        transportationReferenceNumber: %s
                    }
                ) {
                    _links
                    data
                    notifications {
                        message
                        type
                        code
                    }
                }
            }

            """, locationCodeList, shipmentNumber, shipmentStatusList, customerList, productTypeList, shipmentDateFrom, shipmentDateTo, transportationReferenceNumber);
    }

    public static String findShipmentByIdAndLocation(String shipmentId, String locationCode) {
        return String.format("""
            query FindShipmentById {
                 findShipmentById(
                     findShipmentCommandDTO: {
                     shipmentId: %s,
                     locationCode: "%s",
                     employeeId: "4c973896-5761-41fc-8217-07c5d13a004b" }
                 ) {
                     _links
                     data
                     notifications {
                         message
                         type
                         code
                     }
                 }
             }
            """, shipmentId, locationCode);
    }

    public static String generateCartonPackingSlip(int cartonId,String employeeId ,String locationCode) {
        return String.format("""
                query{
                    generateCartonPackingSlip(generateCartonPackingSlipRequest:{
                    cartonId:%s
                    locationCode:"%s"
                    employeeId:"%s"
                   }){
                    data
                    notifications{
                        message
                        type
                        code
                        reason
                        action
                        details
                     }
                    _links
                    }
               }
               """, cartonId, locationCode, employeeId);
    }
    public static String printUnacceptalbleUnitsReport(int shipmentId,String employeeId ,String locationCode) {
        return String.format("""
                query{
                    printUnacceptableUnitsReport(printUnacceptableUnitReportRequest:{
                    shipmentId:%s
                    locationCode:"%s"
                    employeeId:"%s"
                   }){
                    data
                    notifications{
                        message
                        type
                        code
                        reason
                        action
                        details
                     }
                    _links
                    }
               }
               """, shipmentId, locationCode, employeeId);
    }

    public static String findCartonById(int cartonId) {
        return String.format("""
                query{
                    findCartonById(cartonId:%s){
                    data
                    notifications{
                        message
                        type
                        code
                        reason
                        action
                        details
                     }
                    _links
                    }
               }
               """, cartonId);
    }

    public static String printShippingSummaryReport(int shipmentId,String employeeId ,String locationCode) {
        return String.format("""
                query{
                    printShippingSummaryReport(printShippingSummaryReportRequest:{
                    shipmentId:%s
                    locationCode:"%s"
                    employeeId:"%s"
                   }){
                    data
                    notifications{
                        message
                        type
                        code
                        reason
                        action
                        details
                     }
                    _links
                    }
               }
               """, shipmentId, locationCode, employeeId);
    }

    public static String requestShipmentModifyHistory(int id) {
        return String.format("""
                query FindAllShipmentHistoryByShipmentId {
                 findAllShipmentHistoryByShipmentId(shipmentId: %s) {
                     id
                     shipmentId
                     comments
                     createEmployeeId
                     createDate
                 }
             }
             """, id);
    }
}
