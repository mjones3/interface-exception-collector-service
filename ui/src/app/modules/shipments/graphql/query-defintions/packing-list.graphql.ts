import { gql } from 'apollo-angular';
import { PackingListLabelDTO } from '../../models/packing-list.model';

export const GENERATE_PACKING_LIST_LABEL = gql<
    { generatePackingListLabel: PackingListLabelDTO },
    { shipmentId: number }
>`
    query GeneratePackingListLabel($shipmentId: ID!) {
        generatePackingListLabel(shipmentId: $shipmentId) {
            shipmentId
            orderNumber
            orderIdBase64Barcode
            shipmentIdBase64Barcode
            dateTimePacked
            packedBy
            enteredBy
            quantity
            distributionComments
            shipmentType
            labelStatus
            shipFrom {
                bloodCenterCode
                bloodCenterName
                bloodCenterAddressLine1
                bloodCenterAddressLine2
                bloodCenterAddressComplement
            }
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
        }
    }
`;
