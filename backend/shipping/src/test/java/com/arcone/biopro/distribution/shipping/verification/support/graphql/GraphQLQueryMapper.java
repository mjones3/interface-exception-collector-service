package com.arcone.biopro.distribution.shipping.verification.support.graphql;

public class GraphQLQueryMapper {
    public static String printPackingListQuery(Long shipmentId) {
        return (String.format(
            """
                query GeneratePackingListLabel {
                    generatePackingListLabel(shipmentId: "%s") {
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
            , shipmentId));
    }

    public static String printShippingLabelQuery(long shipmentId){
        return String.format("""
            query GenerateShippingLabel {
                generateShippingLabel(shipmentId: "%s") {
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
            """, shipmentId);
    }

    public static String listShipmentsQuery(){
        return """
            query listShipments {
                listShipments {
                  id
                  orderNumber
                  priority
                  status
                  status
                  createDate
                }
              }
        """;
    }

    public static String shipmentDetailsQuery(long shipmentId){
        return String.format("""
            query {
                getShipmentDetailsById(shipmentId:%s) {
                    id
                orderNumber
                priority
                status
                createDate
                shippingCustomerCode
                locationCode
                deliveryType:
                shippingMethod
                productCategory
                shippingDate
                shippingCustomerName
                customerPhoneNumber
                customerAddressState
                customerAddressPostalCode
                customerAddressCountry
                customerAddressCountryCode
                customerAddressCity
                customerAddressDistrict
                customerAddressAddressLine1
                customerAddressAddressLine2
                completeDate
                completedByEmployeeId
                items {
                    id
                    shipmentId
                    productFamily
                    bloodType
                    quantity
                    comments
                    shortDateProducts {
                        id
                        shipmentItemId
                        unitNumber
                        productCode
                        storageLocation
                        comments
                        createDate
                        modificationDate
                    }
                    packedItems {
                        id
                        shipmentItemId
                        inventoryId
                        unitNumber
                        productCode
                        aboRh
                        productDescription
                        productFamily
                        expirationDate
                        collectionDate
                        packedByEmployeeId
                        visualInspection
                    }

                 }
                }
            }
            """, shipmentId);
    }
}
