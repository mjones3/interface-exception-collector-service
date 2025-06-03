package com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.graphql;

import java.util.List;

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

    public static String closeShipment (String shipmentId, String employeeId, String locationCode , String shipDate) {
        return String.format("""
            mutation CloseShipment {
                closeShipment(
                    closeShipmentRequest: { shipmentId: %s, employeeId: "%s", locationCode: "%s" , shipDate:"%s" }
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
            """, shipmentId, employeeId, locationCode , shipDate);
    }

    public static String repackCarton (String cartonId, String employeeId, String locationCode , String comments) {

        if(comments == null){
            return String.format("""
            mutation RepackCarton {
                repackCarton(
                    repackCartonRequest: { cartonId: %s, employeeId: "%s", locationCode: "%s" , comments: null }
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
        }else{
            return String.format("""
            mutation RepackCarton {
                repackCarton(
                    repackCartonRequest: { cartonId: %s, employeeId: "%s", locationCode: "%s" , comments: "%s" }
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
            """, cartonId, employeeId, locationCode , comments);
        }
    }

    public static String removeCarton(int cartonId, String employeeId) {
        return String.format("""
            mutation RemoveCarton {
                removeCarton(removeCartonRequest: { cartonId: %s, employeeId: "%s" }) {
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
            """, cartonId, employeeId);
    }

    public static String removeCartonItems(int cartonId, String employeeId, List<Integer> cartonProductToRemoveIds) {
        return String.format("""
            mutation RemoveCartonItems {
                removeCartonItems(
                    removeCartonItemRequest: { cartonId: %s, employeeId: "%s", cartonItemIds: %s }
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
            """, cartonId, employeeId, cartonProductToRemoveIds);
    }

    public static String modifyShipment(int shipmentId, String customerCode, String productType, String transpRefNumber, String shipmentDate, int cartonTareWeight, String employeeId, String comments) {
        return (String.format("""
            mutation ModifyShipment {
                    modifyShipment(
                        modifyShipmentRequest: {
                            shipmentId: %s
                            customerCode: "%s"
                            productType: "%s"
                            transportationReferenceNumber: %s
                            shipmentDate: "%s"
                            cartonTareWeight: %s
                            modifyEmployeeId: "%s"
                            comments: %s
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
                            name
                        }
                    }
                }
        """, shipmentId, customerCode, productType, transpRefNumber, shipmentDate, cartonTareWeight, employeeId, comments));
    }
}
