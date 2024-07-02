export interface Facility {
    active: boolean;
    address1: string;
    address2?: string;
    city: string;
    closeDate: string;
    facilityTypeId: number;
    id: number;
    name: string;
    orderNumber: number;
    properties?: object;
    state: string;
    zip: string;
}
