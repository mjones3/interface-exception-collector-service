import { gql } from 'apollo-angular';

const LIST_SHIPMENT = gql`
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
`;

const GET_SHIPMENT_BY_ID = gql`
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

export { LIST_SHIPMENT, GET_SHIPMENT_BY_ID };
