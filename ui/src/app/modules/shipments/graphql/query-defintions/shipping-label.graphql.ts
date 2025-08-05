import { gql } from 'apollo-angular';
import { ShippingLabelDTO } from '../../models/shipping-label.model';

export const GENERATE_SHIPPING_LABEL = gql<
    { generateShippingLabel: ShippingLabelDTO },
    { shipmentId: number }
>`
    query GenerateShippingLabel($shipmentId: ID!) {
        generateShippingLabel(shipmentId: $shipmentId) {
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
`;
