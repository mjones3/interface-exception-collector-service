package com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.graphql;

public class GraphQLMutationMapper {

    public static String createShipment(String customerCode, String productType, Float cartonTareWeight, String shipmentDate, String TransportationRefNumber, String locationCode) {
        return (String.format("""
            mutation {
                createShipment(
            createShipmentRequest: {
                customerCode: %s
                productType: %s
                cartonTareWeight: %s
                shipmentDate: %s
                transportationReferenceNumber: %s
                locationCode: %s
                createEmployeeId: "4c973896-5761-41fc-8217-07c5d13a004b"
            }
        ) {
            data
            notifications {
                code
                type
                message
            }
            _links
        }
    }
    """, customerCode, productType, cartonTareWeight, shipmentDate, TransportationRefNumber, locationCode));
    }

    public static String createCarton(String shipmentId) {
        return (String.format("""
            mutation CreateCarton {
                        createCarton(createCartonRequest: { shipmentId: %s, employeeId: "4c973896-5761-41fc-8217-07c5d13a004b" }) {
                            _links
                            data
                            notifications {
                                message
                                type
                                code
                            }
                        }
                    }
    """, shipmentId));
    }

    public static String packCartonItem(int cartonId, String unitNumber, String productCode, String locationCode) {
        return String.format("""
            mutation PackCartonItem {
                packCartonItem(
                    packCartonItemRequest: {
                        cartonId: %s
                        unitNumber: "%s"
                        productCode: "%s"
                        employeeId: "4c973896-5761-41fc-8217-07c5d13a004b"
                        locationCode: "%s"
                    }
                ) {
                    _links
                    data
                    notifications {
                        message
                        type
                        code
                        action
                        reason
                        details
                    }
                }
            }
            """, cartonId, unitNumber, productCode, locationCode);
    }
    public static String verifyCarton(int cartonId, String unitNumber, String productCode, String locationCode) {
        return String.format("""
            mutation VerifyCarton {
                verifyCarton(
                    verifyCartonItemRequest: {
                        cartonId: %s
                        unitNumber: "%s"
                        productCode: "%s"
                        employeeId: "4c973896-5761-41fc-8217-07c5d13a004b"
                        locationCode: "%s"
                    }
                ) {
                    _links
                    data
                    notifications {
                        message
                        type
                        code
                        action
                        reason
                        details
                    }
                }
            }
            """, cartonId, unitNumber, productCode, locationCode);
    }

    public static String closeCarton (String cartonId, String employeeId, String locationCode) {
        return String.format("""
            mutation CloseCarton {
                closeCarton(
                    closeCartonRequest: { cartonId: %s, employeeId: "%s", locationCode: "%s" }
                ) {
                    _links
                    data
                    notifications {
                        message
                        type
                        code
                        action
                        reason
                        details
                        name
                    }
                }
            }
            """, cartonId, employeeId, locationCode);
    }
}
