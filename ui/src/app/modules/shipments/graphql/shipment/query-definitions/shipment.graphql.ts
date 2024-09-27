import { gql } from 'apollo-angular';
import {
    ShipmentDetailResponseDTO,
    ShipmentResponseDTO,
} from '../../../models/shipment-info.dto';

const LIST_SHIPMENTS = gql<{ listShipments: ShipmentResponseDTO[] }, never>`
    query listShipments {
        listShipments {
            id
            orderNumber
            priority
            status
            createDate
        }
    }
`;

const GET_SHIPMENT_BY_ID = gql<
    { getShipmentDetailsById: ShipmentDetailResponseDTO },
    { shipmentId: number }
>`
    query getShipmentDetailsById($shipmentId: ID!) {
        getShipmentDetailsById(shipmentId: $shipmentId) {
            id
            orderNumber
            priority
            status
            createDate
            shippingCustomerCode
            locationCode
            deliveryType
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
            comments
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
`;

export { GET_SHIPMENT_BY_ID, LIST_SHIPMENTS };
