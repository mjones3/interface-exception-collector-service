/* eslint-disable */
import { FuseNavigationItem } from '@fuse/components/navigation';

export const location: any = {
    id: 1,
    code: 'MDL_HUB_1',
    orderNumber: 1,
    active: true,
    locationTypeId: 3,
    partOfId: null,
    externalId: '123456789',
    name: 'MDL Hub 1',
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
        code: 'MDL_HUB_1',
        orderNumber: 1,
        active: true,
        locationTypeId: 1,
        partOfId: null,
        externalId: '123456789',
        name: 'MDL Hub 1',
        description: null,
        properties: {
            LABEL_ADDRESS_TYPE: '3',
            LICENSE_NUMBER: '2222',
            TZ: 'America/New_York',
            REGISTRATION_NUMBER: '44455',
        },
    },
    {
        id: 2,
        code: 'DISTRIBUTION_AND_LABELING',
        partOfId: null,
        externalId: 'DL1',
        name: 'Distribution and Labeling',
        description: null,
        locationTypeIds: 3,
        licenses: [],
        properties: {
            TZ: 'America/Denver',
        },
        orderNumber: 18,
        active: true,
    },
    {
        id: 3,
        code: 'DISTRIBUTION_ONLY',
        partOfId: 1036,
        externalId: 'DO1',
        name: 'Distribution Only',
        description: null,
        locationTypeIds: [2],
        licenses: [],
        properties: {
            TZ: 'America/New_York',
        },
        orderNumber: 20,
        active: true,
    },
    {
        id: 4,
        code: 'MDL_HUB_2',
        partOfId: 1036,
        externalId: '234567891',
        name: 'MDL Hub 2',
        description: null,
        locationTypeIds: [2],
        licenses: [],
        properties: {
            TZ: 'America/Chicago',
        },
        orderNumber: 20,
        active: true,
    },
];

//TODO: Change to the current application menu
export const defaultNavigation: FuseNavigationItem[] = [
    {
        id: 10,
        title: 'Orders',
        type: 'collapsable',
        icon: 'shopping_cart',
        children: [
            {
                id: 11,
                title: 'Search Orders',
                type: 'basic',
                icon: 'search',
                link: 'orders/search',
                disabled: false,
            }
        ],
        disabled: false,
    },
    {
        id: 20,
        title: 'Shipping',
        type: 'collapsable',
        icon: 'local_shipping',
        children: [
            {
                id: 21,
                title: 'Search Shipments',
                type: 'basic',
                icon: 'search',
                link: 'shipments/search',
                disabled: true,
            }
        ],
        disabled: true,
    }
];
