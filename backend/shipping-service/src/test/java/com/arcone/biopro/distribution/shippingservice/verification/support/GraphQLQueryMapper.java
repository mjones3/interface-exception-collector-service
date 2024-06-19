package com.arcone.biopro.distribution.shippingservice.verification.support;

public class GraphQLQueryMapper {
    public static String printPackingListQuery() {
        return (
            """
                query GeneratePackingListLabel {
                    generatePackingListLabel(shipmentId: "1") {
                        orderNumber
                        orderIdBase64Barcode
                        shipmentIdBase64Barcode
                        dateTimePacked
                        packedBy
                        enteredBy
                        quantity
                        distributionComments
                        shipTo {
                            customerCode
                            customerName
                            department
                            addressLine1
                            addressLine2
                            addressComplement
                        }
                        packedItems {
                            shipmentItemId
                            inventoryId
                            unitNumber
                            productCode
                            aboRh
                            productDescription
                            productFamily
                            expirationDate
                            collectionDate
                        }
                        shipmentId
                        shipFrom {
                            bloodCenterCode
                            bloodCenterName
                            bloodCenterAddressLine1
                            bloodCenterAddressLine2
                            bloodCenterAddressComplement
                        }
                    }
                }
                """
        );
    }

    public static String printShippingLabelQuery() {
        return """
            query GenerateShippingLabel {
                generateShippingLabel(shipmentId: "1") {
                    shipmentId
                    orderNumber
                    orderIdBase64Barcode
                    shipmentIdBase64Barcode
                    dateTimePacked
                    shipTo {
                        customerCode
                        customerName
                        department
                        addressLine1
                        addressLine2
                        addressComplement
                        phoneNumber
                    }
                    shipFrom {
                        bloodCenterCode
                        bloodCenterName
                        bloodCenterBase64Barcode
                        bloodCenterAddressLine1
                        bloodCenterAddressLine2
                        bloodCenterAddressComplement
                        phoneNumber
                    }
                }
            }

            """;
    }
}
