/* eslint-disable */
import { FuseNavigationItem } from '@fuse/components/navigation';

export const location: any = {
    id: 1,
    orderNumber: 1,
    active: true,
    locationTypeId: 1,
    partOfId: null,
    externalId: null,
    name: 'Miami',
    description: null,
    properties: {
        LABEL_ADDRESS_TYPE: '3',
        LICENSE_NUMBER: '2222',
        TZ: 'America/New_York',
        REGISTRATION_NUMBER: '44455',
    },
};

export const locations: any[] = [
    {
        id: 1,
        orderNumber: 1,
        active: true,
        locationTypeId: 1,
        partOfId: null,
        externalId: null,
        name: 'Miami',
        description: null,
        properties: {
            LABEL_ADDRESS_TYPE: '3',
            LICENSE_NUMBER: '2222',
            TZ: 'America/New_York',
            REGISTRATION_NUMBER: '44455',
        },
    },
    {
        id: 29,
        partOfId: 1002,
        externalId: '007000005',
        name: 'Broadway Blood Donation Center Tucson',
        description: null,
        locationTypeIds: [2],
        licenses: [],
        properties: {
            LICENSE_NUMBER: '190',
            TZ: 'America/Denver',
            REGISTRATION_NUMBER: '2072997',
        },
        orderNumber: 18,
        active: true,
    },
    {
        id: 1202,
        partOfId: 1036,
        externalId: 'B258',
        name: 'Charlotte Abbey Place Donor Center',
        description: null,
        locationTypeIds: [2],
        licenses: [],
        properties: {
            TZ: 'America/New_York',
        },
        orderNumber: 20,
        active: true,
    },
];

//TODO: Change to the current application menu
export const defaultNavigation: FuseNavigationItem[] = [
    {
        id: 1,
        title: 'Shipment',
        type: 'collapsable',
        icon: 'shopping_cart',
        children: [
            {
                id: 2,
                title: 'Order Fulfillment',
                type: 'basic',
                icon: 'search',
                link: '/feature',
            }
        ],
    },
];
