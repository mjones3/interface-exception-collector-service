/* eslint-disable */
import { FuseNavigationItem } from '@fuse/components/navigation';

export const location: any = {
    id: 1,
    code: '123456789',
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
        code: '123456789',
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
        code: 'DL1',
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
        code: 'DO1',
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
        code: '234567891',
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
        type: 'basic',
        icon: 'shopping_cart',
        link: 'orders/search',
        disabled: false,
    },
    {
        id: 11,
        title: 'External Transfer',
        type: 'basic',
        icon: 'compare_arrows',
        link: 'external-transfer',
        disabled: false,
    },
    {
        id: 12,
        title: 'Recovered Plasma Shipping',
        type: 'basic',
        link: 'recovered-plasma',
        icon: 'local_shipping',
        disabled: false,
    },
    {
        id: 13,
        title: 'Imports',
        type: 'basic',
        link: 'imports/enter-shipment-information',
        icon: 'local_shipping',
        disabled: false,
    },
];
