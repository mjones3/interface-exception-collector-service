import { gql } from 'apollo-angular';
import { Notification } from '../../models/notification.dto';

export interface PickListDTO {
    orderNumber: number;
    customer: PickListCustomerDTO;
    pickListItems: PickListItemDTO[];
    orderComments: string;
}

export interface PickListCustomerDTO {
    code: string;
    name: string;
}

export interface PickListItemDTO {
    productFamily: string;
    bloodType: string;
    quantity: number;
    comments: string;
    shortDateList: PickListItemShortDateDTO[];
}

export interface PickListItemShortDateDTO {
    unitNumber: string;
    productCode: string;
    aboRh: string;
    storageLocation: string;
}

export const GENERATE_PICK_LIST = gql<
    {
        generatePickList: {
            notifications: Notification[];
            data: PickListDTO;
        };
    },
    { orderId: number; skipInventoryUnavailable: boolean }
>`
    mutation GeneratePickList(
        $orderId: ID!
        $skipInventoryUnavailable: Boolean!
    ) {
        generatePickList(
            orderId: $orderId
            skipInventoryUnavailable: $skipInventoryUnavailable
        ) {
            notifications {
                name
                notificationType
                notificationMessage
            }
            data {
                orderNumber
                orderComments
                customer {
                    code
                    name
                }
                pickListItems {
                    productFamily
                    bloodType
                    quantity
                    comments
                    shortDateList {
                        unitNumber
                        productCode
                        aboRh
                        storageLocation
                    }
                }
            }
        }
    }
`;
